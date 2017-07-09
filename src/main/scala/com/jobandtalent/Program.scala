package com.jobandtalent

import com.jobandtalent.models.{GHOrganisation, UserHandle}
import com.jobandtalent.services.{FileService, GithubService, TwitterService, UserStorageService}
import com.jobandtalent.utils.Graph.Edge
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.{ExecutionContext, Future}


class Program(
  fileService: FileService,
  githubService: GithubService,
  twitterService: TwitterService,
  storageService: UserStorageService
)(implicit executionContext: ExecutionContext)
  extends StrictLogging {

  private[this] def retrieveOrganisations(handles: Set[UserHandle]): Future[List[(UserHandle, Set[GHOrganisation])]] = {
    val result =
      handles.map { userHandle =>
        githubService.getOrganisations(userHandle)
          .map { orgs =>
            userHandle -> orgs.toSet
          }
      }

    Future.sequence(result).map { _.toList }
  }

  private[this] def retrieveEdges(userHandles: Set[UserHandle]): Future[List[Edge[UserHandle]]] = {
    @volatile var setAlreadyChecked = Set.empty[String]

    val result =
      for {
        userA <- userHandles.toList
        userB <- userHandles.toList
        if userA != userB &&
           !(setAlreadyChecked.contains(userA.value + "-" + userB.value) ||
             setAlreadyChecked.contains(userB.value + "-" + userA.value))
        _ = setAlreadyChecked = setAlreadyChecked + s"${userA.value}-${userB.value}"
        _ = logger.debug(s"Current memo = $setAlreadyChecked")
      } yield {
        twitterService.getUsersFriendship(userA, userB)
          .map { mapEdges =>
            mapEdges.map { case (a, b) =>
              logger.debug(s"Defining edge $a - $b")
              Edge(a, b)
            }
          }
      }

    Future.sequence(result).map(_.flatten)
  }

  private[this] def addUserOgranisations(handleToOrganisations: List[(UserHandle, Set[GHOrganisation])]): Future[Unit] = {
    val result =
      handleToOrganisations.map {
        case (userHandle, orgs) =>
          storageService.addUserOgranisations(userHandle, orgs)
      }

    Future.sequence(result).map(_.foreach(_ => ()))
  }

  /**
   * Main method
   * ~~~~
   * @param path to the file to extract the results from
   * @return
   */
  def process(path: String): Future[List[Set[UserHandle]]] = {
    for {
      _           <- Future.unit
      userHandles  = fileService.loadUserHandlers(path)
      _           <- storageService.addUserNodes(userHandles.toList)
      edges       <- retrieveEdges(userHandles)
      _ = logger.debug(s"Edges: $edges")
      orgs        <- retrieveOrganisations(userHandles)
      _           <- addUserOgranisations(orgs)
      _           <- storageService.addEdges(edges)
      cliques     <- storageService.getAllCliques
      maximals     = storageService.filterNonValidMaximalCliques(cliques)
    }
      yield maximals
  }
}