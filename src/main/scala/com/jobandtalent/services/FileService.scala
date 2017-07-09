package com.jobandtalent.services

import java.io.{BufferedWriter, FileWriter}

import com.jobandtalent.models.UserHandle
import com.typesafe.scalalogging.StrictLogging

import scala.io.Source

/**
  * FileService
  * ~~~~~~
  *
  * We will use it to load the userHandles from the given file
  *
  */
trait FileService
  extends StrictLogging {

  def loadUserHandlers(filePath: String): Set[UserHandle] = {
    logger.debug(s"Loading user handlers from $filePath")
    Source.fromFile(filePath).getLines().map { line =>
      line.trim()
    }
      .filterNot(_.isEmpty)
      .map(UserHandle.apply)
      .toSet
  }


  def saveResults(fileName: String, results: List[Set[UserHandle]]): Unit = {
    logger.debug(s"Saving results into $fileName")
    val bw = new BufferedWriter(new FileWriter(fileName))
    val s = results.map(_.map(_.value).mkString(" ")).mkString("\n")
    try{
      bw.write(s)
    } catch {
      case error: Throwable =>
        logger.error(s"Unable to save result into file: $fileName")
        throw error
    }
    finally{
      bw.close()
    }
  }

}

object FileService extends FileService