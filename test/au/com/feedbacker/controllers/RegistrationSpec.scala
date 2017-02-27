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
class RegistrationSpec extends PlaySpec with MockitoSugar with Results {

  val validEmail: String = "valid@test.com"
  val upperEmail: String = "VALID@test.COM"
  val bossEmail: String = "boss@test.com"
  val name: String = "Test Guy"
  val role: String = "El Guapo"
  val password: String = "passwordThisIsAHilariousPassword"
  val registrationContent = RegistrationContent(name, role, upperEmail, password, bossEmail)

  def fixture = {
    new {
      val mockEmailer = mock[Emailer]
      val mockPersonDao = mock[PersonDao]
      val mockCredentialsDao = mock[CredentialsDao]
      val mockActivationDao = mock[ActivationDao]
      val mockSessionManager = mock[SessionManager]
      val controller = new Registration(mockEmailer, mockPersonDao, mockCredentialsDao, mockActivationDao, mockSessionManager)
    }
  }
  "Registration#register" should {
    "reject empty requests" in {
      val f = fixture
      val body: JsValue = JsNull
      val result: Future[Result] = f.controller.register().apply(FakeRequest().withBody(body))

      // verify
      status(result) mustBe 400
      contentAsJson(result) mustBe Json.obj("body" -> Json.obj("message" -> "Could not parse request."))
      verifyZeroInteractions(f.mockEmailer)
      verifyZeroInteractions(f.mockPersonDao)
      verifyZeroInteractions(f.mockCredentialsDao)
      verifyZeroInteractions(f.mockActivationDao)
    }
    "reject invalid payloads" in {
      val f = fixture
      val body: JsValue = Json.obj("some" -> "object")
      val result: Future[Result] = f.controller.register().apply(FakeRequest().withBody(body))

      // verify
      status(result) mustBe 400
      contentAsJson(result) mustEqual Json.obj("body" -> Json.obj("message" -> "Could not parse request."))

    }
    "reject duplicate user names" in {
      val f = fixture
      when(f.mockCredentialsDao.findStatusByEmail(validEmail)).thenReturn(Some(1L, CredentialStatus.Active))
      val body: JsValue = Json.toJson(registrationContent)
      val result: Future[Result] = f.controller.register().apply(FakeRequest().withBody(body))

      // verify
      verify(f.mockCredentialsDao).findStatusByEmail(validEmail)
      status(result) mustBe 409
      contentAsJson(result) mustBe Json.obj("body" -> Json.obj("message" -> "User is already registered."))
    }
    "return bad request when failing to send email" in {
      val testPerson = Person(Some(1), name,role, Credentials(validEmail,password,CredentialStatus.Inactive),bossEmail, false)
      val f = fixture
      when(f.mockSessionManager.hash(password)).thenReturn(password)
      when(f.mockCredentialsDao.findStatusByEmail(validEmail)).thenReturn(None)
      when(f.mockPersonDao.create(any())).thenReturn(Right(testPerson))
      when(f.mockActivationDao.createActivationToken(validEmail)).thenReturn(None)
      val body: JsValue = Json.toJson(registrationContent)
      val result: Future[Result] = f.controller.register().apply(FakeRequest().withBody(body))

      // verify
      status(result) mustBe 400
      contentAsJson(result) mustBe Json.obj("body" -> Json.obj("message" -> "Could not send activation email."))
    }
    "return an error when the db write fails" in {
      val testPerson = Person(None, name,role, Credentials(validEmail,password,CredentialStatus.Inactive),bossEmail, false)
      val f = fixture
      when(f.mockSessionManager.hash(password)).thenReturn(password)
      when(f.mockCredentialsDao.findStatusByEmail(validEmail)).thenReturn(None)
      when(f.mockPersonDao.create(any())).thenReturn(Left(new Exception()))
      val body: JsValue = Json.toJson(registrationContent)
      val result: Future[Result] = f.controller.register().apply(FakeRequest().withBody(body))

      // verify
      verify(f.mockSessionManager).hash(password)
      verify(f.mockPersonDao, never()).update(testPerson)
      verify(f.mockPersonDao).create(testPerson)
      status(result) mustBe 400
      contentAsJson(result) mustBe Json.obj("body" -> Json.obj("message" -> "Could not create user."))
    }
    "create a new user" in {
      // mocks
      val testPerson = Person(None, name,role, Credentials(validEmail,password,CredentialStatus.Inactive),bossEmail, false)
      val sessionToken: SessionToken = SessionToken(validEmail, password)
      val f = fixture
      when(f.mockSessionManager.hash(password)).thenReturn(password)
      when(f.mockCredentialsDao.findStatusByEmail(validEmail)).thenReturn(None)
      when(f.mockPersonDao.create(any())).thenReturn(Right(testPerson))
      when(f.mockActivationDao.createActivationToken(validEmail)).thenReturn(Some(sessionToken))
      val body: JsValue = Json.toJson(registrationContent)
      val result: Future[Result] = f.controller.register().apply(FakeRequest().withBody(body))

      // verify
      verify(f.mockSessionManager).hash(password)
      verify(f.mockPersonDao, never()).update(testPerson)
      verify(f.mockPersonDao).create(testPerson)
      verify(f.mockEmailer).sendActivationEmail(testPerson.name, sessionToken)
      status(result) mustBe 200
      contentAsJson(result) mustBe Json.toJson(testPerson)
    }
    "create a new user for an existing nominee" in {
      // mocks
      val testPerson = Person(Some(1), name,role, Credentials(validEmail,password,CredentialStatus.Inactive),bossEmail, false)
      val sessionToken: SessionToken = SessionToken(validEmail, password)
      val f = fixture
      when(f.mockSessionManager.hash(password)).thenReturn(password)
      when(f.mockCredentialsDao.findStatusByEmail(validEmail)).thenReturn(Some(1L, CredentialStatus.Nominated))
      when(f.mockPersonDao.update(any())).thenReturn(Right(testPerson))
      when(f.mockActivationDao.createActivationToken(validEmail)).thenReturn(Some(sessionToken))
      val body: JsValue = Json.toJson(registrationContent)
      val result: Future[Result] = f.controller.register().apply(FakeRequest().withBody(body))

      // verify
      verify(f.mockSessionManager).hash(password)
      verify(f.mockPersonDao, never()).create(testPerson)
      verify(f.mockPersonDao).update(testPerson)
      verify(f.mockEmailer).sendActivationEmail(testPerson.name, sessionToken)
      status(result) mustBe 200
      contentAsJson(result) mustBe Json.toJson(testPerson)
    }
  }
}
