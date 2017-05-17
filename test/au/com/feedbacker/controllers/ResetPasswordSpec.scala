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
import org.mockito.Matchers.any
import org.scalatest.prop.PropertyChecks

/**
  * Created by lachlang on 16/02/2017.
  */
class ResetPasswordSpec extends PlaySpec with MockitoSugar with AllFixtures with PropertyChecks {

  def fixture = {
    new {
      val mockEmailer = mock[Emailer]
      val mockPersonDao = mock[PersonDao]
      val mockActivationDao = mock[ActivationDao]
      val mockSessionManager = mock[SessionManager]
      val controller = new ResetPassword(mockEmailer, mockPersonDao, mockActivationDao, mockSessionManager)
    }
  }
  "ResetPassword#resetPassword" should {
    "return forbidden for no request body" in {
      val f = fixture
      val result: Future[Result] = f.controller.resetPassword().apply(FakeRequest().withBody(JsNull))

      // verify
      status(result) mustBe 400
      contentAsString(result) mustBe ""
      verifyZeroInteractions(f.mockActivationDao)
    }
    "return forbidden for an invalid request" in {
      val f = fixture
      val result: Future[Result] = f.controller.resetPassword().apply(FakeRequest().withBody(Json.obj("some" -> "thing")))

      // verify
      status(result) mustBe 400
      contentAsString(result) mustBe ""
      verifyZeroInteractions(f.mockActivationDao)
    }
    "return forbidden for invalid tokens" in {
      forAll() { request:ResetPasswordContent =>
        val f = fixture

        val result: Future[Result] = f.controller.resetPassword().apply(FakeRequest().withBody(Json.toJson(request)))

        // verify
        status(result) mustBe 403
        contentAsString(result) mustBe ""
        verify(f.mockActivationDao).validateToken(SessionToken(request.username.toLowerCase, request.token.replaceAll(" ", "+")))
        verifyZeroInteractions(f.mockPersonDao)
      }
    }
    "fail for unregistered users" in {
      forAll() { request: ResetPasswordContent =>
        val f = fixture
        when(f.mockActivationDao.validateToken(SessionToken(request.username.toLowerCase, request.token.replaceAll(" ", "+")))).thenReturn(true)
        when(f.mockPersonDao.findByEmail(request.username.toLowerCase)).thenReturn(None)
        val result: Future[Result] = f.controller.resetPassword().apply(FakeRequest().withBody(Json.toJson(request)))

        // verify
        status(result) mustBe 400
        contentAsString(result) mustBe ""
        verify(f.mockActivationDao).validateToken(SessionToken(request.username.toLowerCase, request.token.replaceAll(" ", "+")))
        verify(f.mockPersonDao).findByEmail(request.username.toLowerCase)
        verifyZeroInteractions(f.mockSessionManager)
        verifyZeroInteractions(f.mockEmailer)
      }
    }
    "return forbidden for users who are not active" in {
      forAll() { (user: Person, request: ResetPasswordContent) =>
        whenever(user.credentials.status != CredentialStatus.Active) {
          val f = fixture
          when(f.mockActivationDao.validateToken(SessionToken(request.username.toLowerCase, request.token.replaceAll(" ", "+")))).thenReturn(true)
          when(f.mockPersonDao.findByEmail(request.username.toLowerCase)).thenReturn(Some(user))
          val result: Future[Result] = f.controller.resetPassword().apply(FakeRequest().withBody(Json.toJson(request)))

          // verify
          status(result) mustBe 403
          contentAsString(result) mustBe ""
          verify(f.mockActivationDao).validateToken(SessionToken(request.username.toLowerCase, request.token.replaceAll(" ", "+")))
          verify(f.mockPersonDao).findByEmail(request.username.toLowerCase)
          verifyZeroInteractions(f.mockSessionManager)
          verifyZeroInteractions(f.mockEmailer)
        }
      }
    }
    "return a bad request code when the user update fails" in {
      forAll() { (request: ResetPasswordContent, p: Person) =>
        // mocks

        val person = p.copy(credentials = p.credentials.copy(status = CredentialStatus.Active))
        val errorMessage: String = "error message"
        val f = fixture
        when(f.mockActivationDao.validateToken(SessionToken(request.username.toLowerCase, request.token.replaceAll(" ", "+")))).thenReturn(true)
        when(f.mockPersonDao.findByEmail(request.username.toLowerCase)).thenReturn(Some(person))
        when(f.mockPersonDao.update(person)).thenReturn(Left(new Exception(errorMessage)))
        when(f.mockSessionManager.hash(request.password)).thenReturn(person.credentials.hash)
        val result: Future[Result] = f.controller.resetPassword().apply(FakeRequest().withBody(Json.toJson(request)))

        // verify
        status(result) mustBe 400
        contentAsJson(result) mustBe Json.obj("body" -> Json.obj("message" -> errorMessage))
        verify(f.mockActivationDao).validateToken(SessionToken(request.username.toLowerCase, request.token.replaceAll(" ", "+")))
        verify(f.mockPersonDao).findByEmail(request.username.toLowerCase)
        verify(f.mockSessionManager).hash(request.password)
        verify(f.mockPersonDao).update(person)
        verifyZeroInteractions(f.mockEmailer)
      }
    }
    "reset the users password" in {
      forAll() { (request: ResetPasswordContent, p: Person) =>
        val f = fixture
        val person = p.copy(credentials = p.credentials.copy(status = CredentialStatus.Active))
        when(f.mockActivationDao.validateToken(SessionToken(request.username.toLowerCase, request.token.replaceAll(" ", "+")))).thenReturn(true)
        when(f.mockPersonDao.findByEmail(request.username.toLowerCase)).thenReturn(Some(person))
        when(f.mockPersonDao.update(person)).thenReturn(Right(person))
        when(f.mockSessionManager.hash(request.password)).thenReturn(person.credentials.hash)
        val result: Future[Result] = f.controller.resetPassword().apply(FakeRequest().withBody(Json.toJson(request)))

        // verify
        status(result) mustBe 200
        contentAsString(result) mustBe ""
        verify(f.mockActivationDao).validateToken(SessionToken(request.username.toLowerCase, request.token.replaceAll(" ", "+")))
        verify(f.mockPersonDao).findByEmail(request.username.toLowerCase)
        verify(f.mockSessionManager).hash(request.password)
        verify(f.mockPersonDao).update(person)
        verifyZeroInteractions(f.mockEmailer)
      }
    }
  }
  "ResetPassword#sendPasswordResetEmail" should {
    "fail when no email provided" in {
      val f = fixture
      val result = f.controller.sendPasswordResetEmail().apply(FakeRequest())

      // verify
      status(result) mustBe 400
      contentAsString(result) mustBe ""
      verifyZeroInteractions(f.mockPersonDao)
      verifyZeroInteractions(f.mockEmailer)
    }
    "fail when the user isn't registered" in {
      forAll(validEmailAddresses()) { email =>
        val f = fixture
        when(f.mockPersonDao.findByEmail(email)).thenReturn(None)
        val result = f.controller.sendPasswordResetEmail().apply(FakeRequest().withJsonBody(Json.obj("body" -> Json.obj("email" -> email))))

        // verify
        status(result) mustBe 400
        contentAsString(result) mustBe ""
        verify(f.mockPersonDao).findByEmail(email.toLowerCase)
        verifyZeroInteractions(f.mockEmailer)
      }
    }
    "fail when cannot create token" in {
      forAll() { person: Person =>
        val f = fixture
        when(f.mockPersonDao.findByEmail(person.credentials.email.toLowerCase)).thenReturn(Some(person))
        when(f.mockActivationDao.createActivationToken(person.credentials.email.toLowerCase)).thenReturn(None)
        val result = f.controller.sendPasswordResetEmail().apply(FakeRequest().withJsonBody(Json.obj("body" -> Json.obj("email" -> person.credentials.email))))

        // verify
        status(result) mustBe 400
        contentAsString(result) mustBe ""
        verify(f.mockPersonDao).findByEmail(person.credentials.email.toLowerCase)
        verify(f.mockActivationDao).createActivationToken(person.credentials.email.toLowerCase)
        verifyZeroInteractions(f.mockEmailer)
        verifyZeroInteractions(f.mockSessionManager)
      }
    }
    "return forbidden user is not active" in {
      forAll() { (person: Person, token: String) =>
        whenever(person.credentials.status != CredentialStatus.Active) {
          val f = fixture
          when(f.mockPersonDao.findByEmail(person.credentials.email.toLowerCase)).thenReturn(Some(person))
          when(f.mockActivationDao.createActivationToken(person.credentials.email.toLowerCase)).thenReturn(Some(SessionToken(person.credentials.email, token)))
          val result = f.controller.sendPasswordResetEmail().apply(FakeRequest().withJsonBody(Json.obj("body" -> Json.obj("email" -> person.credentials.email))))

          // verify
          status(result) mustBe 403
          contentAsString(result) mustBe ""
          verify(f.mockPersonDao).findByEmail(person.credentials.email.toLowerCase)
          verify(f.mockActivationDao).createActivationToken(person.credentials.email.toLowerCase)
          verifyZeroInteractions(f.mockEmailer)
          verifyZeroInteractions(f.mockSessionManager)
        }
      }
    }
    "send a password reset email" in {
      forAll() { (p: Person, token: String) =>
        val person = p.copy(credentials = p.credentials.copy(status = CredentialStatus.Active))
        val f = fixture
        when(f.mockPersonDao.findByEmail(person.credentials.email.toLowerCase)).thenReturn(Some(person))
        when(f.mockActivationDao.createActivationToken(person.credentials.email.toLowerCase)).thenReturn(Some(SessionToken(person.credentials.email, token)))
        val result = f.controller.sendPasswordResetEmail().apply(FakeRequest().withJsonBody(Json.obj("body" -> Json.obj("email" -> person.credentials.email))))

        // verify
        status(result) mustBe 200
        contentAsString(result) mustBe ""
        verify(f.mockPersonDao).findByEmail(person.credentials.email.toLowerCase)
        verify(f.mockActivationDao).createActivationToken(person.credentials.email.toLowerCase)
        verify(f.mockEmailer).sendPasswordResetEmail(person.name, SessionToken(person.credentials.email, token))
        verifyZeroInteractions(f.mockSessionManager)
      }
    }
  }
}
