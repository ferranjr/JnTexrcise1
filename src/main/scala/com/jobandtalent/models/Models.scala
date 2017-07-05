package com.jobandtalent.models

import com.danielasfregola.twitter4s.entities.{User => Twitter4sUser}

case class UserHandle(value: String)
  extends AnyVal

case class TwitterUser(
  id: Long,
  screenName: String
)

object TwitterUser {
  def apply(twitter4sUser: Twitter4sUser): TwitterUser =
    TwitterUser(twitter4sUser.id, twitter4sUser.screen_name)
}

case class UserNode(
  userHandle: UserHandle,
  organisations: Set[GHOrganisation]
)

case class GHOrganisation(
  id: Long,
  login: String,
  description: String
)