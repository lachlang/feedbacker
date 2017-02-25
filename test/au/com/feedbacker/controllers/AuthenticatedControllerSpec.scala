package au.com.feedbacker.controllers

import au.com.feedbacker.model.{CredentialStatus, Credentials, Person, PersonDao}
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import org.mockito.Matchers._
import play.api.test.Helpers._
import org.scalatestplus.play.PlaySpec
import play.api.mvc.{Result, Results}
import play.api.test.FakeRequest

import scala.concurrent.Future


/**
  * Created by lachlang on 25/02/2017.
  */
class AuthenticatedControllerSpec extends PlaySpec with MockitoSugar with Results {

  def testBody: Person => Result = { _ => Ok }
  val email: String = "test@email.com"
  val token: String = "token"

  "AuthenticationController#AuthenticatedAction" should {

    "should return forbidden when no session token is provided" in {
      // mocks
      val mockPersonDao = mock[PersonDao]
      val mockSessionManager = mock[SessionManager]
      when(mockSessionManager.extractToken(any())).thenReturn(None)

      // call
      val controller = new AuthenticatedController(mockPersonDao, mockSessionManager)
      val result: Future[Result] = controller.AuthenticatedAction(testBody).apply(FakeRequest())

      // verify
      status(result) mustBe 403
      contentAsString(result) mustBe ""
      verify(mockSessionManager).extractToken(any())
      verifyZeroInteractions(mockPersonDao)
    }
    "should return forbidden for an invalid user" in {
      // mocks
      val mockPersonDao = mock[PersonDao]
      val mockSessionManager = mock[SessionManager]
      when(mockSessionManager.extractToken(any())).thenReturn(Some(SessionToken(email, token)))
      when(mockPersonDao.findByEmail(email)).thenReturn(None)

      // call
      val controller = new AuthenticatedController(mockPersonDao, mockSessionManager)
      val result: Future[Result] = controller.AuthenticatedAction(testBody).apply(FakeRequest())

      // verify
      status(result) mustBe 403
      contentAsString(result) mustBe ""
      verify(mockSessionManager).extractToken(any())
      verify(mockPersonDao).findByEmail(email)
    }
    "should return the body function for a valid user and session" in {
      // mocks
      val mockPersonDao = mock[PersonDao]
      val mockSessionManager = mock[SessionManager]
      val testPerson: Person = Person(Some(1), "name", "role", Credentials(email, "password", CredentialStatus.Active), "boss@test.com")
      when(mockSessionManager.extractToken(any())).thenReturn(Some(SessionToken(email, token)))
      when(mockPersonDao.findByEmail(email)).thenReturn(Some(testPerson))

      // call
      val controller = new AuthenticatedController(mockPersonDao, mockSessionManager)
      val result: Future[Result] = controller.AuthenticatedAction(testBody).apply(FakeRequest())

      // verify
      status(result) mustBe 200
      contentAsString(result) mustBe ""
      verify(mockSessionManager).extractToken(any())
      verify(mockPersonDao).findByEmail(email)
    }
  }
}
