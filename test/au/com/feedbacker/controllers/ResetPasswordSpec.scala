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
import org.scalatest.prop.PropertyChecks

/**
  * Created by lachlang on 16/02/2017.
  */
class ResetPasswordSpec extends PlaySpec with MockitoSugar with Results with AllFixtures with PropertyChecks {

  val staticEmail: String = "test@email.com"
  val staticPassword: String = "thisIsAnAwesomePassword"
  val staticToken: String = "oneTimeToken"
  val staticRequest: ResetPasswordContent = ResetPasswordContent(staticPassword, staticEmail, staticToken)
  val name: String = "That Guy"
  val testPerson: Person = Person(Some(1), name, "role", Credentials(staticEmail, staticPassword, CredentialStatus.Active), "boss@test.com")

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
        verify(f.mockActivationDao).validateToken(SessionToken(request.username, request.token))
        verifyZeroInteractions(f.mockPersonDao)
      }
    }
    "fail for invalid users" in {
      val f = fixture
      when(f.mockActivationDao.validateToken(SessionToken(staticEmail, staticToken))).thenReturn(true)
      when(f.mockPersonDao.findByEmail(staticEmail)).thenReturn(None)
      val result: Future[Result] = f.controller.resetPassword().apply(FakeRequest().withBody(Json.toJson(staticRequest)))

      // verify
      status(result) mustBe 400
      contentAsString(result) mustBe ""
      verify(f.mockActivationDao).validateToken(SessionToken(staticEmail, staticToken))
      verify(f.mockPersonDao).findByEmail(staticEmail)
      verifyZeroInteractions(f.mockSessionManager)
      verifyZeroInteractions(f.mockEmailer)
    }
    "return a bad request code when the user update fails" in {
      // mocks
      val errorMessage: String = "error message"
      val f = fixture
      when(f.mockActivationDao.validateToken(SessionToken(staticEmail, staticToken))).thenReturn(true)
      when(f.mockPersonDao.findByEmail(staticEmail)).thenReturn(Some(testPerson))
      when(f.mockPersonDao.update(testPerson)).thenReturn(Left(new Exception(errorMessage)))
      when(f.mockSessionManager.hash(staticPassword)).thenReturn(staticPassword)
      val result: Future[Result] = f.controller.resetPassword().apply(FakeRequest().withBody(Json.toJson(staticRequest)))

      // verify
      status(result) mustBe 400
      contentAsJson(result) mustBe Json.obj("body" -> Json.obj("message" -> errorMessage))
      verify(f.mockActivationDao).validateToken(SessionToken(staticEmail, staticToken))
      verify(f.mockPersonDao).findByEmail(staticEmail)
      verify(f.mockSessionManager).hash(staticPassword)
      verify(f.mockPersonDao).update(testPerson)
      verifyZeroInteractions(f.mockEmailer)
    }
    "reset the users password" in {
      val f = fixture
      when(f.mockActivationDao.validateToken(SessionToken(staticEmail, staticToken))).thenReturn(true)
      when(f.mockPersonDao.findByEmail(staticEmail)).thenReturn(Some(testPerson))
      when(f.mockPersonDao.update(testPerson)).thenReturn(Right(testPerson))
      when(f.mockSessionManager.hash(staticPassword)).thenReturn(staticPassword)
      val result: Future[Result] = f.controller.resetPassword().apply(FakeRequest().withBody(Json.toJson(staticRequest)))

      // verify
      status(result) mustBe 200
      contentAsString(result) mustBe ""
      verify(f.mockActivationDao).validateToken(SessionToken(staticEmail, staticToken))
      verify(f.mockPersonDao).findByEmail(staticEmail)
      verify(f.mockSessionManager).hash(staticPassword)
      verify(f.mockPersonDao).update(testPerson)
      verifyZeroInteractions(f.mockEmailer)
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
      val f = fixture
      when(f.mockPersonDao.findByEmail(staticEmail)).thenReturn(None)
      val result = f.controller.sendPasswordResetEmail().apply(FakeRequest().withJsonBody(Json.obj("body" -> Json.obj("email" -> staticEmail))))

      // verify
      status(result) mustBe 400
      contentAsString(result) mustBe ""
      verify(f.mockPersonDao).findByEmail(staticEmail)
      verifyZeroInteractions(f.mockEmailer)

    }
    "fail when cannot create token" in {
      val f = fixture
      when(f.mockPersonDao.findByEmail(staticEmail)).thenReturn(Some(testPerson))
      when(f.mockActivationDao.createActivationToken(staticEmail)).thenReturn(None)
      val result = f.controller.sendPasswordResetEmail().apply(FakeRequest().withJsonBody(Json.obj("body" -> Json.obj("email" -> staticEmail))))

      // verify
      status(result) mustBe 400
      contentAsString(result) mustBe ""
      verify(f.mockPersonDao).findByEmail(staticEmail)
      verify(f.mockActivationDao).createActivationToken(staticEmail)
      verifyZeroInteractions(f.mockEmailer)
      verifyZeroInteractions(f.mockSessionManager)
    }
    "send a password reset email" in {
      val f = fixture
      when(f.mockPersonDao.findByEmail(staticEmail)).thenReturn(Some(testPerson))
      when(f.mockActivationDao.createActivationToken(staticEmail)).thenReturn(Some(SessionToken(staticEmail, staticToken)))
      val result = f.controller.sendPasswordResetEmail().apply(FakeRequest().withJsonBody(Json.obj("body" -> Json.obj("email" -> staticEmail))))

      // verify
      status(result) mustBe 200
      contentAsString(result) mustBe ""
      verify(f.mockPersonDao).findByEmail(staticEmail)
      verify(f.mockActivationDao).createActivationToken(staticEmail)
      verify(f.mockEmailer).sendPasswordResetEmail(name, SessionToken(staticEmail, staticToken))
      verifyZeroInteractions(f.mockSessionManager)
    }
  }
}
