package au.com.feedbacker.util

import au.com.feedbacker.controllers.Report
import au.com.feedbacker.model.{FeedbackCycle, FeedbackGroup, Nomination, QuestionResponse}

/**
  * Created by lachlang on 14/03/2017.
  */
class CsvReport {

  def createReportForPerson(report: Report): String = {
    s"""Feedback for: ${report.person.name}
       | ${serialiseReviewCycles(report.reviewCycle)}
       |
     """.stripMargin
  }

  def createReportForCycle(cycle: FeedbackCycle, nominations: Seq[Nomination]): String = {
    s"""Feedback for: ${cycle.label}
       |${serialiseNominations(nominations)}
       |
     """.stripMargin
  }

  private def serialiseReviewCycles(feedback: Seq[FeedbackGroup], output: String = ""): String = {
    feedback match {
      case Nil      => output
      case fg::fgs  => serialiseReviewCycles(fgs, s"\n$output\n\n${fg.cycle.label},${fg.cycle.endDate}\n${serialiseNominations(fg.feedback)}")
    }
  }

  private def serialiseNominations(nominations: Seq[Nomination], output: String = ""): String = {
    nominations match {
      case Nil    => output
      case n::ns  => serialiseNominations(ns, s"$output\nFeedback from ${n.to.map(_.name).getOrElse("")} for ${n.from.map(_.name).getOrElse("")} on ${n.lastUpdated.getOrElse("not submitted")}\n${serialiseQuestions(1, n.questions).toString}")
    }
  }

  private def serialiseQuestions(count: Int, questions: Seq[QuestionResponse], qf: QuestionFormat = QuestionFormat()): QuestionFormat = {
    questions match {
      case Nil    => qf
      case q::qs  => serialiseQuestions(count + 1, qs, QuestionFormat(s"${qf.questionCount},Question ${count}", s"${qf.questionText},${q.text.replace(',',' ')}", s"${qf.response},${q.response.getOrElse("").replace(',',' ')}", s"${qf.comments},${q.comments.getOrElse("").replace(',',' ').replace('\n',' ')}"))
    }
  }
}

object CsvReport extends CsvReport

case class QuestionFormat(questionCount: String = "", questionText: String = "", response: String = "", comments: String = "") {
  override def toString(): String = s"${this.questionCount}\n${this.questionText}\n${this.response}\n${this.comments}"
}
