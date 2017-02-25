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
class ActivationCtrlSpec extends PlaySpec with MockitoSugar with Results {

  val validEmail: String = "valid@test.com"
  val validToken: String = "valid_token"
  val validSessionToken = SessionToken(validEmail, validToken)

  "ActivationCtrl#activate" should {
    "should return an error for an empty request" in {
      // mocks
      val mockEmailer = mock[Emailer]
      val mockPersonDao = mock[PersonDao]
      val mockActivationDao = mock[ActivationDao]
      val mockSessionManager = mock[SessionManager]

      // call
      val controller = new ActivationCtrl(mockEmailer, mockPersonDao, mockActivationDao, mockSessionManager)
      val result: Future[Result] = controller.activate().apply(FakeRequest())

      // verify
      status(result) mustBe 400
      contentAsString(result) mustBe ""
      verifyZeroInteractions(mockPersonDao)
    }
    "should return an error for an invalid request" in {
      // mocks
      val mockEmailer = mock[Emailer]
      val mockPersonDao = mock[PersonDao]
      val mockActivationDao = mock[ActivationDao]
      val mockSessionManager = mock[SessionManager]

      // call
      val controller = new ActivationCtrl(mockEmailer, mockPersonDao, mockActivationDao, mockSessionManager)
      val result1: Future[Result] = controller.activate().apply(FakeRequest(GET, "/api/activate?some=things&other=whatsit"))
      val result2: Future[Result] = controller.activate().apply(FakeRequest(GET, s"/api/activate?username=$validEmail&some=thing"))
      val result3: Future[Result] = controller.activate().apply(FakeRequest(GET, s"/api/activate?token=$validToken&some=thing"))

      // verify
      status(result1) mustBe 400
      contentAsString(result1) mustBe ""
      status(result2) mustBe 400
      contentAsString(result2) mustBe ""
      status(result2) mustBe 400
      contentAsString(result3) mustBe ""
      verifyZeroInteractions(mockPersonDao)
    }
    "should return a forbidden response for invalid tokens" in {
      // mocks
      val mockEmailer = mock[Emailer]
      val mockPersonDao = mock[PersonDao]
      val mockActivationDao = mock[ActivationDao]
      val mockSessionManager = mock[SessionManager]
      when(mockActivationDao.validateToken(validSessionToken)).thenReturn(false)

      // call
      val controller = new ActivationCtrl(mockEmailer, mockPersonDao, mockActivationDao, mockSessionManager)
      val result: Future[Result] = controller.activate().apply(FakeRequest(GET, s"/api/activate?username=$validEmail&token=$validToken&some=thing"))

      // verify
      status(result) mustBe 403
      contentAsString(result) mustBe ""
      verify(mockActivationDao).validateToken(validSessionToken)
      verifyZeroInteractions(mockActivationDao)
    }
    "should return an error when unable to activate the user" in {
      val mockEmailer = mock[Emailer]
      val mockPersonDao = mock[PersonDao]
      val mockActivationDao = mock[ActivationDao]
      val mockSessionManager = mock[SessionManager]
      when(mockActivationDao.validateToken(validSessionToken)).thenReturn(true)
      when(mockActivationDao.activate(validSessionToken)).thenReturn(false)

      // call
      val controller = new ActivationCtrl(mockEmailer, mockPersonDao, mockActivationDao, mockSessionManager)
      val result: Future[Result] = controller.activate().apply(FakeRequest(GET, s"/api/activate?username=$validEmail&token=$validToken&some=thing"))

      // verify
      status(result) mustBe 400
      contentAsString(result) mustBe ""
      verify(mockActivationDao).validateToken(validSessionToken)
      verify(mockActivationDao).activate(validSessionToken)
      verifyZeroInteractions(mockSessionManager)
    }
    "should redirect the user when the activation succeeds" in {
      val mockEmailer = mock[Emailer]
      val mockPersonDao = mock[PersonDao]
      val mockActivationDao = mock[ActivationDao]
      val mockSessionManager = mock[SessionManager]
      val targetUrl: String = "/#/list"
      when(mockActivationDao.validateToken(validSessionToken)).thenReturn(true)
      when(mockActivationDao.activate(validSessionToken)).thenReturn(true)
      when(mockSessionManager.signIn(validSessionToken, Redirect(targetUrl))).thenReturn(Redirect(targetUrl))

      // call
      val controller = new ActivationCtrl(mockEmailer, mockPersonDao, mockActivationDao, mockSessionManager)
      val result: Future[Result] = controller.activate().apply(FakeRequest(GET, s"/api/activate?username=$validEmail&token=$validToken&some=thing"))

      // verify
      status(result) mustBe 303
      contentAsString(result) mustBe ""
      verify(mockActivationDao).validateToken(validSessionToken)
      verify(mockActivationDao).activate(validSessionToken)
      verify(mockSessionManager).signIn(validSessionToken, Redirect(targetUrl))
    }
  }
  "ActivationCtrl#sendActivationEmail" should {
    "should return and error for an empty request" in {
      // mocks
      val mockEmailer = mock[Emailer]
      val mockPersonDao = mock[PersonDao]
      val mockActivationDao = mock[ActivationDao]
      val mockSessionManager = mock[SessionManager]

      // call
      val controller = new ActivationCtrl(mockEmailer, mockPersonDao, mockActivationDao, mockSessionManager)
      val result: Future[Result] = controller.sendActivationEmail().apply(FakeRequest())

      // verify
      status(result) mustBe 400
      contentAsString(result) mustBe ""
      verifyZeroInteractions(mockPersonDao)
    }
    "should return an error for an invalid request format" in {
      // mocks
      val mockEmailer = mock[Emailer]
      val mockPersonDao = mock[PersonDao]
      val mockActivationDao = mock[ActivationDao]
      val mockSessionManager = mock[SessionManager]

      // call
      val controller = new ActivationCtrl(mockEmailer, mockPersonDao, mockActivationDao, mockSessionManager)
      val result: Future[Result] = controller.sendActivationEmail().apply(FakeRequest().withJsonBody(Json.obj("some" -> "thing")))

      // verify
      status(result) mustBe 400
      contentAsString(result) mustBe ""
      verifyZeroInteractions(mockPersonDao)
    }
    "should return an error for unregistered users" in {
      // mocks
      val mockEmailer = mock[Emailer]
      val mockPersonDao = mock[PersonDao]
      val mockActivationDao = mock[ActivationDao]
      val mockSessionManager = mock[SessionManager]
      when(mockPersonDao.findByEmail(validEmail)).thenReturn(None)

      // call
      val controller = new ActivationCtrl(mockEmailer, mockPersonDao, mockActivationDao, mockSessionManager)
      val result: Future[Result] = controller.sendActivationEmail().apply(FakeRequest().withJsonBody(Json.obj("body" -> Json.obj("username" -> validEmail))))

      // verify
      status(result) mustBe 400
      contentAsString(result) mustBe ""
      verify(mockPersonDao).findByEmail(validEmail)
      verifyZeroInteractions(mockActivationDao)
    }
    "should return an error when unable to create a token" in {
      // mocks
      val mockEmailer = mock[Emailer]
      val mockPersonDao = mock[PersonDao]
      val mockActivationDao = mock[ActivationDao]
      val mockSessionManager = mock[SessionManager]
      val testPerson = Person(Some(1),"Test Guy","User", Credentials(validEmail,"password",CredentialStatus.Active),"boss@test.com", false)
      when(mockPersonDao.findByEmail(validEmail)).thenReturn(Some(testPerson))
      when(mockActivationDao.createActivationToken(validEmail)).thenReturn(None)

      // call
      val controller = new ActivationCtrl(mockEmailer, mockPersonDao, mockActivationDao, mockSessionManager)
      val result: Future[Result] = controller.sendActivationEmail().apply(FakeRequest().withJsonBody(Json.obj("body" -> Json.obj("username" -> validEmail))))

      // verify
      status(result) mustBe 400
      contentAsString(result) mustBe ""
      verify(mockPersonDao).findByEmail(validEmail)
      verify(mockActivationDao).createActivationToken(validEmail)
      verifyZeroInteractions(mockEmailer)
    }
    "should send an activation email" in {
      // mocks
      val mockEmailer = mock[Emailer]
      val mockPersonDao = mock[PersonDao]
      val mockActivationDao = mock[ActivationDao]
      val mockSessionManager = mock[SessionManager]
      val name: String = "Test Guy"
      val testPerson = Person(Some(1),name,"User", Credentials(validEmail,"password",CredentialStatus.Active),"boss@test.com", false)
      when(mockPersonDao.findByEmail(validEmail)).thenReturn(Some(testPerson))
      when(mockActivationDao.createActivationToken(validEmail)).thenReturn(Some(validSessionToken))

      // call
      val controller = new ActivationCtrl(mockEmailer, mockPersonDao, mockActivationDao, mockSessionManager)
      val result: Future[Result] = controller.sendActivationEmail().apply(FakeRequest().withJsonBody(Json.obj("body" -> Json.obj("username" -> validEmail))))

      // verify
      status(result) mustBe 200
      contentAsString(result) mustBe ""
      verify(mockPersonDao).findByEmail(validEmail)
      verify(mockActivationDao).createActivationToken(validEmail)
      verify(mockEmailer).sendActivationEmail(name, validSessionToken)
    }
  }
}
