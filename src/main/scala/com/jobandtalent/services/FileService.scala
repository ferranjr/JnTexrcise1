package com.jobandtalent.services

import com.jobandtalent.models.UserHandle

import scala.concurrent.Future

/**
  * FileService
  * ~~~~~~
  *
  * We will use it to load the userHandles from the given file
  *
  */
trait FileService {

  def loadFile(filePath: String): Future[Set[UserHandle]]
}
