package au.com.feedbacker.controllers

import au.com.feedbacker.AllFixtures
import au.com.feedbacker.model._
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatest.mock.MockitoSugar
import org.scalatest.prop.PropertyChecks
import org.scalatestplus.play._
import play.api.libs.json._
import play.api.mvc._
import play.api.test.Helpers._
import play.api.test._

import scala.concurrent.Future


/**
  * Created by lachlang on 10/05/2017.
  */
class FeedbackSpec extends PlaySpec with MockitoSugar with AllFixtures with PropertyChecks {

  def fixture = {
    new {
      val mockPersonDao = mock[PersonDao]
      val mockNominationDao = mock[NominationDao]
      val mockFeedbackCycleDao = mock[FeedbackCycleDao]
      val mockAdHocFeedbackDao = mock[AdHocFeedbackDao]
      val mockSessionManager = mock[SessionManager]
      val controller = new Feedback(mockPersonDao, mockNominationDao, mockFeedbackCycleDao, mockAdHocFeedbackDao, mockSessionManager)
    }
  }

  "Feedback#createAdHocFeedback" should {
    "be forbidden when no session token is found" in {
      val f = fixture
      when(f.mockSessionManager.extractToken(any())).thenReturn(None)
      val result: Future[Result] = f.controller.createAdHocFeedback.apply(FakeRequest())

      // verify
      status(result) mustBe 403
      contentAsString(result) mustBe ""
      verifyZeroInteractions(f.mockPersonDao)
    }
    "fail for invalid username" in {
      forAll() { st: SessionToken =>
        val f = fixture
        when(f.mockSessionManager.extractToken(any())).thenReturn(None)
        val result: Future[Result] = f.controller.createAdHocFeedback.apply(FakeRequest().withCookies(SessionManager.createSessionCookie(st.token)))

        // verify
        verify(f.mockSessionManager).extractToken(any())
        status(result) mustBe 403
        contentAsString(result) mustBe ""
      }
    }
//    "return a valid user" in {
//      forAll() { (person: Person, st: SessionToken) =>
//        val f = fixture
//        when(f.mockSessionManager.extractToken(any())).thenReturn(Some(st))
//        when(f.mockPersonDao.findByEmail(st.username)).thenReturn(Some(person))
//        val result: Future[Result] = f.controller.getUser().apply(FakeRequest().withCookies(SessionManager.createSessionCookie(st.token)))
//
//        // verify
//        status(result) mustBe 200
//        contentAsJson(result) mustEqual Json.obj("body" -> Json.toJson(person))
//        verify(f.mockSessionManager).extractToken(any())
//        verify(f.mockPersonDao).findByEmail(st.username)
//      }
//    }
  }
  "Feedback#getAdHocFeedbackFor" should {
    "be forbidden when no session token is found" in {
      forAll() { person: Person =>
        val f = fixture
        when(f.mockSessionManager.extractToken(any())).thenReturn(None)
        val result: Future[Result] = f.controller.getAdHocFeedbackFor(person.credentials.email).apply(FakeRequest())

        // verify
        verify(f.mockSessionManager).extractToken(any())
        status(result) mustBe 403
      }
    }
    "be forbidden for an unregistered user" in {
      forAll() { (person: Person, st: SessionToken) =>
        // mock
        val f = fixture
        when(f.mockSessionManager.extractToken(any())).thenReturn(Some(st))
        when(f.mockPersonDao.findByEmail(st.username)).thenReturn(None)

        // call
        val result: Future[Result] = f.controller.getAdHocFeedbackFor(person.credentials.email).apply(FakeRequest().withCookies(SessionManager.createSessionCookie(st.token)))

        // verify
        verify(f.mockSessionManager).extractToken(any())
        verify(f.mockPersonDao).findByEmail(st.username)
        status(result) mustBe 403
      }
    }
    "be forbidden for invalid user" in {
      forAll() { (person: Person, st: SessionToken, invalidUsername: String) =>
        whenever(invalidUsername != person.credentials.email & invalidUsername != person.managerEmail) {
          val f = fixture
          when(f.mockSessionManager.extractToken(any())).thenReturn(Some(st))
          when(f.mockPersonDao.findByEmail(st.username)).thenReturn(Some(person))
          val result: Future[Result] = f.controller.getAdHocFeedbackFor(invalidUsername).apply(FakeRequest().withCookies(SessionManager.createSessionCookie(st.token)))

          // verify
          verify(f.mockSessionManager).extractToken(any())
          verify(f.mockPersonDao).findByEmail(st.username)
          status(result) mustBe 403
          contentAsString(result) mustEqual ""
        }
      }
    }
    "successfully return a valid list of ad hoc feedback items for a given user" in {
      forAll() { (person: Person, st: SessionToken, feedback: Seq[AdHocFeedback]) =>
        val f = fixture
        when(f.mockSessionManager.extractToken(any())).thenReturn(Some(st))
        when(f.mockPersonDao.findByEmail(st.username)).thenReturn(Some(person))
        when(f.mockAdHocFeedbackDao.getAdHocFeedbackFor(person.credentials.email)).thenReturn(feedback)
        val result: Future[Result] = f.controller.getAdHocFeedbackFor(person.credentials.email).apply(FakeRequest().withCookies(SessionManager.createSessionCookie(st.token)))

        // verify
        verify(f.mockSessionManager).extractToken(any())
        verify(f.mockPersonDao).findByEmail(st.username)
        verify(f.mockAdHocFeedbackDao).getAdHocFeedbackFor(person.credentials.email)
        status(result) mustBe 200
        contentAsJson(result) mustBe Json.obj("apiVersion" -> "1.0", "body" -> Json.toJson(feedback))
      }
    }
    "successfully return a valid list of ad hoc feedback items for a manager" in {
      forAll() { (person: Person, st: SessionToken, feedback: Seq[AdHocFeedback]) =>
        val f = fixture
        when(f.mockSessionManager.extractToken(any())).thenReturn(Some(st))
        when(f.mockPersonDao.findByEmail(st.username)).thenReturn(Some(person))
        when(f.mockAdHocFeedbackDao.getAdHocFeedbackFor(person.managerEmail)).thenReturn(feedback)
        val result: Future[Result] = f.controller.getAdHocFeedbackFor(person.managerEmail).apply(FakeRequest().withCookies(SessionManager.createSessionCookie(st.token)))

        // verify
        verify(f.mockSessionManager).extractToken(any())
        verify(f.mockPersonDao).findByEmail(st.username)
        verify(f.mockAdHocFeedbackDao).getAdHocFeedbackFor(person.managerEmail)
        contentAsJson(result) mustBe Json.obj("apiVersion" -> "1.0", "body" -> Json.toJson(feedback))
        status(result) mustBe 200
      }
    }
  }
  "Feedback#getAdHocFeedbackFrom" should {
    "be forbidden for an invalid token" in {
      forAll() { (st: SessionToken) =>
        val f = fixture
        when(f.mockSessionManager.extractToken(any())).thenReturn(None)
        val result: Future[Result] = f.controller.getAdHocFeedbackFrom.apply(FakeRequest().withCookies(SessionManager.createSessionCookie(st.token)))

        // verify
        status(result) mustBe 403
        contentAsString(result) mustBe ""
        verifyZeroInteractions(f.mockPersonDao)
      }
    }
    "be forbidden for an unregistered user" in {
      forAll() { (person: Person, st: SessionToken) =>
        val f = fixture
        when(f.mockSessionManager.extractToken(any())).thenReturn(Some(st))
        when(f.mockPersonDao.findByEmail(st.username)).thenReturn(None)
        val result: Future[Result] = f.controller.getAdHocFeedbackFrom.apply(FakeRequest().withCookies(SessionManager.createSessionCookie(st.token)))

        // verify
        verify(f.mockSessionManager).extractToken(any())
        verify(f.mockPersonDao).findByEmail(st.username)
        status(result) mustBe 403
      }
    }
    "successfully return an empty list" in {
      forAll() { (person: Person, st: SessionToken) =>
        val f = fixture
        when(f.mockSessionManager.extractToken(any())).thenReturn(Some(st))
        when(f.mockPersonDao.findByEmail(st.username)).thenReturn(Some(person))
        when(f.mockAdHocFeedbackDao.getAdHocFeedbackFrom(person.credentials.email)).thenReturn(Seq())
        val result: Future[Result] = f.controller.getAdHocFeedbackFrom.apply(FakeRequest().withCookies(SessionManager.createSessionCookie(st.token)))

        // verify
        verify(f.mockSessionManager).extractToken(any())
        verify(f.mockPersonDao).findByEmail(st.username)
        verify(f.mockAdHocFeedbackDao).getAdHocFeedbackFrom(person.credentials.email)
        status(result) mustBe 200
        contentAsJson(result) mustBe Json.obj("apiVersion" -> "1.0", "body" -> Json.arr())
      }
    }
    "successfully return a valid list of ad hoc feedback items" in {
      forAll() { (person: Person, st: SessionToken, feedback: Seq[AdHocFeedback]) =>
        val f = fixture
        when(f.mockSessionManager.extractToken(any())).thenReturn(Some(st))
        when(f.mockPersonDao.findByEmail(st.username)).thenReturn(Some(person))
        when(f.mockAdHocFeedbackDao.getAdHocFeedbackFrom(person.credentials.email)).thenReturn(feedback)
        val result: Future[Result] = f.controller.getAdHocFeedbackFrom.apply(FakeRequest().withCookies(SessionManager.createSessionCookie(st.token)))

        // verify
        verify(f.mockSessionManager).extractToken(any())
        verify(f.mockPersonDao).findByEmail(st.username)
        verify(f.mockAdHocFeedbackDao).getAdHocFeedbackFrom(person.credentials.email)
        status(result) mustBe 200
        contentAsJson(result) mustBe Json.obj("apiVersion" -> "1.0", "body" -> Json.toJson(feedback))
      }
    }
  }
}
