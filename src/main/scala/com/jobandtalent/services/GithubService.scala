package com.jobandtalent.services

import com.jobandtalent.models.{GHOrganisation, UserHandle}
import codecheck.github.api.GitHubAPI

import scala.concurrent.{ExecutionContext, Future}

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

class GithubAPIService(
  private[this] val githubClient: GitHubAPI
)(implicit executionContext: ExecutionContext)
  extends GithubService {

  def getOrganisations(user: UserHandle): Future[List[GHOrganisation]] = {
    githubClient.listUserOrganizations(user = user.value).map { organisations =>
      organisations.map(org => GHOrganisation(org.id, org.login, org.description))
    }
  }
}