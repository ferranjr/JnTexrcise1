package com.jobandtalent.services

import com.jobandtalent.models.{UserHandle, UserNode}
import com.jobandtalent.utils.{Digraph, Graph}
import com.jobandtalent.utils.Graph.Edge

import scala.concurrent.Future


/**
  * StorageService
  * ~~~~~~
  *
  * We will use it to store all the users.
  * Internally we will use the Directional Graph, although this could be
  * replace by the usage of Graph Database if we wanted to
  */
trait UserStorageService {

  def extractGraph: Graph[UserNode]

  def addUserNode(user: UserNode): Future[Unit]
  def addUserNodes(users: List[UserNode]): Future[Unit]

  def addEdge(edge: Edge[UserNode]): Future[Unit]
  def addEdges(edges: List[Edge[UserNode]]): Future[Unit]

  def addConnection(a: UserNode, b: UserNode): Future[Unit]
}


trait DigraphUserStorageService
  extends UserStorageService {

  var graph: Digraph[UserNode]

  def extractGraph: Graph[UserNode] = graph

  def addUserNode(user: UserNode): Future[Unit] =
    Future.fromTry(graph.addNode(user).map(_ => ()))

  def addUserNodes(users: List[UserNode]): Future[Unit] =
    Future.sequence(users.map(addUserNode)).map(_.flatten)

  def addEdge(edge: Edge[UserNode]): Future[Unit] =
    Future.fromTry(graph.addEdge(edge).map(_ => ()))

  def addEdges(edges: List[Edge[UserNode]]): Future[Unit] =
    Future.sequence(edges.map(addEdge)).map(_.flatten)
}