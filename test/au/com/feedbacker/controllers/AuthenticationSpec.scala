package au.com.feedbacker.controllers

import au.com.feedbacker.AllFixtures
import au.com.feedbacker.model.{CredentialStatus, Credentials, Person, PersonDao}
import play.api.test.Helpers._
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import org.mockito.Matchers._
import org.scalatest.prop.PropertyChecks
import org.scalatestplus.play.PlaySpec
import play.api.libs.json.Json
import play.api.mvc.{Result, Results}
import play.api.test.FakeRequest

import scala.concurrent.Future

/**
  * Created by lachlang on 25/02/2017.
  */
class AuthenticationSpec extends PlaySpec with MockitoSugar with AllFixtures with PropertyChecks {

  def fixture = {
    new {
      val mockPersonDao = mock[PersonDao]
      val mockSessionManager = mock[SessionManager]
      val controller = new Authentication(mockPersonDao, mockSessionManager)
    }
  }

  "Authentication#login" should {

    "return bad request when no request body" in {
      // setup
      val f = fixture
      val result: Future[Result] = f.controller.login().apply(FakeRequest())

      // verify
      status(result) mustBe 400
      contentAsString(result) mustBe ""
      verifyZeroInteractions(f.mockSessionManager)
      verifyZeroInteractions(f.mockPersonDao)
    }
    "return bad request when username not provided" in {
      forAll() { password: String =>
        // setup
        val f = fixture
        val result: Future[Result] = f.controller.login().apply(FakeRequest().withJsonBody(Json.obj("body" -> Json.obj("password" -> password))))

        // verify
        status(result) mustBe 400
        contentAsString(result) mustBe ""
        verifyZeroInteractions(f.mockSessionManager)
        verifyZeroInteractions(f.mockPersonDao)
      }
    }
    "return bad request when username not a valid email address" in {
      forAll() { email: String =>
        // setup
        val f = fixture
        val result: Future[Result] = f.controller.login().apply(FakeRequest().withJsonBody(Json.obj("body" -> Json.obj("username" -> email))))

        // verify
        status(result) mustBe 400
        contentAsString(result) mustBe ""
        verifyZeroInteractions(f.mockSessionManager)
        verifyZeroInteractions(f.mockPersonDao)
      }
    }
    "return bad request when password not provided" in {
      forAll() { credentials: Credentials =>
        // setup
        val f = fixture
        val result: Future[Result] = f.controller.login().apply(FakeRequest().withJsonBody(Json.obj("body" -> Json.obj("username" -> credentials.email))))

        // verify
        status(result) mustBe 400
        contentAsString(result) mustBe ""
        verifyZeroInteractions(f.mockSessionManager)
        verifyZeroInteractions(f.mockPersonDao)
      }
    }
    "return forbidden when person not registered" in {
      forAll() { (credentials: Credentials, password: String) =>
        // setup
        val f = fixture
        when(f.mockPersonDao.findByEmail(credentials.email.toLowerCase)).thenReturn(None)
        val jsonRequest = Json.obj("body" -> (Json.obj("password" -> password) ++ Json.obj("username" -> credentials.email)))
        val result: Future[Result] = f.controller.login().apply(FakeRequest().withJsonBody(jsonRequest))

        // verify
        verify(f.mockPersonDao).findByEmail(credentials.email.toLowerCase)
        status(result) mustBe 403
        contentAsString(result) mustBe ""
        verifyZeroInteractions(f.mockSessionManager)
      }
    }
    "return unauthorised when person is not active" in {
      forAll() { (person: Person, password: String) =>
        whenever(person.credentials.status != CredentialStatus.Active) {
          // setup
          val f = fixture
          when(f.mockPersonDao.findByEmail(person.credentials.email.toLowerCase)).thenReturn(Some(person))
          val jsonRequest = Json.obj("body" -> (Json.obj("password" -> password) ++ Json.obj("username" -> person.credentials.email)))
          val result: Future[Result] = f.controller.login().apply(FakeRequest().withJsonBody(jsonRequest))

          // verify
          verify(f.mockPersonDao).findByEmail(person.credentials.email.toLowerCase)
          status(result) mustBe 401
          contentAsString(result) mustBe ""
          verifyZeroInteractions(f.mockSessionManager)
        }
      }
    }
    "return bad request when no session token is created" in {
      forAll() { (example: Person, password: String) =>
        // setup
        val person = example.copy(credentials = example.credentials.copy(status = CredentialStatus.Active))
        val f = fixture
        when(f.mockPersonDao.findByEmail(person.credentials.email.toLowerCase)).thenReturn(Some(person))
        when(f.mockSessionManager.initialiseToken(person, password)).thenReturn(None)
        val jsonRequest = Json.obj("body" -> (Json.obj("password" -> password) ++ Json.obj("username" -> person.credentials.email)))
        val result: Future[Result] = f.controller.login().apply(FakeRequest().withJsonBody(jsonRequest))

        // verify
        verify(f.mockPersonDao).findByEmail(person.credentials.email.toLowerCase)
        verify(f.mockSessionManager).initialiseToken(person, password)
        status(result) mustBe 400
        contentAsString(result) mustBe ""
      }
    }
    "return success when login succeeds" in {
      forAll() { (example: Person, password: String) =>
        // setup
        val person = example.copy(credentials = example.credentials.copy(status = CredentialStatus.Active))
        val f = fixture
        val sessionToken: SessionToken = SessionToken(person.credentials.email.toLowerCase, password)
        when(f.mockPersonDao.findByEmail(person.credentials.email.toLowerCase)).thenReturn(Some(person))
        when(f.mockSessionManager.initialiseToken(person, password)).thenReturn(Some(sessionToken))
        when(f.mockSessionManager.signIn(sessionToken, Ok)).thenReturn(Ok)
        val jsonRequest = Json.obj("body" -> (Json.obj("password" -> password) ++ Json.obj("username" -> person.credentials.email)))
        val result: Future[Result] = f.controller.login().apply(FakeRequest().withJsonBody(jsonRequest))

        // verify
        verify(f.mockPersonDao).findByEmail(person.credentials.email.toLowerCase)
        verify(f.mockSessionManager).initialiseToken(person, password)
        verify(f.mockSessionManager).signIn(sessionToken, Ok)
        status(result) mustBe 200
        contentAsString(result) mustBe ""
      }
    }
  }
  "Authentication#logout" should {

    "return bad request when the session is not valid" in {
      // setup
      val f = fixture
      when(f.mockSessionManager.extractToken(any())).thenReturn(None)
      val result: Future[Result] = f.controller.logout().apply(FakeRequest())

      // verify
      status(result) mustBe 400
      contentAsString(result) mustBe ""
      verifyZeroInteractions(f.mockPersonDao)
    }
    "sign out the session" in {
      // setup
      val f = fixture
      val sessionToken: SessionToken = SessionToken("email@test.com", "token")
      when(f.mockSessionManager.extractToken(any())).thenReturn(Some(sessionToken))
      when(f.mockSessionManager.signOut(sessionToken, Ok)).thenReturn(Ok)
      val result: Future[Result] = f.controller.logout().apply(FakeRequest())

      // verify
      status(result) mustBe 200
      contentAsString(result) mustBe ""
      verify(f.mockSessionManager).signOut(sessionToken, Ok)
      verifyZeroInteractions(f.mockPersonDao)
    }
  }
}
