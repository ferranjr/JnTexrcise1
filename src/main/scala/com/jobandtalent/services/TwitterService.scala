package com.jobandtalent.services

import com.jobandtalent.models.{TwitterUser, UserHandle}

import scala.concurrent.Future


/**
  * TwitterService
  * ~~~~~~
  *
  * Used to retrieve the list of Users followed by a specific user in order to
  * specify the connections of our graph.
  *
  */
trait TwitterService {

  def getFriends(user: UserHandle): Future[List[TwitterUser]]
}
