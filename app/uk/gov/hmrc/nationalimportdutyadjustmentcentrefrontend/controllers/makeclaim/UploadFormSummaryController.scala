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

package uk.gov.hmrc.nationalimportdutyadjustmentcentrefrontend.controllers.makeclaim

import javax.inject.{Inject, Singleton}
import play.api.i18n.I18nSupport
import play.api.mvc._
import uk.gov.hmrc.nationalimportdutyadjustmentcentrefrontend.controllers
import uk.gov.hmrc.nationalimportdutyadjustmentcentrefrontend.controllers.actions.IdentifierAction
import uk.gov.hmrc.nationalimportdutyadjustmentcentrefrontend.forms.YesNoFormProvider
import uk.gov.hmrc.nationalimportdutyadjustmentcentrefrontend.models.UserAnswers
import uk.gov.hmrc.nationalimportdutyadjustmentcentrefrontend.navigation.Navigator
import uk.gov.hmrc.nationalimportdutyadjustmentcentrefrontend.pages.UploadSummaryPage
import uk.gov.hmrc.nationalimportdutyadjustmentcentrefrontend.services.CacheDataService
import uk.gov.hmrc.nationalimportdutyadjustmentcentrefrontend.views.html.makeclaim.UploadSummaryPage
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.ExecutionContext

@Singleton
class UploadFormSummaryController @Inject() (
  mcc: MessagesControllerComponents,
  identify: IdentifierAction,
  data: CacheDataService,
  formProvider: YesNoFormProvider,
  navigator: Navigator,
  summaryPage: UploadSummaryPage
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport {

  private val form = formProvider("upload_documents_summary.add.required")

  def onPageLoad(): Action[AnyContent] = identify.async { implicit request =>
    data.getAnswers map { answers =>
      answers.uploads match {
        case Some(documents) if documents.nonEmpty => Ok(summaryPage(form, answers.claimType, documents))
        case _                                     => Redirect(controllers.makeclaim.routes.UploadFormController.onPageLoad())
      }
    }
  }

  def onSubmit(): Action[AnyContent] = identify.async { implicit request =>
    data.getAnswers map { answers =>
      form.bindFromRequest().fold(
        formWithErrors =>
          BadRequest(summaryPage(formWithErrors, answers.claimType, answers.uploads.getOrElse(Seq.empty))),
        addAnother =>
          if (addAnother)
            Redirect(controllers.makeclaim.routes.UploadFormController.onPageLoad())
          else
            Redirect(navigator.nextPage(UploadSummaryPage, answers))
      )
    }
  }

  def onRemove(documentReference: String): Action[AnyContent] = identify.async { implicit request =>
    data.updateAnswers(removeDocument(documentReference)) map { _ =>
      Redirect(controllers.makeclaim.routes.UploadFormSummaryController.onPageLoad())
    }
  }

  def removeDocument: String => UserAnswers => UserAnswers = (ref: String) =>
    (userAnswers: UserAnswers) => {
      val remainingFiles = userAnswers.uploads.map(_.filterNot(_.upscanReference == ref))
      userAnswers.copy(uploads = remainingFiles)
    }

}
