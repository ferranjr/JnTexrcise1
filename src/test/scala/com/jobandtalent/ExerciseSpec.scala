package com.jobandtalent

import com.jobandtalent.models.{GHOrganisation, TwitterUser, UserHandle}
import com.jobandtalent.services._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps

class ExerciseSpec
  extends FlatSpec
  with Matchers
  with ScalaFutures {

  import ExerciseSpec._

  val testProgram = new Program(
    mockFileService,
    mockGithubService,
    mockTwitterService,
    mockStorageService
  )

  implicit val patientConfig: PatienceConfig = PatienceConfig(timeout = 10 seconds)

  "process" should "get list of maximal cliques as expected" in {
    whenReady(testProgram.process(fakePath)) { res =>
      res should contain theSameElementsAs Set(
        Set(fooHandle, bazHandle)
      )
    }
  }
}

object ExerciseSpec {

  val fakePath = "foo.txt"

  val fooHandle = UserHandle("foo")
  val barHandle = UserHandle("bar")
  val bazHandle = UserHandle("baz")
  val otherHandle = UserHandle("other")

  val organisation1 = GHOrganisation(1L, "org1")
  val organisation2 = GHOrganisation(2L, "org2")
  val organisation3 = GHOrganisation(3L, "org3")
  val organisation4 = GHOrganisation(4L, "org4")

  val twitterUserFoo = TwitterUser(1L, "foo")
  val twitterUserBar = TwitterUser(2L, "bar")
  val twitterUserBaz = TwitterUser(3L, "baz")
  val twitterUserOther = TwitterUser(4L, "Other")

  val mockFileService = new FileService {
    override def loadUserHandlers(filePath: String): Set[UserHandle] =
      Set(fooHandle, barHandle, bazHandle)
  }

  val mockGithubService = new GithubService {
    def getOrganisations(user: UserHandle): Future[List[GHOrganisation]] =
      Future.successful {
        if (user == fooHandle)
          List(organisation1, organisation2)
        else if (user == bazHandle)
          List(organisation1)
        else
          List(organisation3)
      }
  }

  val mockTwitterService = new TwitterService {

    def getUsersFriendship(userA: UserHandle, userB: UserHandle): Future[Map[UserHandle, UserHandle]] =
      (userA, userB) match {
        case (a, b)
          if ((a == fooHandle && b == barHandle) || (a == barHandle && b == fooHandle)) ||
            ((a == fooHandle && b == bazHandle) || (a == bazHandle && b == fooHandle)) ||
            ((a == barHandle && b == bazHandle) || (a == bazHandle && b == barHandle))  =>
          Future.successful(Map(a -> b, b -> a))
        case (a, b)
          if (a == bazHandle && b == otherHandle) || (a == otherHandle && b == bazHandle) =>
          Future.successful(Map(a -> b))
        case _ =>
          Future.successful(Map())
      }

    def getFriends(user: UserHandle): Future[List[TwitterUser]] =
      Future.successful {
        if (user == fooHandle)
          List(twitterUserBar, twitterUserBaz)
        else if (user == barHandle)
          List(twitterUserFoo, twitterUserBaz)
        else if (user == bazHandle)
          List(twitterUserOther, twitterUserFoo, twitterUserBar)
        else
          List(twitterUserFoo, twitterUserOther, twitterUserBaz, twitterUserBar)
      }
  }

  val mockStorageService = new DigraphUserStorageService()
}