package com.jobandtalent

import com.jobandtalent.models.{GHOrganisation, UserHandle}
import com.jobandtalent.services._
import com.jobandtalent.utils.Graph.Edge

import scala.concurrent.{ExecutionContext, Future}

object Exercise
  extends App {

}

class Program(
  fileService: FileService,
  githubService: GithubService,
  twitterService: TwitterService,
  storageService: UserStorageService
)(implicit executionContext: ExecutionContext) {

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
    val edges =
      userHandles.map { userHandle =>
        twitterService
          .getFriends(userHandle)
          .map { friends =>

            friends
              .filter(el => userHandles.contains(UserHandle(el.screenName)))
              .map(tu => Edge(userHandle, UserHandle(tu.screenName)))
          }
      }

    Future.sequence(edges).map(_.toList.flatten)
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
   *
   *
   * @param path
   * @return
   */
  def process(path: String): Future[List[Set[UserHandle]]] = {
    for {
      userHandles <- fileService.loadFile(path)
      _ <- storageService.addUserNodes(userHandles.toList)
      edges <- retrieveEdges(userHandles)
      orgs <- retrieveOrganisations(userHandles)
      _ <- addUserOgranisations(orgs)
      _ <- storageService.addEdges(edges)
      cliques <- storageService.getAllCliques
    } yield storageService.filterNonValidMaximalCliques(cliques)
  }
}