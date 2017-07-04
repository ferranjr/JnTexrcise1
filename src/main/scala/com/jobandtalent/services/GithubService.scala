package com.jobandtalent.services

import com.jobandtalent.models.{GHOrganisation, UserHandle}

import scala.concurrent.Future

/**
  * Github service
  * ~~~~~
  *
  * We will require to extract the user's organisations list in order to check if connected users
  * within a clique have at least on organisation in common.
  *
  *
  */
trait GithubService {

  /**
    * Fetch User organisations from Github Api
    *
    * @param user UserHandle to retrieve organisations from
    * @return List of organisations
    */
  def getOrganisations(user: UserHandle): Future[List[GHOrganisation]]
}
