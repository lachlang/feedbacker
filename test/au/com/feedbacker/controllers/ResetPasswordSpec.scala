package au.com.feedbacker.controllers

import au.com.feedbacker.model._
import au.com.feedbacker.util.Emailer
import play.api.libs.json._

import scala.concurrent.Future
import org.scalatestplus.play._
import play.api.mvc._
import play.api.test._
import play.api.test.Helpers._
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import org.mockito.Matchers._

/**
  * Created by lachlang on 16/02/2017.
  */
class ResetPasswordSpec extends PlaySpec with MockitoSugar with Results {

  val validEmail: String = "valid@test.com"
  "ResetPassword#resetPassword" should {
    "should return forbidden for no request body" in {
      // mock
      val mockEmailer = mock[Emailer]
      val mockPersonDao = mock[PersonDao]
      val mockActivationDao = mock[ActivationDao]
      val mockSessionManager = mock[SessionManager]
      when(mockSessionManager.extractToken(any())).thenReturn(None)

      // call
      val controller = new ResetPassword(mockEmailer, mockPersonDao, mockActivationDao, mockSessionManager)
      val result: Future[Result] = controller.resetPassword().apply(FakeRequest().withBody(JsNull))

      // verify
      status(result) mustBe 403
      contentAsString(result) mustBe ""
      verifyZeroInteractions(mockPersonDao)
    }
    "should return forbidden for an invalid request" in {
      // mocks
    }
    "should reject invalid tokens" in {

    }
    "should fail for invalid users" in {

    }
    "should return a bad request code when the user update fails" in {

    }
    "should reset the users password" in {
      // mock
    }
  }
  "ResetPassword#sendPasswordResetEmail" should {
    "should fail when no email provided" in {

    }
    "should fail when the user isn't registered" in {

    }
    "should send a password reset email" in {

    }
  }
}
