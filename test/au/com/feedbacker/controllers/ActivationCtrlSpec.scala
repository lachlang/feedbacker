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
class ActivationCtrlSpec extends PlaySpec with MockitoSugar with Results {

  val validEmail: String = "valid@test.com"
  val validToken: String = "valid_token"
  val validSessionToken = SessionToken(validEmail, validToken)

  def fixture = {
    new {
      val mockEmailer = mock[Emailer]
      val mockPersonDao = mock[PersonDao]
      val mockActivationDao = mock[ActivationDao]
      val mockSessionManager = mock[SessionManager]
      val controller = new ActivationCtrl(mockEmailer, mockPersonDao, mockActivationDao, mockSessionManager)
    }
  }

  "ActivationCtrl#activate" should {
    "return an error for an empty request" in {
      val f = fixture
      val result: Future[Result] = f.controller.activate().apply(FakeRequest())

      // verify
      status(result) mustBe 400
      contentAsString(result) mustBe ""
      verifyZeroInteractions(f.mockPersonDao)
    }
    "return an error for an invalid request" in {
      val f = fixture
      val result1: Future[Result] = f.controller.activate().apply(FakeRequest(GET, "/api/activate?some=things&other=whatsit"))
      val result2: Future[Result] = f.controller.activate().apply(FakeRequest(GET, s"/api/activate?username=$validEmail&some=thing"))
      val result3: Future[Result] = f.controller.activate().apply(FakeRequest(GET, s"/api/activate?token=$validToken&some=thing"))

      // verify
      status(result1) mustBe 400
      contentAsString(result1) mustBe ""
      status(result2) mustBe 400
      contentAsString(result2) mustBe ""
      status(result2) mustBe 400
      contentAsString(result3) mustBe ""
      verifyZeroInteractions(f.mockPersonDao)
    }
    "return a forbidden response for invalid tokens" in {
      val f = fixture
      when(f.mockActivationDao.validateToken(validSessionToken)).thenReturn(false)
      val result: Future[Result] = f.controller.activate().apply(FakeRequest(GET, s"/api/activate?username=$validEmail&token=$validToken&some=thing"))

      // verify
      status(result) mustBe 403
      contentAsString(result) mustBe ""
      verify(f.mockActivationDao).validateToken(validSessionToken)
      verifyZeroInteractions(f.mockActivationDao)
    }
    "return an error when unable to activate the user" in {
      val f = fixture
      when(f.mockActivationDao.validateToken(validSessionToken)).thenReturn(true)
      when(f.mockActivationDao.activate(validSessionToken)).thenReturn(false)
      val result: Future[Result] = f.controller.activate().apply(FakeRequest(GET, s"/api/activate?username=$validEmail&token=$validToken&some=thing"))

      // verify
      status(result) mustBe 400
      contentAsString(result) mustBe ""
      verify(f.mockActivationDao).validateToken(validSessionToken)
      verify(f.mockActivationDao).activate(validSessionToken)
      verifyZeroInteractions(f.mockSessionManager)
    }
    "redirect the user when the activation succeeds" in {
      val f = fixture
      val targetUrl: String = "/#/list"
      when(f.mockActivationDao.validateToken(validSessionToken)).thenReturn(true)
      when(f.mockActivationDao.activate(validSessionToken)).thenReturn(true)
      when(f.mockSessionManager.signIn(validSessionToken, Redirect(targetUrl))).thenReturn(Redirect(targetUrl))
      val result: Future[Result] = f.controller.activate().apply(FakeRequest(GET, s"/api/activate?username=$validEmail&token=$validToken&some=thing"))

      // verify
      status(result) mustBe 303
      contentAsString(result) mustBe ""
      verify(f.mockActivationDao).validateToken(validSessionToken)
      verify(f.mockActivationDao).activate(validSessionToken)
      verify(f.mockSessionManager).signIn(validSessionToken, Redirect(targetUrl))
    }
  }
  "ActivationCtrl#sendActivationEmail" should {
    "return and error for an empty request" in {
      val f = fixture
      val result: Future[Result] = f.controller.sendActivationEmail().apply(FakeRequest())

      // verify
      status(result) mustBe 400
      contentAsString(result) mustBe ""
      verifyZeroInteractions(f.mockPersonDao)
    }
    "return an error for an invalid request format" in {
      val f = fixture
      val result: Future[Result] = f.controller.sendActivationEmail().apply(FakeRequest().withJsonBody(Json.obj("some" -> "thing")))

      // verify
      status(result) mustBe 400
      contentAsString(result) mustBe ""
      verifyZeroInteractions(f.mockPersonDao)
    }
    "return an error for unregistered users" in {
      val f = fixture
      when(f.mockPersonDao.findByEmail(validEmail)).thenReturn(None)
      val result: Future[Result] = f.controller.sendActivationEmail().apply(FakeRequest().withJsonBody(Json.obj("body" -> Json.obj("username" -> validEmail))))

      // verify
      status(result) mustBe 400
      contentAsString(result) mustBe ""
      verify(f.mockPersonDao).findByEmail(validEmail)
      verifyZeroInteractions(f.mockActivationDao)
    }
    "return an error when unable to create a token" in {
      val f = fixture
      val testPerson = Person(Some(1),"Test Guy","User", Credentials(validEmail,"password",CredentialStatus.Active),"boss@test.com", false)
      when(f.mockPersonDao.findByEmail(validEmail)).thenReturn(Some(testPerson))
      when(f.mockActivationDao.createActivationToken(validEmail)).thenReturn(None)
      val result: Future[Result] = f.controller.sendActivationEmail().apply(FakeRequest().withJsonBody(Json.obj("body" -> Json.obj("username" -> validEmail))))

      // verify
      status(result) mustBe 400
      contentAsString(result) mustBe ""
      verify(f.mockPersonDao).findByEmail(validEmail)
      verify(f.mockActivationDao).createActivationToken(validEmail)
      verifyZeroInteractions(f.mockEmailer)
    }
    "send an activation email" in {
      val f = fixture
      val name: String = "Test Guy"
      val testPerson = Person(Some(1),name,"User", Credentials(validEmail,"password",CredentialStatus.Active),"boss@test.com", false)
      when(f.mockPersonDao.findByEmail(validEmail)).thenReturn(Some(testPerson))
      when(f.mockActivationDao.createActivationToken(validEmail)).thenReturn(Some(validSessionToken))
      val result: Future[Result] = f.controller.sendActivationEmail().apply(FakeRequest().withJsonBody(Json.obj("body" -> Json.obj("username" -> validEmail))))

      // verify
      status(result) mustBe 200
      contentAsString(result) mustBe ""
      verify(f.mockPersonDao).findByEmail(validEmail)
      verify(f.mockActivationDao).createActivationToken(validEmail)
      verify(f.mockEmailer).sendActivationEmail(name, validSessionToken)
    }
  }
}
