package com.jobandtalent.services

import com.jobandtalent.models.{GHOrganisation, UserHandle}
import com.jobandtalent.utils.Graph.Edge
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps

class DigraphUserStorageServiceSpec
  extends FlatSpec
  with Matchers
  with ScalaFutures {

  val handleUserA = UserHandle("userA")
  val handleUserB = UserHandle("userB")
  val handleUserC = UserHandle("userC")

  val orgGithub = GHOrganisation(1L, "github")
  val orgFooBar = GHOrganisation(2L, "foobar")

  "DigraphUserStorageService" should "add users properly" in {

    val storage = new DigraphUserStorageService()

    val result =
      for {
        _ <- storage.addUserHandle(handleUserA)
        _ <- storage.addUserHandle(handleUserB)
        _ <- storage.addUserHandle(handleUserC)
        _ <- storage.addUserOgranisations(handleUserA, Set(orgGithub))
        _ <- storage.addUserOgranisations(handleUserB, Set(orgFooBar))
      } yield ()

    whenReady(result) { _ =>
      val usersOrgsKeys = storage.organisations.keySet

      usersOrgsKeys should contain theSameElementsAs Set(handleUserA, handleUserB)
      storage.organisations(handleUserA) shouldBe Set(orgGithub)
      storage.organisations(handleUserB) shouldBe Set(orgFooBar)
    }
  }

  it should "add connexions properly" in {

    val storage = new DigraphUserStorageService()

    val result =
      for {
        _ <- storage.addUserHandle(handleUserA)
        _ <- storage.addUserHandle(handleUserB)
        _ <- storage.addUserHandle(handleUserC)
        _ <- storage.addUserOgranisations(handleUserA, Set(orgGithub))
        _ <- storage.addUserOgranisations(handleUserB, Set(orgFooBar))
        _ <- storage.addEdge(Edge(handleUserA, handleUserB))
        _ <- storage.addEdge(Edge(handleUserB, handleUserA))
        _ <- storage.addEdge(Edge(handleUserC, handleUserA))
      } yield ()

    whenReady(result) { _ =>
      val graph = storage.extractGraph

      graph.adjacentNodes(handleUserA) should contain theSameElementsAs Set(handleUserB)
      graph.adjacentNodes(handleUserB) should contain theSameElementsAs Set(handleUserA)
      graph.adjacentNodes(handleUserC) should contain theSameElementsAs Set(handleUserA)
    }
  }

  it should "check for same organisation" in {

    val storage = new DigraphUserStorageService()

    val result =
      for {
        _ <- storage.addUserHandle(handleUserA)
        _ <- storage.addUserHandle(handleUserB)
        _ <- storage.addUserHandle(handleUserC)
        _ <- storage.addUserOgranisations(handleUserA, Set(orgFooBar))
        _ <- storage.addUserOgranisations(handleUserB, Set(orgFooBar))
        _ <- storage.addUserOgranisations(handleUserC, Set(orgGithub))
      } yield ()

    whenReady(result) { _ =>
      val usersOrgsKeys = storage.organisations.keySet

      usersOrgsKeys should contain theSameElementsAs Set(
        handleUserA, handleUserB, handleUserC
      )
      storage.shareSameOrganisation(storage.organisations.keySet) shouldBe false
      storage.shareSameOrganisation(Set(handleUserA, handleUserB)) shouldBe true
    }
  }
}
