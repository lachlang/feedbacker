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

/**
  * Created by lachlang on 16/02/2017.
  */
class ResetPasswordSpec extends PlaySpec with MockitoSugar with Results {

  val email: String = "test@email.com"
  val password: String = "thisIsAnAwesomePassword"
  val token: String = "oneTimeToken"
  val request: ResetPasswordContent = ResetPasswordContent(password, email, token)
  val name: String = "That Guy"
  val testPerson: Person = Person(Some(1), name, "role", Credentials(email, password, CredentialStatus.Active), "boss@test.com")

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
    "should return forbidden for no request body" in {
      val f = fixture
      val result: Future[Result] = f.controller.resetPassword().apply(FakeRequest().withBody(JsNull))

      // verify
      status(result) mustBe 403
      contentAsString(result) mustBe ""
      verifyZeroInteractions(f.mockActivationDao)
    }
    "should return forbidden for an invalid request" in {
      val f = fixture
      val result: Future[Result] = f.controller.resetPassword().apply(FakeRequest().withBody(Json.obj("some" -> "thing")))

      // verify
      status(result) mustBe 403
      contentAsString(result) mustBe ""
      verifyZeroInteractions(f.mockActivationDao)
    }
    "should reject invalid tokens" in {
      val f = fixture
      val result: Future[Result] = f.controller.resetPassword().apply(FakeRequest().withBody(Json.toJson(request)))

      // verify
      status(result) mustBe 403
      contentAsString(result) mustBe ""
      verify(f.mockActivationDao).validateToken(SessionToken(email, token))
      verifyZeroInteractions(f.mockPersonDao)
    }
    "should fail for invalid users" in {
      val f = fixture
      when(f.mockActivationDao.validateToken(SessionToken(email, token))).thenReturn(true)
      when(f.mockPersonDao.findByEmail(email)).thenReturn(None)
      val result: Future[Result] = f.controller.resetPassword().apply(FakeRequest().withBody(Json.toJson(request)))

      // verify
      status(result) mustBe 400
      contentAsString(result) mustBe ""
      verify(f.mockActivationDao).validateToken(SessionToken(email, token))
      verify(f.mockPersonDao).findByEmail(email)
      verifyZeroInteractions(f.mockSessionManager)
      verifyZeroInteractions(f.mockEmailer)
    }
    "should return a bad request code when the user update fails" in {
      // mocks
      val errorMessage: String = "error message"
      val f = fixture
      when(f.mockActivationDao.validateToken(SessionToken(email, token))).thenReturn(true)
      when(f.mockPersonDao.findByEmail(email)).thenReturn(Some(testPerson))
      when(f.mockPersonDao.update(testPerson)).thenReturn(Left(new Exception(errorMessage)))
      when(f.mockSessionManager.hash(password)).thenReturn(password)
      val result: Future[Result] = f.controller.resetPassword().apply(FakeRequest().withBody(Json.toJson(request)))

      // verify
      status(result) mustBe 400
      contentAsJson(result) mustBe Json.obj("body" -> Json.obj("message" -> errorMessage))
      verify(f.mockActivationDao).validateToken(SessionToken(email, token))
      verify(f.mockPersonDao).findByEmail(email)
      verify(f.mockSessionManager).hash(password)
      verify(f.mockPersonDao).update(testPerson)
      verifyZeroInteractions(f.mockEmailer)
    }
    "should reset the users password" in {
      val f = fixture
      when(f.mockActivationDao.validateToken(SessionToken(email, token))).thenReturn(true)
      when(f.mockPersonDao.findByEmail(email)).thenReturn(Some(testPerson))
      when(f.mockPersonDao.update(testPerson)).thenReturn(Right(testPerson))
      when(f.mockSessionManager.hash(password)).thenReturn(password)
      val result: Future[Result] = f.controller.resetPassword().apply(FakeRequest().withBody(Json.toJson(request)))

      // verify
      status(result) mustBe 200
      contentAsString(result) mustBe ""
      verify(f.mockActivationDao).validateToken(SessionToken(email, token))
      verify(f.mockPersonDao).findByEmail(email)
      verify(f.mockSessionManager).hash(password)
      verify(f.mockPersonDao).update(testPerson)
      verifyZeroInteractions(f.mockEmailer)
    }
  }
  "ResetPassword#sendPasswordResetEmail" should {
    "should fail when no email provided" in {
      val f = fixture
      val result = f.controller.sendPasswordResetEmail().apply(FakeRequest())

      // verify
      status(result) mustBe 400
      contentAsString(result) mustBe ""
      verifyZeroInteractions(f.mockPersonDao)
      verifyZeroInteractions(f.mockEmailer)
    }
    "should fail when the user isn't registered" in {
      val f = fixture
      when(f.mockPersonDao.findByEmail(email)).thenReturn(None)
      val result = f.controller.sendPasswordResetEmail().apply(FakeRequest().withJsonBody(Json.obj("body" -> Json.obj("email" -> email))))

      // verify
      status(result) mustBe 400
      contentAsString(result) mustBe ""
      verify(f.mockPersonDao).findByEmail(email)
      verifyZeroInteractions(f.mockEmailer)

    }
    "should fail when cannot create token" in {
      val f = fixture
      when(f.mockPersonDao.findByEmail(email)).thenReturn(Some(testPerson))
      when(f.mockActivationDao.createActivationToken(email)).thenReturn(None)
      val result = f.controller.sendPasswordResetEmail().apply(FakeRequest().withJsonBody(Json.obj("body" -> Json.obj("email" -> email))))

      // verify
      status(result) mustBe 400
      contentAsString(result) mustBe ""
      verify(f.mockPersonDao).findByEmail(email)
      verify(f.mockActivationDao).createActivationToken(email)
      verifyZeroInteractions(f.mockEmailer)
      verifyZeroInteractions(f.mockSessionManager)
    }
    "should send a password reset email" in {
      val f = fixture
      when(f.mockPersonDao.findByEmail(email)).thenReturn(Some(testPerson))
      when(f.mockActivationDao.createActivationToken(email)).thenReturn(Some(SessionToken(email, token)))
      val result = f.controller.sendPasswordResetEmail().apply(FakeRequest().withJsonBody(Json.obj("body" -> Json.obj("email" -> email))))

      // verify
      status(result) mustBe 200
      contentAsString(result) mustBe ""
      verify(f.mockPersonDao).findByEmail(email)
      verify(f.mockActivationDao).createActivationToken(email)
      verify(f.mockEmailer).sendPasswordResetEmail(name, SessionToken(email, token))
      verifyZeroInteractions(f.mockSessionManager)
    }
  }
}
