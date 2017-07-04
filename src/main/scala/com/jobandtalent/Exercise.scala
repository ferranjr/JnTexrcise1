package com.jobandtalent

import com.jobandtalent.models.{UserHandle, UserNode}
import com.jobandtalent.services.{FileService, GithubService, UserStorageService, TwitterService}
import com.jobandtalent.utils.Graph
import com.jobandtalent.utils.Graph.Edge

import scala.concurrent.{ExecutionContext, Future}


object Exercise
  extends App {


}


@Singleton
class Program(
  fileService: FileService,
  githubService: GithubService,
  twitterService: TwitterService,
  storageService: UserStorageService
)(implicit executionContext: ExecutionContext) {

  private[this] def genUserNodes(handles: Set[UserHandle]): Future[Set[UserNode]] = {
    val result =
      handles.map { userHandle =>
        githubService.getOrganisations(userHandle)
          .map { orgs =>
            UserNode(userHandle, orgs.toSet)
          }
      }

    Future.sequence(result)
  }

  private[this] def getAllEdges(handles: Set[UserHandle]): Future[List[Edge[UserNode]]] = {
    val edges =
      handles.map { userHandle =>
        twitterService
          .getFriends(userHandle)
          .map { friends =>
            friends
              .filter(el => handles.contains(UserHandle(el.screenName)))
              .map(Edge(userHandle,_))
          }
      }

    Future.sequence(edges).flatten
  }

  private[this] def getMaximalCliques(graph: Graph[UserNode]): Future[Set[Set[UserNode]]] = ???


  def process(path: String): Future[Unit] = {
    for {
      userHandles <- fileService.loadFile(path)
      userNodes <- genUserNodes(userHandles)
      _ <- storageService.addUserNodes(userNodes.toList)
      edges <- getAllEdges(userHandles)
      _ <- storageService.addEdges(edges)
      graph <- storageService.extractGraph
      cliques <- getMaximalCliques(graph)
      lines = cliques.map(_.mkString(",")).toList
      result <- fileService.saveFile(path, lines)
    }
      yield result
  }
}