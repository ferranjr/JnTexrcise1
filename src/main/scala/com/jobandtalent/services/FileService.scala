package com.jobandtalent.services

import java.io.{BufferedWriter, File, FileWriter}

import com.jobandtalent.models.UserHandle

import scala.io.Source

/**
  * FileService
  * ~~~~~~
  *
  * We will use it to load the userHandles from the given file
  *
  */
trait FileService {

  def loadUserHandlers(filePath: String): Set[UserHandle] =
    Source.fromFile(filePath).getLines().map{ line =>
      line.trim()
    }
    .filterNot(_.isEmpty)
    .map(UserHandle.apply)
    .toSet


  def saveResults(fileName: String, results: List[Set[UserHandle]]): Unit = {

    val bw = new BufferedWriter(new FileWriter(fileName))
    val s = results.map(_.map(_.value).mkString(" ")).mkString("\n")
    try{
      bw.write(s)
    } catch {
      case error: Throwable =>
        println(s"Unable to save result into file: $fileName")
    }
    finally{
      bw.close()
    }
  }

}

object FileService extends FileService