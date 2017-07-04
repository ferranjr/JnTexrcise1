package com.jobandtalent.utils

import scala.util.{Failure, Success, Try}
import Graph._

import scala.annotation.tailrec

/**
  * Graph implementation to be the core of this application,
  * as the edges between nodes are defined by user following another user, this
  * is a directional graph.
  *
  * We can abstract over node as it really doesn't matter the type of node we use
  * for the implementation of the graph.
  */
trait Graph[Node] {

  def nodes: Set[Node]
  def adjacentNodes: Map[Node, Set[Node]]

  def addNode(in: Node): Try[Graph[Node]]
  def addEdge(in: Edge[Node]): Try[Graph[Node]]

  def isEmpty: Boolean = nodes.isEmpty
  def nonEmpty: Boolean = !isEmpty

  def contains(in: Node): Boolean = nodes.contains(in)
  def areConnected(a: Node, b: Node): Boolean = adjacentNodes.get(a).exists(_.contains(b))
  def areStronglyConnected(a: Node, b: Node): Boolean = areConnected(a, b)

  def powerSet: Set[Set[Node]] = {
    @tailrec
    def pwr(t: Set[Node], pwrSet: Set[Set[Node]]): Set[Set[Node]] =
      if (t.isEmpty) pwrSet
      else pwr(t.tail, pwrSet ++ pwrSet.map(_ + t.head))

    pwr(nodes, Set(Set.empty[Node]))
  }
}

object Graph {
  case class Edge[Node](from: Node, to: Node)


  sealed abstract class GraphError(msg: String)
    extends Exception(msg)

  case class NodeAlreadyExists[Node](node: Node)
    extends GraphError(s"Node $node already exists")

  case class EdgeAlreadyExists[Node](edge: Edge[Node])
    extends GraphError(s"Edge $edge already exists")

  case class MissingNodesForEdge[Node](a: Node, b: Node)
    extends GraphError(s"Node ")
}


case class Digraph[Node](adjacentNodes: Map[Node, Set[Node]] = Map.empty[Node,Set[Node]])
  extends Graph[Node] {

  lazy val nodes: Set[Node] = adjacentNodes.keySet

  def addNode(in: Node): Try[Digraph[Node]] = {
    if(nodes.contains(in)) {
      Failure(NodeAlreadyExists(in))
    } else {
      Success(Digraph(adjacentNodes + (in -> Set())))
    }
  }

  override def areStronglyConnected(a: Node, b: Node): Boolean = {
    areConnected(a, b) && areConnected(b, a)
  }

  def addEdge(in: Edge[Node]): Try[Digraph[Node]] = {
    if(!nodes.contains(in.from) || !nodes.contains(in.to)) {
      Failure(MissingNodesForEdge(in.from, in.to))
    } else if (adjacentNodes(in.from).contains(in.to)) {
      Failure(EdgeAlreadyExists(in))
    } else {
      val updatedSet = adjacentNodes(in.from) + in.to
      Success(Digraph(adjacentNodes.updated(in.from, updatedSet)))
    }
  }
}