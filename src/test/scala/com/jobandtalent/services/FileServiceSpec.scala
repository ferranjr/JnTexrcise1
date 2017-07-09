package com.jobandtalent.services

import java.io.{BufferedWriter, File, FileWriter}

import com.jobandtalent.models.UserHandle
import org.scalatest.{FixtureContext, FlatSpec, Matchers}

import scala.io.Source
import scala.language.postfixOps

class FileServiceSpec
  extends FlatSpec
  with Matchers {

  val basePath = new File("src/test/resources")

  def withFileUsers(users: List[String])(testCode: File => Any): Unit = {

    val fileTest = File.createTempFile("user", "handles", basePath)
    val writer = new FileWriter(fileTest)
    try {
      writer.write(users.mkString("\n"))
      writer.flush()
      testCode(fileTest)
    } finally {
      fileTest.delete()
      writer.close()
    }
  }

  "FileService" should "load list of users from file" in
    withFileUsers(1 to 10 map { i => s"user$i" } toList) { fileTest =>

      val result = FileService.loadUserHandlers(fileTest.getAbsolutePath)
      result should contain theSameElementsAs (
        1 to 10 map { i => UserHandle(s"user$i") }
      )
    }

  it should "ignore repeated users" in
    withFileUsers(List("user1", "user2", "user1", "user2")) { fileTest =>

      val result = FileService.loadUserHandlers(fileTest.getAbsolutePath)
      result should contain theSameElementsAs (
        1 to 2 map { i => UserHandle(s"user$i") }
      )
    }

  it should "trim spaces" in
    withFileUsers(1 to 10 map { i => s" user$i " } toList) { fileTest =>

      val result = FileService.loadUserHandlers(fileTest.getAbsolutePath)
      result should contain theSameElementsAs (
        1 to 10 map { i => UserHandle(s"user$i") }
      )
    }

  it should "store result properly" in {

    val fileTemp = File.createTempFile("results", "test", basePath)
    fileTemp.deleteOnExit()

    FileService.saveResults(
      fileTemp.getAbsolutePath,
      List(
        Set(UserHandle("user1"), UserHandle("user2"), UserHandle("user3")),
        Set(UserHandle("user1"), UserHandle("user4"))
      )
    )

    val results = Source.fromFile(fileTemp.getAbsoluteFile).getLines().toList
    results should contain theSameElementsInOrderAs List(
      "user1 user2 user3",
      "user1 user4"
    )
  }
 }
