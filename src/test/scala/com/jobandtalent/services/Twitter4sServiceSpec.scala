package com.jobandtalent.services

import com.danielasfregola.twitter4s.TwitterRestClient
import com.danielasfregola.twitter4s.entities._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.mockito.Mockito._
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.Future
import scala.language.postfixOps
import java.util.Date
import scala.concurrent.ExecutionContext.Implicits.global

import com.jobandtalent.models.{TwitterUser, UserHandle}


class Twitter4sServiceSpec
  extends FlatSpec
  with Matchers
  with ScalaFutures
  with MockitoSugar {


  "Twitter4sService" should "convert results into our TwitterUser" in {

    val mockClient = mock[TwitterRestClient]

    when(mockClient.friendsForUser("ferranjr")).thenReturn(Future.successful(
      RatedData(RateLimit(0,0, 0), Users(
        Seq(fakeUser(1L, "foo"), fakeUser(2L, "bar"), fakeUser(3L, "baz")), 0, 0)
    )))

    val testService = new Twitter4sService(mockClient)

    whenReady(testService.getFriends(UserHandle("ferranjr"))) { res =>
      res should contain theSameElementsAs Set(
        TwitterUser(1L, "foo"), TwitterUser(2L, "bar"), TwitterUser(3L, "baz")
      )
    }
  }

  private def fakeUser(id: Long, screenName: String): User = {
    User(
      created_at = new Date(),
      favourites_count = 12,
      followers_count = 20,
      friends_count = 10,
      id = id,
      id_str = id.toString,
      lang = "English",
      listed_count = 10,
      name = screenName,
      profile_background_color = "#FFEFFE",
      profile_background_image_url = "foo.png",
      profile_background_image_url_https = "foo.png",
      profile_image_url = ProfileImage("http://ferranjr.com/foo_original.png"),
      profile_image_url_https = ProfileImage("http://ferranjr.com/foo_original.png"),
      profile_link_color = "#FFEFFE",
      profile_sidebar_border_color = "#FFEFFE",
      profile_sidebar_fill_color = "#FFEFFE",
      profile_text_color = "#FFEFFE",
      screen_name = screenName,
      statuses_count = 30
    )
  }
}
