package com.jobandtalent.services

import codecheck.github.api.GitHubAPI
import codecheck.github.models.Organization
import com.jobandtalent.models.{GHOrganisation, UserHandle}
import org.json4s.JsonAST.{JLong, JObject, JString, JValue}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{FlatSpec, Matchers}
import org.mockito.Mockito._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class GithubAPIServiceSpec
  extends FlatSpec
  with Matchers
  with MockitoSugar
  with ScalaFutures {

  "GithubApiService" should "convert organisations into our format" in {
    val githubClient = mock[GitHubAPI]

    when(githubClient.listUserOrganizations("ferranjr")).thenReturn(Future.successful(
      List(fakeOrganization(1L, "jobandtalent"), fakeOrganization(2L, "ferranjr"))
    ))

    val testService = new GithubAPIService(githubClient)

    whenReady(testService.getOrganisations(UserHandle("ferranjr"))) { res =>
      res should contain theSameElementsAs Set(
        GHOrganisation(1L, "jobandtalent"),
        GHOrganisation(2L, "ferranjr")
      )
    }
  }

  def fakeOrganization(id: Long, login: String): Organization =
    new Organization(
      JObject(
        List(
          ("id", JLong(id)),
          ("login", JString(login)),
          ("description", JString("foo"))
        )
      )
    )

}
