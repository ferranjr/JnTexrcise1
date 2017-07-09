package com.jobandtalent

import com.ning.http.client.AsyncHttpClient
import codecheck.github.transport.asynchttp19.AsyncHttp19Transport
import codecheck.github.api.GitHubAPI
import com.danielasfregola.twitter4s.TwitterRestClient
import com.jobandtalent.models.UserHandle
import com.jobandtalent.services._
import com.typesafe.config.ConfigFactory

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success, Try}


object Exercise {

  private val conf = ConfigFactory.load()
  private val githubAccessToken = conf.getString("github.access.token")

  private val fileService = FileService

  private val httpClient = new AsyncHttp19Transport(new AsyncHttpClient())
  private val githubClient = GitHubAPI(githubAccessToken)(httpClient)
  private val githubService = new GithubAPIService(githubClient)

  private val twitterService = new Twitter4sService(TwitterRestClient())

  private val storageService = new DigraphUserStorageService()

  private val program = new Program(
    fileService = fileService,
    githubService = githubService,
    twitterService = twitterService,
    storageService = storageService
  )

  def main(args: Array[String]): Unit = {
    for {
      fileOrigin      <- Try(args(0))
      fileDestination <- Try(args(1)).orElse(Success("results.txt"))
    }
      yield {
        program.process(fileOrigin)
          .onComplete {
            case Success(results) =>
              println(s"Request processed successfully, check your results at $results.")
              fileService.saveResults(fileDestination, results)
            case Failure(error) =>
              println(s"Something went wrong processing your request.")
          }
      }
  }
}