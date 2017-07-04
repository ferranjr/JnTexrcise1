package com.jobandtalent.models

case class UserHandle(value: String)
  extends AnyVal

case class TwitterUser(
  id: Long,
  screenName: String
)

case class UserNode(
  userHandle: UserHandle,
  organisations: Set[GHOrganisation]
)

case class GHOrganisation(
  id: Long,
  login: String,
  description: String
)