package au.com.feedbacker.controllers

import au.com.feedbacker.AllFixtures
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
import org.scalatest.prop.PropertyChecks

/**
  * Created by lachlang on 16/02/2017.
  */
class RegistrationSpec extends PlaySpec with MockitoSugar with AllFixtures with PropertyChecks {

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
      contentAsJson(result) mustBe Json.obj("message" -> "Your request was invalid.  Please submit all the required fields.")
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
      contentAsJson(result) mustBe Json.obj("message" -> "Your request was invalid.  Please submit all the required fields.")

    }
    "reject duplicate user names for active users" in {
      forAll() { (reg: RegistrationContent, id: Long) =>
        val f = fixture
        when(f.mockCredentialsDao.findStatusByEmail(reg.email.toLowerCase)).thenReturn(Some(id, CredentialStatus.Active))
        val body: JsValue = Json.toJson(reg)
        val result: Future[Result] = f.controller.register().apply(FakeRequest().withBody(body))

        // verify
        verify(f.mockCredentialsDao).findStatusByEmail(reg.email.toLowerCase)
        status(result) mustBe 409
        contentAsJson(result) mustBe Json.obj("message" -> "This email address is already registered.  Please try using the password reset link to update your password.")
      }
    }
    "reject duplicate user names for inactive users" in {
      forAll() { (reg: RegistrationContent, id: Long) =>
        val f = fixture
        when(f.mockCredentialsDao.findStatusByEmail(reg.email.toLowerCase)).thenReturn(Some(id, CredentialStatus.Inactive))
        val body: JsValue = Json.toJson(reg)
        val result: Future[Result] = f.controller.register().apply(FakeRequest().withBody(body))

        // verify
        verify(f.mockCredentialsDao).findStatusByEmail(reg.email.toLowerCase)
        status(result) mustBe 409
        contentAsJson(result) mustBe Json.obj("message" -> "This account is already registered but awaiting activation.  Please click the link in your activation email or click the link below to request a new activation email.")
      }
    }
    "reject same email for user and manager" in {
      forAll() { reg: RegistrationContent =>
        val f = fixture

        val body: JsValue = Json.toJson(reg.copy(managerEmail = reg.email))
        val result: Future[Result] = f.controller.register().apply(FakeRequest().withBody(body))
        // verify
        status(result) mustBe 400
        contentAsJson(result) mustEqual Json.obj("message" -> "Your email and manager's email must be different.")
      }
    }
    "return bad request when failing to send email" in {
      forAll() { reg: RegistrationContent =>
        val f = fixture
        val p = Person(None, reg.name, reg.role, Credentials(reg.email.toLowerCase, reg.password, CredentialStatus.Inactive), reg.managerEmail.toLowerCase)
        when(f.mockSessionManager.hash(reg.password)).thenReturn(reg.password)
        when(f.mockCredentialsDao.findStatusByEmail(reg.email.toLowerCase)).thenReturn(None)
        when(f.mockPersonDao.create(p)).thenReturn(Right(p))
        when(f.mockActivationDao.createActivationToken(reg.email.toLowerCase)).thenReturn(None)
        val result: Future[Result] = f.controller.register().apply(FakeRequest().withBody(Json.toJson(reg)))

        // verify
        verify(f.mockCredentialsDao).findStatusByEmail(reg.email.toLowerCase)
        verify(f.mockPersonDao, never()).update(p)
        verify(f.mockPersonDao).create(p)
        status(result) mustBe 400
        contentAsJson(result) mustBe Json.obj("body" -> Json.obj("message" -> "Could not send activation email."))
      }
    }
    "return an error when the db write fails" in {
      forAll() { reg: RegistrationContent =>
        val p = Person(None, reg.name, reg.role, Credentials(reg.email.toLowerCase, reg.password, CredentialStatus.Inactive), reg.managerEmail.toLowerCase)
        val f = fixture
        when(f.mockCredentialsDao.findStatusByEmail(reg.email.toLowerCase)).thenReturn(None)
        when(f.mockSessionManager.hash(reg.password)).thenReturn(p.credentials.hash)
        when(f.mockCredentialsDao.findStatusByEmail(reg.email.toLowerCase)).thenReturn(None)
        when(f.mockPersonDao.create(p)).thenReturn(Left(new Exception()))
        val result: Future[Result] = f.controller.register().apply(FakeRequest().withBody(Json.toJson(reg)))

        // verify
        verify(f.mockCredentialsDao).findStatusByEmail(reg.email.toLowerCase)
        verify(f.mockSessionManager).hash(reg.password)
        verify(f.mockPersonDao, never()).update(p)
        verify(f.mockPersonDao).create(p)
        status(result) mustBe 400
        contentAsJson(result) mustBe Json.obj("body" -> Json.obj("message" -> "Could not create user."))
      }
    }
    "create a new user" in {
      forAll() { (st: SessionToken, reg: RegistrationContent) =>
        // mocks
        val testPerson = Person(None, reg.name, reg.role, Credentials(reg.email.toLowerCase, reg.password, CredentialStatus.Inactive), reg.managerEmail.toLowerCase, false)
        val f = fixture
        when(f.mockSessionManager.hash(reg.password)).thenReturn(reg.password)
        when(f.mockCredentialsDao.findStatusByEmail(reg.email.toLowerCase)).thenReturn(None)
        when(f.mockPersonDao.create(any())).thenReturn(Right(testPerson))
        when(f.mockActivationDao.createActivationToken(reg.email.toLowerCase)).thenReturn(Some(st))
        val result: Future[Result] = f.controller.register().apply(FakeRequest().withBody(Json.toJson(reg)))

        // verify
        verify(f.mockSessionManager).hash(reg.password)
        verify(f.mockPersonDao, never()).update(testPerson)
        verify(f.mockPersonDao).create(testPerson)
        verify(f.mockEmailer).sendActivationEmail(testPerson.name, st)
        status(result) mustBe 200
        contentAsJson(result) mustBe Json.toJson(testPerson)
      }
    }
    "create a new user for an existing nominee" in {
      forAll() { (st: SessionToken, reg: RegistrationContent, id: Long) =>
        // mocks
        val testPerson = Person(Some(id), reg.name, reg.role, Credentials(reg.email.toLowerCase, reg.password, CredentialStatus.Inactive), reg.managerEmail.toLowerCase, false)
        val f = fixture
        when(f.mockSessionManager.hash(reg.password)).thenReturn(reg.password)
        when(f.mockCredentialsDao.findStatusByEmail(reg.email.toLowerCase)).thenReturn(Some(id, CredentialStatus.Nominated))
        when(f.mockPersonDao.update(any())).thenReturn(Right(testPerson))
        when(f.mockActivationDao.createActivationToken(reg.email.toLowerCase)).thenReturn(Some(st))
        val result: Future[Result] = f.controller.register().apply(FakeRequest().withBody(Json.toJson(reg)))

        // verify
        verify(f.mockSessionManager).hash(reg.password)
        verify(f.mockPersonDao, never()).create(testPerson)
        verify(f.mockPersonDao).update(testPerson)
        verify(f.mockEmailer).sendActivationEmail(testPerson.name, st)
        status(result) mustBe 200
        contentAsJson(result) mustBe Json.toJson(testPerson)
      }
    }
  }
}
