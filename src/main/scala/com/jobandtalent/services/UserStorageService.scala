package com.jobandtalent.services

import com.jobandtalent.models.{GHOrganisation, UserHandle}
import com.jobandtalent.utils.{Digraph, Graph}
import com.jobandtalent.utils.Graph.Edge

import scala.annotation.tailrec
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global


/**
  * StorageService
  * ~~~~~~
  *
  * We will use it to store all the users.
  * Internally we will use the Directional Graph, although this could be
  * replace by the usage of Graph Database if we wanted to
  */
trait UserStorageService {

  def extractGraph: Graph[UserHandle]

  def addUserHandle(user: UserHandle): Future[Unit]
  def addUserNodes(users: List[UserHandle]): Future[Unit]

  def addEdge(edge: Edge[UserHandle]): Future[Unit]
  def addEdges(edges: List[Edge[UserHandle]]): Future[Unit]

  def addUserOgranisations(userHandle: UserHandle, orgs: Set[GHOrganisation]): Future[Unit]

  def getAllCliques: Future[Set[Set[UserHandle]]]
  def filterNonMaximalCliques(cliques: Set[Set[UserHandle]]): List[Set[UserHandle]]
}


class DigraphUserStorageService
  extends UserStorageService {

  var graph: Digraph[UserHandle] = Digraph[UserHandle]()
  var organisations: Map[UserHandle, Set[GHOrganisation]] = Map()

  def extractGraph: Digraph[UserHandle] = graph

  def addUserHandle(user: UserHandle): Future[Unit] =
    Future.fromTry {
      graph.addNode(user).map(g => graph = g)
    }

  def addUserNodes(users: List[UserHandle]): Future[Unit] =
    Future.sequence(users.map(addUserHandle)).map(_ => ())

  def addEdge(edge: Edge[UserHandle]): Future[Unit] =
    Future.fromTry {
      graph.addEdge(edge).map(g => graph = g)
    }

  def addEdges(edges: List[Edge[UserHandle]]): Future[Unit] =
    Future.sequence(edges.map(addEdge)).map(_ => ())

  def addUserOgranisations(userHandle: UserHandle, orgs: Set[GHOrganisation]): Future[Unit] = {
    Future(organisations.updated(userHandle, orgs))
  }

  def getAllCliques: Future[Set[Set[UserHandle]]] = {
    Future {
      graph.powerSet
        .filter { xs =>
          xs.nonEmpty && xs.size >= 2 &&
            (
              for {
                x <- xs.toList
                y <- xs.toList
                if x != y
              } yield graph.areStronglyConnected(x, y)
            ).forall(_ == true)
        }
    }
  }

  def filterNonMaximalCliques(
    cliques: Set[Set[UserHandle]]
  ): List[Set[UserHandle]] = {

    @tailrec
    def recurse(
      xs: List[Set[UserHandle]],
      solutions: List[Set[UserHandle]]
    ): List[Set[UserHandle]] = {
      if(xs.isEmpty)
        solutions
      else if(!solutions.exists(s => xs.head.forall(s.contains)))
        recurse(xs.tail, xs.head :: solutions)
      else
        recurse(xs.tail, solutions)
    }

    val orderedCliques = cliques.toList.sortWith { (a, b) => a.size > b.size }
    recurse(orderedCliques, List.empty[Set[UserHandle]])
  }

}