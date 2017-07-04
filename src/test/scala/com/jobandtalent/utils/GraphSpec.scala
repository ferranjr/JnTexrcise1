package com.jobandtalent.utils

import com.jobandtalent.utils.Graph.{Edge, EdgeAlreadyExists, MissingNodesForEdge, NodeAlreadyExists}
import org.scalatest.{FlatSpec, Matchers}

import scala.util.{Failure, Success}

class GraphSpec
  extends FlatSpec
    with Matchers {

  type Node = Int

  s"Digraph" should "be be empty if initialise without parameters" in {
    val graph: Graph[Node] = Digraph[Node]()

    graph.isEmpty shouldBe true
    graph.nonEmpty shouldBe false
  }

  it should "allow to add new nodes" in {
    val testNode = 1
    val graphTry =
      for {
        g1 <- Success(Digraph[Node]())
        g2 <- g1.addNode(testNode)
      }
        yield g2

    graphTry shouldBe a[Success[_]]
    graphTry.get.nodes should contain only testNode
  }

  it should "fail for already existing nodes" in {
    val testNode = 1

    val graphTry =
      for {
        g1 <- Success(Digraph[Node]())
        g2 <- g1.addNode(testNode)
        g3 <- g2.addNode(2)
        g4 <- g3.addNode(testNode)
      }
        yield g4

    graphTry shouldBe a[Failure[_]]
    a[NodeAlreadyExists[Node]] should be thrownBy {
      graphTry.get
    }
  }

  it should "connect nodes when adding edges" in {
    val graphTry =
      for {
        g1 <- Success(Digraph[Node]())
        g2 <- g1.addNode(1)
        g3 <- g2.addNode(2)
        g4 <- g3.addNode(3)
        g5 <- g4.addNode(4)
        g6 <- g5.addEdge(Edge(1, 2))
        g7 <- g6.addEdge(Edge(2, 3))
        g8 <- g7.addEdge(Edge(2, 1))
      } yield g8

    graphTry shouldBe a[Success[_]]
    val graph = graphTry.get

    graph.nodes should contain theSameElementsAs Set(1, 2, 3, 4)
    graph.adjacentNodes(1) should contain theSameElementsAs Set(2)
    graph.adjacentNodes(2) should contain theSameElementsAs Set(1, 3)
  }

  it should "fail to connect non-existing nodes" in {
    val graphTry =
      for {
        g1 <- Success(Digraph[Node]())
        g2 <- g1.addNode(1)
        g3 <- g2.addNode(2)
        g4 <- g3.addEdge(Edge(2, 3))
      } yield g4

    graphTry shouldBe a[Failure[_]]
    a[MissingNodesForEdge[Node]] should be thrownBy {
      graphTry.get
    }
  }

  it should "fail to connect already connected nodes" in {
    val graphTry =
      for {
        g1 <- Success(Digraph[Node]())
        g2 <- g1.addNode(1)
        g3 <- g2.addNode(2)
        g4 <- g3.addEdge(Edge(1, 2))
        g5 <- g4.addEdge(Edge(1, 2))
      } yield g5

    graphTry shouldBe a[Failure[_]]
    a[EdgeAlreadyExists[Node]] should be thrownBy {
      graphTry.get
    }
  }

  it should "check if contains element correctly" in {
    val graphTry =
      for {
        g1 <- Success(Digraph[Node]())
        g2 <- g1.addNode(1)
      } yield g2

    graphTry shouldBe a[Success[_]]
    val graph = graphTry.get

    graph.contains(2) shouldBe false
    graph.contains(1) shouldBe true
  }

  it should "check if nodes are connected" in {
    val graphTry =
      for {
        g1 <- Success(Digraph[Node]())
        g2 <- g1.addNode(1)
        g3 <- g2.addNode(2)
        g4 <- g3.addEdge(Edge(1, 2))
      } yield g4

    graphTry shouldBe a[Success[_]]
    val graph = graphTry.get

    graph.areConnected(1, 2) shouldBe true
    graph.areConnected(2, 1) shouldBe false
    graph.areConnected(3, 1) shouldBe false
  }

  it should "check if strongly connected" in {
    val graphTry =
      for {
        g1 <- Success(Digraph[Node]())
        g2 <- g1.addNode(1)
        g3 <- g2.addNode(2)
        g4 <- g3.addNode(3)
        g5 <- g4.addEdge(Edge(1, 2))
        g6 <- g5.addEdge(Edge(2, 1))
        g7 <- g6.addEdge(Edge(1, 3))
      } yield g7

    graphTry shouldBe a[Success[_]]
    val graph = graphTry.get

    graph.areStronglyConnected(1, 2) shouldBe true
    graph.areStronglyConnected(2, 1) shouldBe true
    graph.areStronglyConnected(1, 3) shouldBe false
  }
}
