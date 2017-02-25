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

  "ResetPassword#resetPassword" should {
    "should return forbidden for no request body" in {
      // mocks
      val mockEmailer = mock[Emailer]
      val mockPersonDao = mock[PersonDao]
      val mockActivationDao = mock[ActivationDao]
      val mockSessionManager = mock[SessionManager]

      // call
      val controller = new ResetPassword(mockEmailer, mockPersonDao, mockActivationDao, mockSessionManager)
      val result: Future[Result] = controller.resetPassword().apply(FakeRequest().withBody(JsNull))

      // verify
      status(result) mustBe 403
      contentAsString(result) mustBe ""
      verifyZeroInteractions(mockActivationDao)
    }
    "should return forbidden for an invalid request" in {
      // mocks
      val mockEmailer = mock[Emailer]
      val mockPersonDao = mock[PersonDao]
      val mockActivationDao = mock[ActivationDao]
      val mockSessionManager = mock[SessionManager]

      // call
      val controller = new ResetPassword(mockEmailer, mockPersonDao, mockActivationDao, mockSessionManager)
      val result: Future[Result] = controller.resetPassword().apply(FakeRequest().withBody(Json.obj("some" -> "thing")))

      // verify
      status(result) mustBe 403
      contentAsString(result) mustBe ""
      verifyZeroInteractions(mockActivationDao)
    }
    "should reject invalid tokens" in {
      // mocks
      val mockEmailer = mock[Emailer]
      val mockPersonDao = mock[PersonDao]
      val mockActivationDao = mock[ActivationDao]
      val mockSessionManager = mock[SessionManager]
      when(mockActivationDao.validateToken(SessionToken(email, token))).thenReturn(false)

      // call
      val controller = new ResetPassword(mockEmailer, mockPersonDao, mockActivationDao, mockSessionManager)
      val result: Future[Result] = controller.resetPassword().apply(FakeRequest().withBody(Json.toJson(request)))

      // verify
      status(result) mustBe 403
      contentAsString(result) mustBe ""
      verify(mockActivationDao).validateToken(SessionToken(email, token))
      verifyZeroInteractions(mockPersonDao)
    }
    "should fail for invalid users" in {
      // mocks
      val mockEmailer = mock[Emailer]
      val mockPersonDao = mock[PersonDao]
      val mockActivationDao = mock[ActivationDao]
      val mockSessionManager = mock[SessionManager]
      when(mockActivationDao.validateToken(SessionToken(email, token))).thenReturn(true)
      when(mockPersonDao.findByEmail(email)).thenReturn(None)

      // call
      val controller = new ResetPassword(mockEmailer, mockPersonDao, mockActivationDao, mockSessionManager)
      val result: Future[Result] = controller.resetPassword().apply(FakeRequest().withBody(Json.toJson(request)))

      // verify
      status(result) mustBe 400
      contentAsString(result) mustBe ""
      verify(mockActivationDao).validateToken(SessionToken(email, token))
      verify(mockPersonDao).findByEmail(email)
      verifyZeroInteractions(mockSessionManager)
      verifyZeroInteractions(mockEmailer)
    }
    "should return a bad request code when the user update fails" in {
      // mocks
      val errorMessage: String = "error message"
      val mockEmailer = mock[Emailer]
      val mockPersonDao = mock[PersonDao]
      val mockActivationDao = mock[ActivationDao]
      val mockSessionManager = mock[SessionManager]
      when(mockActivationDao.validateToken(SessionToken(email, token))).thenReturn(true)
      when(mockPersonDao.findByEmail(email)).thenReturn(Some(testPerson))
      when(mockPersonDao.update(testPerson)).thenReturn(Left(new Exception(errorMessage)))
      when(mockSessionManager.hash(password)).thenReturn(password)

      // call
      val controller = new ResetPassword(mockEmailer, mockPersonDao, mockActivationDao, mockSessionManager)
      val result: Future[Result] = controller.resetPassword().apply(FakeRequest().withBody(Json.toJson(request)))

      // verify
      status(result) mustBe 400
      contentAsJson(result) mustBe Json.obj("body" -> Json.obj("message" -> errorMessage))
      verify(mockActivationDao).validateToken(SessionToken(email, token))
      verify(mockPersonDao).findByEmail(email)
      verify(mockSessionManager).hash(password)
      verify(mockPersonDao).update(testPerson)
      verifyZeroInteractions(mockEmailer)
    }
    "should reset the users password" in {
      // mocks
      val mockEmailer = mock[Emailer]
      val mockPersonDao = mock[PersonDao]
      val mockActivationDao = mock[ActivationDao]
      val mockSessionManager = mock[SessionManager]
      when(mockActivationDao.validateToken(SessionToken(email, token))).thenReturn(true)
      when(mockPersonDao.findByEmail(email)).thenReturn(Some(testPerson))
      when(mockPersonDao.update(testPerson)).thenReturn(Right(testPerson))
      when(mockSessionManager.hash(password)).thenReturn(password)

      // call
      val controller = new ResetPassword(mockEmailer, mockPersonDao, mockActivationDao, mockSessionManager)
      val result: Future[Result] = controller.resetPassword().apply(FakeRequest().withBody(Json.toJson(request)))

      // verify
      status(result) mustBe 200
      contentAsString(result) mustBe ""
      verify(mockActivationDao).validateToken(SessionToken(email, token))
      verify(mockPersonDao).findByEmail(email)
      verify(mockSessionManager).hash(password)
      verify(mockPersonDao).update(testPerson)
      verifyZeroInteractions(mockEmailer)
    }
  }
  "ResetPassword#sendPasswordResetEmail" should {
    "should fail when no email provided" in {
      // mocks
      val mockEmailer = mock[Emailer]
      val mockPersonDao = mock[PersonDao]
      val mockActivationDao = mock[ActivationDao]
      val mockSessionManager = mock[SessionManager]

      // call
      val controller = new ResetPassword(mockEmailer, mockPersonDao, mockActivationDao, mockSessionManager)
      val result = controller.sendPasswordResetEmail().apply(FakeRequest())

      // verify
      status(result) mustBe 400
      contentAsString(result) mustBe ""
      verifyZeroInteractions(mockPersonDao)
      verifyZeroInteractions(mockEmailer)
    }
    "should fail when the user isn't registered" in {
      // mocks
      val mockEmailer = mock[Emailer]
      val mockPersonDao = mock[PersonDao]
      val mockActivationDao = mock[ActivationDao]
      val mockSessionManager = mock[SessionManager]
      when(mockPersonDao.findByEmail(email)).thenReturn(None)

      // call
      val controller = new ResetPassword(mockEmailer, mockPersonDao, mockActivationDao, mockSessionManager)
      val result = controller.sendPasswordResetEmail().apply(FakeRequest().withJsonBody(Json.obj("body" -> Json.obj("email" -> email))))

      // verify
      status(result) mustBe 400
      contentAsString(result) mustBe ""
      verify(mockPersonDao).findByEmail(email)
      verifyZeroInteractions(mockEmailer)

    }
    "should fail when cannot create token" in {
      // mocks
      val mockEmailer = mock[Emailer]
      val mockPersonDao = mock[PersonDao]
      val mockActivationDao = mock[ActivationDao]
      val mockSessionManager = mock[SessionManager]
      when(mockPersonDao.findByEmail(email)).thenReturn(Some(testPerson))
      when(mockActivationDao.createActivationToken(email)).thenReturn(None)

      // call
      val controller = new ResetPassword(mockEmailer, mockPersonDao, mockActivationDao, mockSessionManager)
      val result = controller.sendPasswordResetEmail().apply(FakeRequest().withJsonBody(Json.obj("body" -> Json.obj("email" -> email))))

      // verify
      status(result) mustBe 400
      contentAsString(result) mustBe ""
      verify(mockPersonDao).findByEmail(email)
      verify(mockActivationDao).createActivationToken(email)
      verifyZeroInteractions(mockEmailer)
      verifyZeroInteractions(mockSessionManager)
    }
    "should send a password reset email" in {
      // mocks
      val mockEmailer = mock[Emailer]
      val mockPersonDao = mock[PersonDao]
      val mockActivationDao = mock[ActivationDao]
      val mockSessionManager = mock[SessionManager]
      when(mockPersonDao.findByEmail(email)).thenReturn(Some(testPerson))
      when(mockActivationDao.createActivationToken(email)).thenReturn(Some(SessionToken(email, token)))

      // call
      val controller = new ResetPassword(mockEmailer, mockPersonDao, mockActivationDao, mockSessionManager)
      val result = controller.sendPasswordResetEmail().apply(FakeRequest().withJsonBody(Json.obj("body" -> Json.obj("email" -> email))))

      // verify
      status(result) mustBe 200
      contentAsString(result) mustBe ""
      verify(mockPersonDao).findByEmail(email)
      verify(mockActivationDao).createActivationToken(email)
      verify(mockEmailer).sendPasswordResetEmail(name, SessionToken(email, token))
      verifyZeroInteractions(mockSessionManager)
    }
  }
}
