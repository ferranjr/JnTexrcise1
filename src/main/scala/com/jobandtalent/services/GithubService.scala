package com.jobandtalent.services

import com.jobandtalent.models.{GHOrganisation, UserHandle}
import codecheck.github.api.GitHubAPI
import codecheck.github.exceptions.NotFoundException
import com.typesafe.scalalogging.StrictLogging

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
  extends GithubService
  with StrictLogging {

  def getOrganisations(user: UserHandle): Future[List[GHOrganisation]] = {
    logger.debug(s"Retrieving organisations for user: ${user.value}")
    githubClient.listUserOrganizations(user = user.value)
      .map { organisations =>
        logger.debug(s"Organisations for ${user.value}: ${organisations.map(_.login).mkString("," )}")
        organisations.map(org => GHOrganisation(org.id, org.login))
      }
      .recover {
        case t: NotFoundException =>
          logger.warn(s"User ${user.value} doesn't have a github account.")
          List()
      }
  }
}