package au.com.feedbacker.controllers

import au.com.feedbacker.AllFixtures
import au.com.feedbacker.model._
import play.api.libs.json._

import scala.concurrent.Future
import org.scalatestplus.play._
import play.api.mvc._
import play.api.test._
import play.api.test.Helpers._
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import org.mockito.Matchers._
import org.scalatest.prop.PropertyChecks
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen


/**
  * Created by lachlang on 16/02/2017.
  */
class AccountSpec extends PlaySpec with MockitoSugar with AllFixtures with PropertyChecks {

  val upToTenPeople = for {
    n <- Gen.choose(0, 10)
    people <- Gen.listOfN(n, arbitrary[Person])
  } yield people

  def fixture = {
    new {
      val mockPersonDao = mock[PersonDao]
      val mockNominationDao = mock[NominationDao]
      val mockSessionManager = mock[SessionManager]
      val controller = new Account(mockPersonDao, mockNominationDao, mockSessionManager)
    }
  }

  "Account#getUser" should {
    "be forbidden when no session token is found" in {
      val f = fixture
      when(f.mockSessionManager.extractToken(any())).thenReturn(None)
      val result: Future[Result] = f.controller.getUser().apply(FakeRequest())

      // verify
      status(result) mustBe 403
      contentAsString(result) mustBe ""
      verifyZeroInteractions(f.mockPersonDao)
    }
    "fail for invalid username" in {
      forAll() { st: SessionToken =>
        val f = fixture
        when(f.mockSessionManager.extractToken(any())).thenReturn(None)
        val result: Future[Result] = f.controller.getUser().apply(FakeRequest().withCookies(SessionManager.createSessionCookie(st.token)))

        // verify
        verify(f.mockSessionManager).extractToken(any())
        status(result) mustBe 403
        contentAsString(result) mustBe ""
      }
    }
    "return a valid user" in {
      forAll() { (person: Person, st: SessionToken) =>
        val f = fixture
        when(f.mockSessionManager.extractToken(any())).thenReturn(Some(st))
        when(f.mockPersonDao.findByEmail(st.username)).thenReturn(Some(person))
        val result: Future[Result] = f.controller.getUser().apply(FakeRequest().withCookies(SessionManager.createSessionCookie(st.token)))

        // verify
        status(result) mustBe 200
        contentAsJson(result) mustEqual Json.obj("body" -> Json.toJson(person))
        verify(f.mockSessionManager).extractToken(any())
        verify(f.mockPersonDao).findByEmail(st.username)
      }
    }
  }
  "Account#getReports" should {
    "successfully return no reports when none are found" in {
      forAll() { (person: Person, st: SessionToken) =>
        val f = fixture
        when(f.mockSessionManager.extractToken(any())).thenReturn(Some(st))
        when(f.mockPersonDao.findByEmail(st.username)).thenReturn(Some(person))
        when(f.mockPersonDao.findDirectReports(person.credentials.email)).thenReturn(Seq())
        val result: Future[Result] = f.controller.getReports().apply(FakeRequest().withCookies(SessionManager.createSessionCookie(st.token)))

        // verify
        verify(f.mockSessionManager).extractToken(any())
        verify(f.mockPersonDao).findByEmail(st.username)
        verify(f.mockPersonDao).findDirectReports(person.credentials.email)
        status(result) mustBe 200
        contentAsJson(result) mustEqual Json.obj("body" -> Json.arr())
      }
    }

    "render a report when found" in {
      forAll(arbitrary[Person], arbitrary[SessionToken], upToTenPeople) { (person: Person, st: SessionToken, reports: List[Person]) =>
        // mock
        val feedbackGroup: FeedbackGroup = FeedbackGroup(FeedbackCycle.orphan, Seq(Nomination(Some(1), Some(person), Some(person), FeedbackStatus.Pending, None, Seq(), false, 1)))
        val f = fixture
        when(f.mockSessionManager.extractToken(any())).thenReturn(Some(st))
        when(f.mockPersonDao.findByEmail(st.username)).thenReturn(Some(person))
        when(f.mockPersonDao.findDirectReports(person.credentials.email)).thenReturn(reports)
        reports.foreach { report =>
          when(f.mockNominationDao.getHistoryReportForUser(report.credentials.email)).thenReturn(Seq(feedbackGroup))

        }
//        when(f.mockNominationDao.getHistoryReportForUser(person.credentials.email)).thenReturn(Seq(feedbackGroup))

        // call
        val result: Future[Result] = f.controller.getReports().apply(FakeRequest().withCookies(SessionManager.createSessionCookie(st.token)))

        // verify
        verify(f.mockSessionManager).extractToken(any())
        verify(f.mockPersonDao).findByEmail(st.username)
        verify(f.mockPersonDao).findDirectReports(person.credentials.email)
//        verify(f.mockNominationDao).getHistoryReportForUser(person.credentials.email)
        status(result) mustBe 200
//        contentAsJson(result) mustEqual Json.obj("body" -> Json.arr(Json.toJson(Report(person, Seq(feedbackGroup)))))
//        fail
      }
    }
    "be forbidden for invalid user" in {
      forAll() { st: SessionToken =>
        val f = fixture
        when(f.mockSessionManager.extractToken(any())).thenReturn(None)
        val result: Future[Result] = f.controller.getReports().apply(FakeRequest().withCookies(SessionManager.createSessionCookie(st.token)))

        // verify
        verify(f.mockSessionManager).extractToken(any())
        status(result) mustBe 403
        contentAsString(result) mustEqual ""
      }
    }
  }
  "Account#updateUserDetails" should {
    "be forbidden for an invalid user" in {
      forAll() { st: SessionToken =>
        val f = fixture
        when(f.mockSessionManager.extractToken(any())).thenReturn(None)
        val result: Future[Result] = f.controller.updateUserDetails().apply(FakeRequest().withCookies(SessionManager.createSessionCookie(st.token)))

        // verify
        status(result) mustBe 403
        contentAsString(result) mustBe ""
        verifyZeroInteractions(f.mockPersonDao)
      }
    }
    "require a request body" in {
      forAll() { (person: Person, st: SessionToken) =>
        val f = fixture
        when(f.mockSessionManager.extractToken(any())).thenReturn(Some(st))
        when(f.mockPersonDao.findByEmail(st.username)).thenReturn(Some(person))
        val result: Future[Result] = f.controller.updateUserDetails().apply(FakeRequest().withCookies(SessionManager.createSessionCookie(st.token)))

        // verify
        verify(f.mockSessionManager).extractToken(any())
        verify(f.mockPersonDao).findByEmail(st.username)
        status(result) mustBe 403
      }
    }
    "require a json request body" in {
      forAll() { (person: Person, st: SessionToken) =>
        val f = fixture
        when(f.mockSessionManager.extractToken(any())).thenReturn(Some(st))
        when(f.mockPersonDao.findByEmail(st.username)).thenReturn(Some(person))
        val fakeRequest = FakeRequest(PUT, "api/user").withTextBody("This is some text").withCookies(SessionManager.createSessionCookie(st.token))
        val result: Future[Result] = f.controller.updateUserDetails().apply(fakeRequest)

        // verify
        verify(f.mockSessionManager).extractToken(any())
        verify(f.mockPersonDao).findByEmail(st.username)
        status(result) mustBe 403
      }
    }
    "require a valid request body to update" in {
      forAll() { (person: Person, st: SessionToken) =>
        val f = fixture
        when(f.mockSessionManager.extractToken(any())).thenReturn(Some(st))
        when(f.mockPersonDao.findByEmail(st.username)).thenReturn(Some(person))
        val fakeRequest = FakeRequest().withJsonBody(Json.toJson("{'this':'thing'}")).withCookies(SessionManager.createSessionCookie(st.token))
        val result: Future[Result] = f.controller.updateUserDetails().apply(fakeRequest)

        // verify
        verify(f.mockSessionManager).extractToken(any())
        verify(f.mockPersonDao).findByEmail(st.username)
        status(result) mustBe 400
        contentAsJson(result) mustBe Json.obj("body" -> Json.obj("message" -> "Could not update user details."))
      }
    }
    "return an error when the update fails" in {
      forAll() { (person: Person, st: SessionToken, up: UpdateContent) =>
        val f = fixture
        val upPerson = Person(person.id, up.name, up.role, person.credentials, up.managerEmail.toLowerCase, person.isLeader)
        val errorMessage: String = "Could not update yo' stuff."
        when(f.mockSessionManager.extractToken(any())).thenReturn(Some(st))
        when(f.mockPersonDao.findByEmail(st.username)).thenReturn(Some(person))
        when(f.mockPersonDao.update(upPerson)).thenReturn(Left(new Exception(errorMessage)))

        // call
        val fakeRequest = FakeRequest().withJsonBody(Json.toJson(UpdateContent(up.name, up.role, up.managerEmail))).withCookies(SessionManager.createSessionCookie(st.token))
        val result: Future[Result] = f.controller.updateUserDetails().apply(fakeRequest)

        // verify
        verify(f.mockSessionManager).extractToken(any())
        verify(f.mockPersonDao).findByEmail(st.username)
        verify(f.mockPersonDao).update(upPerson)
        status(result) mustBe 400
        contentAsJson(result) mustBe Json.obj("body" -> Json.obj("message" -> errorMessage))
      }
    }
    "return the updated user on success" in {
      forAll() { (person: Person, st: SessionToken, up: UpdateContent) =>
        val f = fixture
        val upPerson = Person(person.id, up.name, up.role, person.credentials, up.managerEmail.toLowerCase, person.isLeader)
        when(f.mockSessionManager.extractToken(any())).thenReturn(Some(st))
        when(f.mockPersonDao.findByEmail(st.username)).thenReturn(Some(person))
        when(f.mockPersonDao.update(upPerson)).thenReturn(Right(upPerson))
        val fakeRequest = FakeRequest().withJsonBody(Json.toJson(UpdateContent(up.name, up.role, up.managerEmail))).withCookies(SessionManager.createSessionCookie(st.token))
        val result: Future[Result] = f.controller.updateUserDetails().apply(fakeRequest)

        // verify
        verify(f.mockSessionManager).extractToken(any())
        verify(f.mockPersonDao).findByEmail(st.username)
        verify(f.mockPersonDao).update(upPerson)
        status(result) mustBe 200
        contentAsJson(result) mustBe Json.obj("apiVersion" -> "1.0") ++ Json.obj("body" -> Json.toJson(upPerson))
      }
    }
  }
}
