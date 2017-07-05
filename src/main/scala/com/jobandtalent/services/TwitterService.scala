package com.jobandtalent.services

import com.danielasfregola.twitter4s.TwitterRestClient
import com.jobandtalent.models.{TwitterUser, UserHandle}

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
  extends TwitterService {

  def getFriends(user: UserHandle): Future[List[TwitterUser]] = {
    twitterClient.friendsForUser(user.value)
      .map { ratedUser =>
        ratedUser.data.users.map(TwitterUser.apply).toList
      }
  }
}