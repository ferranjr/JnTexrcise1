package com.jobandtalent.services

import com.danielasfregola.twitter4s.TwitterRestClient
import com.danielasfregola.twitter4s.entities.User
import com.danielasfregola.twitter4s.exceptions.TwitterException
import com.jobandtalent.models.{TwitterUser, UserHandle}
import com.typesafe.scalalogging.StrictLogging

import scala.annotation.tailrec
import scala.concurrent.{ExecutionContext, Future}

/**
 * TwitterService
 * ~~~~~~
 * Used to retrieve the list of Users followed by a specific user in order to
 * specify the connections of our graph.
 */
trait TwitterService {

  def getFriends(user: UserHandle): Future[List[TwitterUser]]
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

  def getFriends(user: UserHandle): Future[List[TwitterUser]] = {
    logger.debug(s"Retrieving connexions for ${user.value}")

    def helper(cursor: Long, acc: Seq[User]): Future[Seq[User]] = {
      twitterClient.friendsForUser(user.value, cursor, count = 100, skip_status = true)
        .flatMap { ratedUser =>
          logger.debug(s"Twitter Friends for ${user.value} cursor[$cursor]: \n")

          if(ratedUser.data.next_cursor != 0) {
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
        case t:TwitterException =>
          logger.error(s"Unable to extract list of friends from twitter. ${t.getMessage}", t)
          List()
      }
  }
}