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

  "Registration#register" should {
    "should reject empty requests" in {
      // mock
      val mockEmailer = mock[Emailer]
      val mockPersonDao = mock[PersonDao]
      val mockCredentialsDao = mock[CredentialsDao]
      val mockActivationDao = mock[ActivationDao]
      val mockSessionManager = mock[SessionManager]

      // call
      val body: JsValue = JsNull
      val controller = new Registration(mockEmailer, mockPersonDao, mockCredentialsDao, mockActivationDao, mockSessionManager)
      val result: Future[Result] = controller.register().apply(FakeRequest().withBody(body))

      // verify
      status(result) mustBe 400
      contentAsJson(result) mustBe Json.obj("body" -> Json.obj("message" -> "Could not parse request."))
      verifyZeroInteractions(mockEmailer)
      verifyZeroInteractions(mockPersonDao)
      verifyZeroInteractions(mockCredentialsDao)
      verifyZeroInteractions(mockActivationDao)
    }
    "should reject invalid payloads" in {
      // mocks
      val mockEmailer = mock[Emailer]
      val mockPersonDao = mock[PersonDao]
      val mockCredentialsDao = mock[CredentialsDao]
      val mockActivationDao = mock[ActivationDao]
      val mockSessionManager = mock[SessionManager]

      // call
      val body: JsValue = Json.obj("some" -> "object")
      val controller = new Registration(mockEmailer, mockPersonDao, mockCredentialsDao, mockActivationDao, mockSessionManager)
      val result: Future[Result] = controller.register().apply(FakeRequest().withBody(body))

      // verify
      status(result) mustBe 400
      contentAsJson(result) mustEqual Json.obj("body" -> Json.obj("message" -> "Could not parse request."))

    }
    "should reject duplicate user names" in {
      // mock
      val mockEmailer = mock[Emailer]
      val mockPersonDao = mock[PersonDao]
      val mockCredentialsDao = mock[CredentialsDao]
      val mockActivationDao = mock[ActivationDao]
      val mockSessionManager = mock[SessionManager]
      when(mockCredentialsDao.findStatusByEmail(validEmail)).thenReturn(Some(1L, CredentialStatus.Active))

      // call
      val body: JsValue = Json.toJson(registrationContent)
      val controller = new Registration(mockEmailer, mockPersonDao, mockCredentialsDao, mockActivationDao, mockSessionManager)
      val result: Future[Result] = controller.register().apply(FakeRequest().withBody(body))

      // verify
      verify(mockCredentialsDao).findStatusByEmail(validEmail)
      status(result) mustBe 409
      contentAsJson(result) mustBe Json.obj("body" -> Json.obj("message" -> "User is already registered."))
    }
    "should return bad request when failing to send email" in {
      // mocks
      val testPerson = Person(Some(1), name,role, Credentials(validEmail,password,CredentialStatus.Inactive),bossEmail, false)
      val mockEmailer = mock[Emailer]
      val mockPersonDao = mock[PersonDao]
      val mockCredentialsDao = mock[CredentialsDao]
      val mockActivationDao = mock[ActivationDao]
      val mockSessionManager = mock[SessionManager]
      when(mockSessionManager.hash(password)).thenReturn(password)
      when(mockCredentialsDao.findStatusByEmail(validEmail)).thenReturn(None)
      when(mockPersonDao.create(any())).thenReturn(Right(testPerson))
      when(mockActivationDao.createActivationToken(validEmail)).thenReturn(None)

      // call
      val body: JsValue = Json.toJson(registrationContent)
      val controller = new Registration(mockEmailer, mockPersonDao, mockCredentialsDao, mockActivationDao, mockSessionManager)
      val result: Future[Result] = controller.register().apply(FakeRequest().withBody(body))

      // verify
      status(result) mustBe 400
      contentAsJson(result) mustBe Json.obj("body" -> Json.obj("message" -> "Could not send activation email."))
    }
    "should return an error when the db write fails" in {
      // mocks
      val testPerson = Person(None, name,role, Credentials(validEmail,password,CredentialStatus.Inactive),bossEmail, false)
      val mockEmailer = mock[Emailer]
      val mockPersonDao = mock[PersonDao]
      val mockCredentialsDao = mock[CredentialsDao]
      val mockActivationDao = mock[ActivationDao]
      val mockSessionManager = mock[SessionManager]
      when(mockSessionManager.hash(password)).thenReturn(password)
      when(mockCredentialsDao.findStatusByEmail(validEmail)).thenReturn(None)
      when(mockPersonDao.create(any())).thenReturn(Left(new Exception()))

      // call
      val body: JsValue = Json.toJson(registrationContent)
      val controller = new Registration(mockEmailer, mockPersonDao, mockCredentialsDao, mockActivationDao, mockSessionManager)
      val result: Future[Result] = controller.register().apply(FakeRequest().withBody(body))

      // verify
      verify(mockSessionManager).hash(password)
      verify(mockPersonDao, never()).update(testPerson)
      verify(mockPersonDao).create(testPerson)
      status(result) mustBe 400
      contentAsJson(result) mustBe Json.obj("body" -> Json.obj("message" -> "Could not create user."))
    }
    "should create a new user" in {
      // mocks
      val testPerson = Person(None, name,role, Credentials(validEmail,password,CredentialStatus.Inactive),bossEmail, false)
      val sessionToken: SessionToken = SessionToken(validEmail, password)
      val mockEmailer = mock[Emailer]
      val mockPersonDao = mock[PersonDao]
      val mockCredentialsDao = mock[CredentialsDao]
      val mockActivationDao = mock[ActivationDao]
      val mockSessionManager = mock[SessionManager]
      when(mockSessionManager.hash(password)).thenReturn(password)
      when(mockCredentialsDao.findStatusByEmail(validEmail)).thenReturn(None)
      when(mockPersonDao.create(any())).thenReturn(Right(testPerson))
      when(mockActivationDao.createActivationToken(validEmail)).thenReturn(Some(sessionToken))

      // call
      val body: JsValue = Json.toJson(registrationContent)
      val controller = new Registration(mockEmailer, mockPersonDao, mockCredentialsDao, mockActivationDao, mockSessionManager)
      val result: Future[Result] = controller.register().apply(FakeRequest().withBody(body))

      // verify
      verify(mockSessionManager).hash(password)
      verify(mockPersonDao, never()).update(testPerson)
      verify(mockPersonDao).create(testPerson)
      verify(mockEmailer).sendActivationEmail(testPerson.name, sessionToken)
      status(result) mustBe 200
      contentAsJson(result) mustBe Json.toJson(testPerson)
    }
    "should create a new user for an existing nominee" in {
      // mocks
      val testPerson = Person(Some(1), name,role, Credentials(validEmail,password,CredentialStatus.Inactive),bossEmail, false)
      val sessionToken: SessionToken = SessionToken(validEmail, password)
      val mockEmailer = mock[Emailer]
      val mockPersonDao = mock[PersonDao]
      val mockCredentialsDao = mock[CredentialsDao]
      val mockActivationDao = mock[ActivationDao]
      val mockSessionManager = mock[SessionManager]
      when(mockSessionManager.hash(password)).thenReturn(password)
      when(mockCredentialsDao.findStatusByEmail(validEmail)).thenReturn(Some(1L, CredentialStatus.Nominated))
      when(mockPersonDao.update(any())).thenReturn(Right(testPerson))
      when(mockActivationDao.createActivationToken(validEmail)).thenReturn(Some(sessionToken))

      // call
      val body: JsValue = Json.toJson(registrationContent)
      val controller = new Registration(mockEmailer, mockPersonDao, mockCredentialsDao, mockActivationDao, mockSessionManager)
      val result: Future[Result] = controller.register().apply(FakeRequest().withBody(body))

      // verify
      verify(mockSessionManager).hash(password)
      verify(mockPersonDao, never()).create(testPerson)
      verify(mockPersonDao).update(testPerson)
      verify(mockEmailer).sendActivationEmail(testPerson.name, sessionToken)
      status(result) mustBe 200
      contentAsJson(result) mustBe Json.toJson(testPerson)
    }
  }
}
