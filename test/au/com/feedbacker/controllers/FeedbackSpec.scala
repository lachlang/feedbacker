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

  val upToTwentyAdHocSubmissions = for {
    n <- Gen.choose(0, 20)
    people <- Gen.listOfN(n, arbitrary[AdHocFeedback])
  } yield people

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
    "reject and invalid request" in {
      forAll() { (person: Person, st: SessionToken) =>
        val f = fixture
        when(f.mockSessionManager.extractToken(any())).thenReturn(Some(st))
        when(f.mockPersonDao.findByEmail(st.username)).thenReturn(Some(person))
        val fakeRequest = FakeRequest().withJsonBody(Json.toJson("{'this':'thing'}")).withCookies(SessionManager.createSessionCookie(st.token))
        val result: Future[Result] = f.controller.createAdHocFeedback().apply(fakeRequest)

        // verify
        status(result) mustBe 400
        verify(f.mockSessionManager).extractToken(any())
        verify(f.mockPersonDao).findByEmail(st.username)
      }
    }
//    "return an internal server error if the database write fails" in {
//      forAll() { (person: Person, st: SessionToken, feedback: AdHocFeedback) =>
//        val f = fixture
//        when(f.mockSessionManager.extractToken(any())).thenReturn(Some(st))
//        when(f.mockPersonDao.findByEmail(st.username)).thenReturn(Some(person))
//        when(f.mockPersonDao.findByEmail(feedback.toEmail)).thenReturn(Some(Person(None, feedback.toName, feedback.toRole, Credentials(feedback.toEmail,"",CredentialStatus.Active),"")))
//        val newFeedback = feedback.copy(id = None, fromEmail = person.credentials.email, fromName = person.name, fromRole = person.role)
//        println(s"testing for:  $newFeedback")
//        when(f.mockAdHocFeedbackDao.createAdHocFeedback(newFeedback)).thenReturn(None)
//        val fakeRequest = FakeRequest().withJsonBody(Json.toJson(AdHocFeedbackRequest(feedback.toEmail, feedback.message, feedback.publish))).withCookies(SessionManager.createSessionCookie(st.token))
//        val result: Future[Result] = f.controller.createAdHocFeedback().apply(fakeRequest)
//
//        // verify
//        status(result) mustBe 500
//        contentAsJson(result) mustBe Json.obj("message" -> "Could not create ad-hoc feedback.")
//        verify(f.mockSessionManager).extractToken(any())
//        verify(f.mockPersonDao).findByEmail(st.username)
////        verify(f.mockPerosnDao).findByEmail(feedback.toEmail)
//        verify(f.mockAdHocFeedbackDao).createAdHocFeedback(newFeedback)
//      }
//    }
//    "return success when feedback is successfully created" in {
//      forAll() { (person: Person, st: SessionToken, feedback: AdHocFeedback, id: Long) =>
//        val f = fixture
//        when(f.mockSessionManager.extractToken(any())).thenReturn(Some(st))
//        when(f.mockPersonDao.findByEmail(st.username)).thenReturn(Some(person))
//        when(f.mockPersonDao.findByEmail(feedback.toEmail)).thenReturn(Some(Person(None, feedback.toName, feedback.toRole, Credentials(feedback.toEmail,"",CredentialStatus.Active),"")))
//        val newFeedback = feedback.copy(id = None)
//        val createdFeedback = feedback.copy(id = Some(id))
//        when(f.mockAdHocFeedbackDao.createAdHocFeedback(newFeedback)).thenReturn(Some(createdFeedback))
//        val fakeRequest = FakeRequest().withJsonBody(Json.toJson(AdHocFeedbackRequest(feedback.toEmail, feedback.message, feedback.publish))).withCookies(SessionManager.createSessionCookie(st.token))
//        val result: Future[Result] = f.controller.createAdHocFeedback().apply(fakeRequest)
//
//          // verify
//        status(result) mustBe 200
//        contentAsJson(result) mustEqual Json.obj("body" -> Json.toJson(createdFeedback))
//        verify(f.mockSessionManager).extractToken(any())
//        verify(f.mockPersonDao).findByEmail(st.username)
//        verify(f.mockAdHocFeedbackDao).createAdHocFeedback(newFeedback)
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
      forAll(arbitrary[Person], arbitrary[SessionToken], upToTwentyAdHocSubmissions) { (person: Person, st: SessionToken, feedback: Seq[AdHocFeedback]) =>
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
      forAll(arbitrary[Person], arbitrary[SessionToken], upToTwentyAdHocSubmissions) { (person: Person, st: SessionToken, feedback: Seq[AdHocFeedback]) =>
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
      forAll(arbitrary[Person], arbitrary[SessionToken], upToTwentyAdHocSubmissions) { (person: Person, st: SessionToken, feedback: Seq[AdHocFeedback]) =>
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
