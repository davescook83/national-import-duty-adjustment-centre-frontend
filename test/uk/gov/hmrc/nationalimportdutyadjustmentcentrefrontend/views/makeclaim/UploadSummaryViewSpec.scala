/*
 * Copyright 2021 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.nationalimportdutyadjustmentcentrefrontend.views.makeclaim

import org.jsoup.nodes.Document
import play.api.data.Form
import uk.gov.hmrc.nationalimportdutyadjustmentcentrefrontend.base.{TestData, UnitViewSpec}
import uk.gov.hmrc.nationalimportdutyadjustmentcentrefrontend.controllers.makeclaim.routes
import uk.gov.hmrc.nationalimportdutyadjustmentcentrefrontend.forms.YesNoFormProvider
import uk.gov.hmrc.nationalimportdutyadjustmentcentrefrontend.models.create.ClaimType._
import uk.gov.hmrc.nationalimportdutyadjustmentcentrefrontend.models.create.ClaimType
import uk.gov.hmrc.nationalimportdutyadjustmentcentrefrontend.models.upscan.UploadedFile
import uk.gov.hmrc.nationalimportdutyadjustmentcentrefrontend.views.html.makeclaim.UploadSummaryView

import scala.collection.JavaConverters._

class UploadSummaryViewSpec extends UnitViewSpec with TestData {

  private val page = instanceOf[UploadSummaryView]
  private val form = new YesNoFormProvider().apply("upload_documents_summary.add.required")

  private def view(
    form: Form[Boolean] = form,
    claimType: Option[ClaimType] = None,
    uploadedDocuments: Seq[UploadedFile] = Seq.empty
  ): Document =
    page(form, claimType, uploadedDocuments, navigatorBack)

  "UploadSummaryPage" should {

    "have correct title" in {
      view().title() must startWith(messages("upload_documents_summary.title.multiple", 0))
    }

    "have correct heading" in {
      view().getElementsByTag("h1") must containMessage("upload_documents_summary.title.multiple", 0)
    }

    "have back link" in {
      view() must haveNavigatorBackLink(navigatorBackUrl)
    }

    "have the uploaded file names" in {
      val summaryText =
        view(uploadedDocuments = Seq(uploadAnswer, uploadAnswer2)).getElementsByClass("govuk-summary-list").text()
      summaryText must include(uploadAnswer.fileName)
      summaryText must include(uploadAnswer2.fileName)
    }

    "have a remove link" in {
      val link = view(uploadedDocuments = Seq(uploadAnswer)).getElementsByClass("govuk-link").first()
      link must includeMessage("upload_documents_summary.remove.label")
      link must includeMessage("upload_documents_summary.remove.label.hidden", uploadAnswer.fileName)
      link must haveAttribute("href", routes.UploadFormSummaryController.onRemove(uploadAnswer.upscanReference).url)
    }

    "have correct document types" when {

      def documentTypes(claimType: ClaimType) =
        view(claimType = Some(claimType)).getElementById("document-type-list").children().eachText().asScala

      "claim type is Preference" in {
        val types = documentTypes(Preference)
        types mustBe List("C88", "commercial invoice", "E2", "preference certificate (EUR, ATR or other)")
      }

      "claim type is Quota" in {
        val types = documentTypes(Quota)
        types mustBe List("C88", "commercial invoice", "E2", "preference certificate (EUR, ATR or other)")
      }

      "claim type is Airworthiness" in {
        val types = documentTypes(Airworthiness)
        types mustBe List("air worthiness certificate", "C88", "commercial invoice", "E2")
      }

      "claim type is AntiDumping" in {
        val types = documentTypes(AntiDumping)
        types mustBe List("C88", "commercial invoice", "E2")
      }
    }
  }

  "UploadSummaryPage on filled form" should {

    "have populated fields" in {
      val filledView = view(form.bind(Map("yesOrNo" -> "true")))

      filledView.getElementById("yesOrNo") must haveValue("true")
    }

    "display error when " when {

      "answer missing" in {
        view(form.bind(Map("yesOrNo" -> ""))).getElementsByClass("govuk-error-summary") must containMessage(
          "upload_documents_summary.add.required"
        )
      }

    }

  }
}
