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
class ActivationCtrlSpec extends PlaySpec with MockitoSugar with AllFixtures with PropertyChecks {

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
      forAll() { (c: Credentials, token: String) =>
        val f = fixture
        val result1: Future[Result] = f.controller.activate().apply(FakeRequest(GET, "/api/activate?some=things&other=whatsit"))
        val result2: Future[Result] = f.controller.activate().apply(FakeRequest(GET, s"/api/activate?username=${c.email}&some=thing"))
        val result3: Future[Result] = f.controller.activate().apply(FakeRequest(GET, s"/api/activate?token=$token&some=thing"))

        // verify
        status(result1) mustBe 400
        contentAsString(result1) mustBe ""
        status(result2) mustBe 400
        contentAsString(result2) mustBe ""
        status(result2) mustBe 400
        contentAsString(result3) mustBe ""
        verifyZeroInteractions(f.mockPersonDao)
      }
    }
    "return a forbidden response for invalid tokens" in {
      forAll() { (st: SessionToken) =>
        val f = fixture
        when(f.mockActivationDao.validateToken(st)).thenReturn(false)
        val result: Future[Result] = f.controller.activate().apply(FakeRequest(GET, s"/api/activate?username=${st.username}&token=${st.token}&some=thing"))

        // verify
        status(result) mustBe 403
        contentAsString(result) mustBe ""
        verify(f.mockActivationDao).validateToken(SessionToken(st.username.toLowerCase, st.token))
        verifyZeroInteractions(f.mockActivationDao)
      }
    }
    "return an error when unable to activate the user" in {
      forAll() { (st: SessionToken) =>
        val f = fixture
        val tt = SessionToken(st.username.toLowerCase, st.token)
        when(f.mockActivationDao.validateToken(tt)).thenReturn(true)
        when(f.mockActivationDao.activate(tt)).thenReturn(false)
        val result: Future[Result] = f.controller.activate().apply(FakeRequest(GET, s"/api/activate?username=${st.username}&token=${st.token}&some=thing"))

        // verify
        status(result) mustBe 400
        contentAsString(result) mustBe ""
        verify(f.mockActivationDao).validateToken(tt)
        verify(f.mockActivationDao).activate(tt)
        verifyZeroInteractions(f.mockSessionManager)
      }
    }
    "redirect the user when the activation succeeds" in {
      forAll() { (st: SessionToken) =>
        val f = fixture
        val targetUrl: String = "/#/list"
        val tt = SessionToken(st.username.toLowerCase, st.token)
        when(f.mockActivationDao.validateToken(tt)).thenReturn(true)
        when(f.mockActivationDao.activate(tt)).thenReturn(true)
        when(f.mockSessionManager.signIn(tt, Redirect(targetUrl))).thenReturn(Redirect(targetUrl))
        val result: Future[Result] = f.controller.activate().apply(FakeRequest(GET, s"/api/activate?username=${st.username}&token=${st.token}&some=thing"))

        // verify
        status(result) mustBe 303
        contentAsString(result) mustBe ""
        verify(f.mockActivationDao).validateToken(tt)
        verify(f.mockActivationDao).activate(tt)
        verify(f.mockSessionManager).signIn(tt, Redirect(targetUrl))
      }
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
      forAll() { (st: SessionToken) =>
        val f = fixture
        when(f.mockPersonDao.findByEmail(st.username)).thenReturn(None)
        val result: Future[Result] = f.controller.sendActivationEmail().apply(FakeRequest().withJsonBody(Json.obj("body" -> Json.obj("username" -> st.username))))

        // verify
        status(result) mustBe 400
        contentAsString(result) mustBe ""
        verify(f.mockPersonDao).findByEmail(st.username)
        verifyZeroInteractions(f.mockActivationDao)
      }
    }
    "return an error when unable to create a token" in {
      forAll() { (st: SessionToken, person: Person) =>
        val f = fixture
        when(f.mockPersonDao.findByEmail(st.username)).thenReturn(Some(person))
        when(f.mockActivationDao.createActivationToken(person.credentials.email)).thenReturn(None)
        val result: Future[Result] = f.controller.sendActivationEmail().apply(FakeRequest().withJsonBody(Json.obj("body" -> Json.obj("username" -> st.username))))

        // verify
        status(result) mustBe 400
        contentAsString(result) mustBe ""
        verify(f.mockPersonDao).findByEmail(st.username)
        verify(f.mockActivationDao).createActivationToken(person.credentials.email)
        verifyZeroInteractions(f.mockEmailer)
      }
    }
    "send an activation email" in {
      forAll() { (st: SessionToken, person: Person) =>
        val f = fixture
        when(f.mockPersonDao.findByEmail(person.credentials.email)).thenReturn(Some(person))
        when(f.mockActivationDao.createActivationToken(person.credentials.email)).thenReturn(Some(st))
        val result: Future[Result] = f.controller.sendActivationEmail().apply(FakeRequest().withJsonBody(Json.obj("body" -> Json.obj("username" -> person.credentials.email))))

        // verify
        status(result) mustBe 200
        contentAsString(result) mustBe ""
        verify(f.mockPersonDao).findByEmail(person.credentials.email)
        verify(f.mockActivationDao).createActivationToken(person.credentials.email)
        verify(f.mockEmailer).sendActivationEmail(person.name, st)
      }
    }
  }
}
