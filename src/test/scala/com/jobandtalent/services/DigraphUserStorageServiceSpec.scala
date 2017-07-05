package com.jobandtalent.services

import com.jobandtalent.models.{GHOrganisation, UserHandle}
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps

class DigraphUserStorageServiceSpec
  extends FlatSpec
  with Matchers {

  val handleUserA = UserHandle("userA")
  val handleUserB = UserHandle("userB")
  val handleUserC = UserHandle("userC")

  val orgGithub = GHOrganisation(1L, "github", "description")
  val orgFooBar = GHOrganisation(2L, "foobar", "description")

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

    Await.ready(result, 5 seconds)

    storage.organisations.keySet should contain theSameElementsAs Set(
      handleUserA, handleUserB
    )

    storage.organisations.get(handleUserA) shouldBe Some(Set(orgGithub))
    storage.organisations.get(handleUserB) shouldBe Some(Set(orgFooBar))
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

    Await.ready(result, 5 seconds)

    storage.organisations.keySet should contain theSameElementsAs Set(
      handleUserA, handleUserB, handleUserC
    )

    storage.shareSameOrganisation(storage.organisations.keySet) shouldBe false
    storage.shareSameOrganisation(Set(handleUserA, handleUserB)) shouldBe true
  }
}
