package com.jobandtalent.services

import com.danielasfregola.twitter4s.TwitterRestClient
import com.danielasfregola.twitter4s.entities.{Relationship, User}
import com.danielasfregola.twitter4s.exceptions.TwitterException
import com.jobandtalent.models.{TwitterUser, UserHandle}
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.{ExecutionContext, Future}

/**
 * TwitterService
 * ~~~~~~
 * Used to retrieve the list of Users followed by a specific user in order to
 * specify the connections of our graph.
 */
trait TwitterService {

  def getFriends(user: UserHandle): Future[List[TwitterUser]]
  def getUsersFriendship(userA: UserHandle, userB: UserHandle): Future[Map[UserHandle, UserHandle]]
}

/**
 * Twitter4sService
 * ~~~~~~
 * Implementation of twitter client, picks up settings directly.
 */
class Twitter4sService(
  private[this] val twitterClient: TwitterRestClient
)(implicit executionContext: ExecutionContext)
  extends TwitterService
  with StrictLogging {

  def getUsersFriendship(userA: UserHandle, userB: UserHandle): Future[Map[UserHandle, UserHandle]] = {
    logger.debug(s"Checking friendship between $userA and $userB")
    twitterClient.relationshipBetweenUsers(userA.value, userB.value)
      .map { ratedFriendship =>
        val relationship = ratedFriendship.data.relationship
        logger.debug(s"Got ratedFriendship between $userA and $userB.\n${relationship.source}\n${relationship.target}")
        (relationship.source.following, relationship.source.followed_by) match {
          case (true, true) =>
            Map(userA -> userB, userB -> userA)
          case (true, false) =>
            Map(userA -> userB)
          case (false, true) =>
            Map(userB -> userA)
          case (false, false) =>
            Map.empty[UserHandle, UserHandle]
        }
      }
      .recover {
        case t: Throwable =>
          logger.error(s"Error processing relationship between: $userA and $userB: ${t.getMessage}", t)
          Map.empty[UserHandle, UserHandle]
      }
  }

  @deprecated(s"This option has too strict rate limitations to be properly useful, check for friendship instead")
  def getFriends(user: UserHandle): Future[List[TwitterUser]] = {
    logger.debug(s"Retrieving connexions for ${user.value}")

    def helper(cursor: Long, acc: Seq[User]): Future[Seq[User]] = {
      twitterClient.friendsForUser(user.value, cursor, count = 100, skip_status = true)
        .flatMap { ratedUser =>
          logger.debug(s"Twitter Friends for ${user.value} cursor[$cursor]: \n")
          logger.debug(s"Current rate limits: \n- ${ratedUser.rate_limit}")

          if (ratedUser.data.next_cursor != 0) {
            helper(ratedUser.data.next_cursor, ratedUser.data.users ++ acc)
          } else {
            Future.successful(ratedUser.data.users ++ acc)
          }
        }
    }

    helper(cursor = -1, List()).map { friends =>
      logger.debug(s"Friends for $user:\n${friends.map(_.screen_name).mkString("- ", ", ", ".")}")
      friends.map(TwitterUser.apply).toList
    }
      .recover {
        case t: TwitterException =>
          logger.error(s"Unable to extract list of friends from twitter. ${t.getMessage}", t)
          List()
      }
  }

}