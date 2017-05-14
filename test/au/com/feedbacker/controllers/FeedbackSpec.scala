package au.com.feedbacker.controllers

import au.com.feedbacker.AllFixtures
import au.com.feedbacker.model._
import au.com.feedbacker.util.Emailer
import org.joda.time.DateTime
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
      val mockEmailer = mock[Emailer]
      val controller = new Feedback(mockPersonDao, mockNominationDao, mockFeedbackCycleDao, mockAdHocFeedbackDao, mockEmailer, mockSessionManager)
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
    "return an internal server error if the database write fails" in {
      forAll() { (user: Person, recipient: Person, st: SessionToken, feedbackReq: AdHocFeedbackRequest) =>
        val f = fixture
        when(f.mockSessionManager.extractToken(any())).thenReturn(Some(st))
        when(f.mockPersonDao.findByEmail(st.username)).thenReturn(Some(user))
        when(f.mockPersonDao.findByEmail(feedbackReq.toEmail)).thenReturn(Some(recipient))
        when(f.mockAdHocFeedbackDao.createAdHocFeedback(AdHocFeedback(None, fromEmail = user.credentials.email, fromName = user.name,
          fromRole = user.role, toEmail = recipient.credentials.email, toName = recipient.name, toRole = recipient.role,
          created = any(), message = feedbackReq.message, publish = feedbackReq.publishToRecipient))).thenReturn(None)
        val fakeRequest = FakeRequest().withJsonBody(Json.toJson(feedbackReq)).withCookies(SessionManager.createSessionCookie(st.token))
        val result: Future[Result] = f.controller.createAdHocFeedback().apply(fakeRequest)

        // verify
        status(result) mustBe 500
        contentAsJson(result) mustBe Json.obj("message" -> "Could not create ad-hoc feedback.")
        verify(f.mockSessionManager).extractToken(any())
        verify(f.mockPersonDao).findByEmail(st.username)
        verify(f.mockPersonDao).findByEmail(feedbackReq.toEmail)
        verify(f.mockAdHocFeedbackDao).createAdHocFeedback(AdHocFeedback(None, fromEmail = user.credentials.email, fromName = user.name,
          fromRole = user.role, toEmail = recipient.credentials.email, toName = recipient.name, toRole = recipient.role,
          created = any(), message = feedbackReq.message, publish = feedbackReq.publishToRecipient))
      }
    }
    "return success when feedback is successfully created" in {
      forAll() { (user: Person, recipient: Person, st: SessionToken, feedbackReq: AdHocFeedbackRequest, id: Long) =>
        val f = fixture
        when(f.mockSessionManager.extractToken(any())).thenReturn(Some(st))
        when(f.mockPersonDao.findByEmail(st.username)).thenReturn(Some(user))
        when(f.mockPersonDao.findByEmail(feedbackReq.toEmail)).thenReturn(Some(recipient))
        val feedback = AdHocFeedback(Some(id), fromEmail = user.credentials.email, fromName = user.name,
          fromRole = user.role, toEmail = recipient.credentials.email, toName = recipient.name, toRole = recipient.role,
          created = DateTime.now, message = feedbackReq.message, publish = feedbackReq.publishToRecipient)
        when(f.mockAdHocFeedbackDao.createAdHocFeedback(AdHocFeedback(None, fromEmail = user.credentials.email, fromName = user.name,
          fromRole = user.role, toEmail = recipient.credentials.email, toName = recipient.name, toRole = recipient.role,
          created = any(), message = feedbackReq.message, publish = feedbackReq.publishToRecipient))).thenReturn(Some(feedback))
        when(f.mockPersonDao.findByEmail(recipient.managerEmail)).thenReturn(Some(recipient))
        val fakeRequest = FakeRequest().withJsonBody(Json.toJson(feedbackReq)).withCookies(SessionManager.createSessionCookie(st.token))
        val result: Future[Result] = f.controller.createAdHocFeedback().apply(fakeRequest)

          // verify
        status(result) mustBe 200
        contentAsJson(result) mustEqual Json.obj("apiVersion" -> "1.0","body" -> Json.toJson(feedback))
        verify(f.mockSessionManager).extractToken(any())
        verify(f.mockPersonDao).findByEmail(st.username)
        verify(f.mockPersonDao).findByEmail(feedbackReq.toEmail)
        verify(f.mockPersonDao).findByEmail(recipient.managerEmail)
        verify(f.mockAdHocFeedbackDao).createAdHocFeedback(AdHocFeedback(None, fromEmail = user.credentials.email, fromName = user.name,
          fromRole = user.role, toEmail = recipient.credentials.email, toName = recipient.name, toRole = recipient.role,
          created = any(), message = feedbackReq.message, publish = feedbackReq.publishToRecipient))
        verify(f.mockEmailer).sendAdHocFeedbackEmail(feedback, Some(recipient), recipient.managerEmail)
      }
    }
  }
  "Feedback#getAdHocFeedbackForUser" should {
    "be forbidden when no session token is found" in {
      forAll() { person: Person =>
        val f = fixture
        when(f.mockSessionManager.extractToken(any())).thenReturn(None)
        val result: Future[Result] = f.controller.getAdHocFeedbackForUser(person.credentials.email).apply(FakeRequest())

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
        val result: Future[Result] = f.controller.getAdHocFeedbackForUser(person.credentials.email).apply(FakeRequest().withCookies(SessionManager.createSessionCookie(st.token)))

        // verify
        verify(f.mockSessionManager).extractToken(any())
        verify(f.mockPersonDao).findByEmail(st.username)
        status(result) mustBe 403
      }
    }
    "successfully reject returning ad hoc feedback items for the given user" in {
      forAll(arbitrary[Person], arbitrary[SessionToken], upToTwentyAdHocSubmissions) { (person: Person, st: SessionToken, feedback: Seq[AdHocFeedback]) =>
        val f = fixture
        when(f.mockSessionManager.extractToken(any())).thenReturn(Some(st))
        when(f.mockPersonDao.findByEmail(st.username)).thenReturn(Some(person))
        val result: Future[Result] = f.controller.getAdHocFeedbackForUser(person.credentials.email).apply(FakeRequest().withCookies(SessionManager.createSessionCookie(st.token)))

        // verify
        verify(f.mockSessionManager).extractToken(any())
        verify(f.mockPersonDao).findByEmail(st.username)
        status(result) mustBe 403
        contentAsString(result) mustEqual ""
      }
    }
    "successfully reject returning ad hoc feedback items for a user not in the appropriate line management structure" in {
      forAll(arbitrary[Person], arbitrary[SessionToken], upToTwentyAdHocSubmissions) { (person: Person, st: SessionToken, feedback: Seq[AdHocFeedback]) =>
        val f = fixture
        when(f.mockSessionManager.extractToken(any())).thenReturn(Some(st))
        when(f.mockPersonDao.findByEmail(st.username)).thenReturn(Some(person))
        when(f.mockPersonDao.findByEmail(person.credentials.email)).thenReturn(Some(person))
        when(f.mockPersonDao.findByEmail(person.managerEmail)).thenReturn(None)
        val result: Future[Result] = f.controller.getAdHocFeedbackForUser(person.credentials.email).apply(FakeRequest().withCookies(SessionManager.createSessionCookie(st.token)))

        // verify
        verify(f.mockSessionManager).extractToken(any())
        verify(f.mockPersonDao).findByEmail(st.username)
        status(result) mustBe 403
        contentAsString(result) mustEqual ""
      }
    }
    "successfully return an empty list of ad hoc feedback items for a manager" in {
      forAll() { (requestor: Person, recipient: Person, st: SessionToken) =>
        val f = fixture
        when(f.mockSessionManager.extractToken(any())).thenReturn(Some(st))
        when(f.mockPersonDao.findByEmail(st.username)).thenReturn(Some(requestor))
        when(f.mockPersonDao.findByEmail(recipient.credentials.email)).thenReturn(Some(recipient.copy(managerEmail = requestor.credentials.email)))
        when(f.mockAdHocFeedbackDao.getAdHocFeedbackForReport(recipient.credentials.email)).thenReturn(Seq())
        val result: Future[Result] = f.controller.getAdHocFeedbackForUser(recipient.credentials.email).apply(FakeRequest().withCookies(SessionManager.createSessionCookie(st.token)))

        // verify
        verify(f.mockSessionManager).extractToken(any())
        verify(f.mockPersonDao).findByEmail(st.username)
        verify(f.mockPersonDao).findByEmail(recipient.credentials.email)
        verify(f.mockAdHocFeedbackDao).getAdHocFeedbackForReport(recipient.credentials.email)
        contentAsJson(result) mustBe Json.obj("apiVersion" -> "1.0", "body" -> Json.arr())
        status(result) mustBe 200
      }
    }
    "successfully return a valid list of ad hoc feedback items for a manager" in {
      forAll(arbitrary[Person], arbitrary[Person], arbitrary[SessionToken], upToTwentyAdHocSubmissions) { (requestor: Person, recipient: Person, st: SessionToken, feedback: Seq[AdHocFeedback]) =>
        val f = fixture
        when(f.mockSessionManager.extractToken(any())).thenReturn(Some(st))
        when(f.mockPersonDao.findByEmail(st.username)).thenReturn(Some(requestor))
        when(f.mockPersonDao.findByEmail(recipient.credentials.email)).thenReturn(Some(recipient.copy(managerEmail = requestor.credentials.email)))
        when(f.mockAdHocFeedbackDao.getAdHocFeedbackForReport(recipient.credentials.email)).thenReturn(feedback)
        val result: Future[Result] = f.controller.getAdHocFeedbackForUser(recipient.credentials.email).apply(FakeRequest().withCookies(SessionManager.createSessionCookie(st.token)))

        // verify
        verify(f.mockSessionManager).extractToken(any())
        verify(f.mockPersonDao).findByEmail(st.username)
        verify(f.mockPersonDao).findByEmail(recipient.credentials.email)
        verify(f.mockAdHocFeedbackDao).getAdHocFeedbackForReport(recipient.credentials.email)
        contentAsJson(result) mustBe Json.obj("apiVersion" -> "1.0", "body" -> Json.toJson(feedback))
        status(result) mustBe 200
      }
    }
  }
  "Feedback#getAdHocFeedbackForSelf" should {
    "be forbidden when no session token is found" in {
      forAll() { person: Person =>
        val f = fixture
        when(f.mockSessionManager.extractToken(any())).thenReturn(None)
        val result: Future[Result] = f.controller.getAdHocFeedbackForSelf.apply(FakeRequest())

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
        val result: Future[Result] = f.controller.getAdHocFeedbackForSelf.apply(FakeRequest().withCookies(SessionManager.createSessionCookie(st.token)))

        // verify
        verify(f.mockSessionManager).extractToken(any())
        verify(f.mockPersonDao).findByEmail(st.username)
        status(result) mustBe 403
      }
    }
    "successfully return an empty list when there is no feedback for the current user" in {
      forAll() { (person: Person, st: SessionToken, invalidUsername: String) =>
        whenever(invalidUsername != person.credentials.email & invalidUsername != person.managerEmail) {
          val f = fixture
          when(f.mockSessionManager.extractToken(any())).thenReturn(Some(st))
          when(f.mockPersonDao.findByEmail(st.username)).thenReturn(Some(person))
          when(f.mockAdHocFeedbackDao.getAdHocFeedbackForSelf(person.credentials.email)).thenReturn(Seq())
          val result: Future[Result] = f.controller.getAdHocFeedbackForSelf.apply(FakeRequest().withCookies(SessionManager.createSessionCookie(st.token)))

          // verify
          verify(f.mockSessionManager).extractToken(any())
          verify(f.mockPersonDao).findByEmail(st.username)
          verify(f.mockAdHocFeedbackDao).getAdHocFeedbackForSelf(person.credentials.email)
          status(result) mustBe 200
          contentAsJson(result) mustBe Json.obj("apiVersion" -> "1.0", "body" -> Json.arr())
        }
      }
    }
    "successfully return a valid list of ad hoc feedback items for the current user" in {
      forAll(arbitrary[Person], arbitrary[SessionToken], upToTwentyAdHocSubmissions) { (person: Person, st: SessionToken, feedback: Seq[AdHocFeedback]) =>
        val f = fixture
        when(f.mockSessionManager.extractToken(any())).thenReturn(Some(st))
        when(f.mockPersonDao.findByEmail(st.username)).thenReturn(Some(person))
        when(f.mockAdHocFeedbackDao.getAdHocFeedbackForSelf(person.credentials.email)).thenReturn(feedback)
        val result: Future[Result] = f.controller.getAdHocFeedbackForSelf.apply(FakeRequest().withCookies(SessionManager.createSessionCookie(st.token)))

        // verify
        verify(f.mockSessionManager).extractToken(any())
        verify(f.mockPersonDao).findByEmail(st.username)
        verify(f.mockAdHocFeedbackDao).getAdHocFeedbackForSelf(person.credentials.email)
        status(result) mustBe 200
        contentAsJson(result) mustBe Json.obj("apiVersion" -> "1.0", "body" -> Json.toJson(feedback))
      }
    }
  }
  "Feedback#getAdHocFeedbackFromSelf" should {
    "be forbidden for an invalid token" in {
      forAll() { (st: SessionToken) =>
        val f = fixture
        when(f.mockSessionManager.extractToken(any())).thenReturn(None)
        val result: Future[Result] = f.controller.getAdHocFeedbackFromSelf.apply(FakeRequest().withCookies(SessionManager.createSessionCookie(st.token)))

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
        val result: Future[Result] = f.controller.getAdHocFeedbackFromSelf.apply(FakeRequest().withCookies(SessionManager.createSessionCookie(st.token)))

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
        when(f.mockAdHocFeedbackDao.getAdHocFeedbackFromSelf(person.credentials.email)).thenReturn(Seq())
        val result: Future[Result] = f.controller.getAdHocFeedbackFromSelf.apply(FakeRequest().withCookies(SessionManager.createSessionCookie(st.token)))

        // verify
        verify(f.mockSessionManager).extractToken(any())
        verify(f.mockPersonDao).findByEmail(st.username)
        verify(f.mockAdHocFeedbackDao).getAdHocFeedbackFromSelf(person.credentials.email)
        status(result) mustBe 200
        contentAsJson(result) mustBe Json.obj("apiVersion" -> "1.0", "body" -> Json.arr())
      }
    }
    "successfully return a valid list of ad hoc feedback items" in {
      forAll(arbitrary[Person], arbitrary[SessionToken], upToTwentyAdHocSubmissions) { (person: Person, st: SessionToken, feedback: Seq[AdHocFeedback]) =>
        val f = fixture
        when(f.mockSessionManager.extractToken(any())).thenReturn(Some(st))
        when(f.mockPersonDao.findByEmail(st.username)).thenReturn(Some(person))
        when(f.mockAdHocFeedbackDao.getAdHocFeedbackFromSelf(person.credentials.email)).thenReturn(feedback)
        val result: Future[Result] = f.controller.getAdHocFeedbackFromSelf.apply(FakeRequest().withCookies(SessionManager.createSessionCookie(st.token)))

        // verify
        verify(f.mockSessionManager).extractToken(any())
        verify(f.mockPersonDao).findByEmail(st.username)
        verify(f.mockAdHocFeedbackDao).getAdHocFeedbackFromSelf(person.credentials.email)
        status(result) mustBe 200
        contentAsJson(result) mustBe Json.obj("apiVersion" -> "1.0", "body" -> Json.toJson(feedback))
      }
    }
  }
}
