package au.com.feedbacker.controllers

import au.com.feedbacker.model.{CredentialStatus, Credentials, Person, PersonDao}
import play.api.test._
import play.api.test.Helpers._
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import org.mockito.Matchers._
import org.scalatestplus.play.PlaySpec
import play.api.libs.json.Json
import play.api.mvc.{Result, Results}
import play.api.test.FakeRequest

import scala.concurrent.Future

/**
  * Created by lachlang on 25/02/2017.
  */
class AuthenticationSpec extends PlaySpec with MockitoSugar with Results {

  val email: String = "email@test.com"
  val password: String = "password"

  def fixture = {
    new {
      val mockPersonDao = mock[PersonDao]
      val mockSessionManager = mock[SessionManager]
      val controller = new Authentication(mockPersonDao, mockSessionManager)
    }
  }

  "Authentication#login" should {

    "should return bad request when no request body" in {
      // setup
      val f = fixture
      val result: Future[Result] = f.controller.login().apply(FakeRequest())

      // verify
      status(result) mustBe 400
      contentAsString(result) mustBe ""
      verifyZeroInteractions(f.mockSessionManager)
      verifyZeroInteractions(f.mockPersonDao)
    }
    "should return bad request when username not provided" in {
      // setup
      val f = fixture
      val result: Future[Result] = f.controller.login().apply(FakeRequest().withJsonBody(Json.obj("body" -> Json.obj("password" -> password))))

      // verify
      status(result) mustBe 400
      contentAsString(result) mustBe ""
      verifyZeroInteractions(f.mockSessionManager)
      verifyZeroInteractions(f.mockPersonDao)
    }
    "should return bad request when password not provided" in {
      // setup
      val f = fixture
      val result: Future[Result] = f.controller.login().apply(FakeRequest().withJsonBody(Json.obj("body" -> Json.obj("username" -> email))))

      // verify
      status(result) mustBe 400
      contentAsString(result) mustBe ""
      verifyZeroInteractions(f.mockSessionManager)
      verifyZeroInteractions(f.mockPersonDao)
    }
    "should return forbidden when person not registered" in {
      // setup
      val f = fixture
      when(f.mockPersonDao.findByEmail(email)).thenReturn(None)
      val jsonRequest = Json.obj("body" -> (Json.obj("password" -> password) ++ Json.obj("username" -> email)))
      val result: Future[Result] = f.controller.login().apply(FakeRequest().withJsonBody(jsonRequest))

      // verify
      verify(f.mockPersonDao).findByEmail(email)
      status(result) mustBe 403
      contentAsString(result) mustBe ""
      verifyZeroInteractions(f.mockSessionManager)
    }
    "should return unauthorised when person is inactive" in {
      // setup
      val f = fixture
      val testPerson: Person = Person(Some(1), "name", "role", Credentials(email, password, CredentialStatus.Inactive), "boss@test.com")
      when(f.mockPersonDao.findByEmail(email)).thenReturn(Some(testPerson))
      val jsonRequest = Json.obj("body" -> (Json.obj("password" -> password) ++ Json.obj("username" -> email)))
      val result: Future[Result] = f.controller.login().apply(FakeRequest().withJsonBody(jsonRequest))

      // verify
      verify(f.mockPersonDao).findByEmail(email)
      status(result) mustBe 401
      contentAsString(result) mustBe ""
      verifyZeroInteractions(f.mockSessionManager)
    }
    "should return bad request when no session token is created" in {
      // setup
      val f = fixture
      val testPerson: Person = Person(Some(1), "name", "role", Credentials(email, password, CredentialStatus.Active), "boss@test.com")
      when(f.mockPersonDao.findByEmail(email)).thenReturn(Some(testPerson))
      when(f.mockSessionManager.initialiseToken(testPerson, password)).thenReturn(None)
      val jsonRequest = Json.obj("body" -> (Json.obj("password" -> password) ++ Json.obj("username" -> email)))
      val result: Future[Result] = f.controller.login().apply(FakeRequest().withJsonBody(jsonRequest))

      // verify
      verify(f.mockPersonDao).findByEmail(email)
      verify(f.mockSessionManager).initialiseToken(testPerson, password)
      status(result) mustBe 400
      contentAsString(result) mustBe ""
    }
    "should return success when login succeeds" in {
      // setup
      val f = fixture
      val testPerson: Person = Person(Some(1), "name", "role", Credentials(email, password, CredentialStatus.Active), "boss@test.com")
      val sessionToken: SessionToken = SessionToken(email,password)
      when(f.mockPersonDao.findByEmail(email)).thenReturn(Some(testPerson))
      when(f.mockSessionManager.initialiseToken(testPerson, password)).thenReturn(Some(sessionToken))
      when(f.mockSessionManager.signIn(sessionToken, Ok)).thenReturn(Ok)
      val jsonRequest = Json.obj("body" -> (Json.obj("password" -> password) ++ Json.obj("username" -> email)))
      val result: Future[Result] = f.controller.login().apply(FakeRequest().withJsonBody(jsonRequest))

      // verify
      verify(f.mockPersonDao).findByEmail(email)
      verify(f.mockSessionManager).initialiseToken(testPerson, password)
      verify(f.mockSessionManager).signIn(sessionToken, Ok)
      status(result) mustBe 200
      contentAsString(result) mustBe ""
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
