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

import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import play.api.data.{Form, FormError}
import play.api.http.Status
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.nationalimportdutyadjustmentcentrefrontend.base.{BarsTestData, ControllerSpec, TestData}
import uk.gov.hmrc.nationalimportdutyadjustmentcentrefrontend.forms.create.BankDetailsFormProvider
import uk.gov.hmrc.nationalimportdutyadjustmentcentrefrontend.models.create.{BankDetails, CreateAnswers}
import uk.gov.hmrc.nationalimportdutyadjustmentcentrefrontend.pages.BankDetailsPage
import uk.gov.hmrc.nationalimportdutyadjustmentcentrefrontend.services.BankAccountReputationService
import uk.gov.hmrc.nationalimportdutyadjustmentcentrefrontend.views.html.makeclaim.BankDetailsView
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents

import scala.concurrent.Future

class BankDetailsControllerSpec extends ControllerSpec with TestData with BarsTestData {

  private val page         = mock[BankDetailsView]
  private val formProvider = new BankDetailsFormProvider

  private val bankAccountReputationService = mock[BankAccountReputationService]

  private def controller =
    new BankDetailsController(
      fakeAuthorisedIdentifierAction,
      cacheDataService,
      bankAccountReputationService,
      formProvider,
      stubMessagesControllerComponents(),
      navigator,
      page
    )(executionContext)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    withEmptyCache()
    when(page.apply(any(), any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
    when(bankAccountReputationService.validate(any())(any())).thenReturn(Future.successful(barsSuccessResult))
  }

  override protected def afterEach(): Unit = {
    reset(page, bankAccountReputationService)
    super.afterEach()
  }

  def theResponseForm: Form[BankDetails] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[BankDetails]])
    verify(page).apply(captor.capture(), any(), any())(any(), any())
    captor.getValue
  }

  "GET" should {

    "display page when cache is empty" in {
      val result = controller.onPageLoad()(fakeGetRequest)
      status(result) mustBe Status.OK

      theResponseForm.value mustBe None
    }

    "display page when cache has answer" in {
      withCacheCreateAnswers(CreateAnswers(bankDetails = Some(bankDetailsAnswer)))
      val result = controller.onPageLoad()(fakeGetRequest)
      status(result) mustBe Status.OK

      theResponseForm.value mustBe Some(bankDetailsAnswer)
    }
  }

  "POST" should {

    val validRequest = postRequest(
      "accountName"   -> bankDetailsAnswer.accountName,
      "sortCode"      -> bankDetailsAnswer.sortCode,
      "accountNumber" -> bankDetailsAnswer.accountNumber
    )

    "update cache and redirect when valid answer is submitted" in {

      withCacheCreateAnswers(emptyAnswers)

      val result = controller.onSubmit()(validRequest)
      status(result) mustEqual SEE_OTHER
      theUpdatedCreateAnswers.bankDetails mustBe Some(bankDetailsAnswer)
      redirectLocation(result) mustBe Some(navigator.nextPage(BankDetailsPage, emptyAnswers).url)
    }

    "return 400 (BAD REQUEST) when BARS mod check fails" in {

      when(bankAccountReputationService.validate(any())(any())).thenReturn(Future.successful(barsInvalidAccountResult))
      val result = controller.onSubmit()(validRequest)
      status(result) mustEqual BAD_REQUEST

      theResponseForm.errors mustBe Seq(FormError("accountNumber", "bankDetails.bars.validation.modCheckFailed"))
    }

    "return 400 (BAD REQUEST) when BARS roll required check fails" in {

      when(bankAccountReputationService.validate(any())(any())).thenReturn(Future.successful(barsRollRequiredResult))
      val result = controller.onSubmit()(validRequest)
      status(result) mustEqual BAD_REQUEST

      theResponseForm.errors mustBe Seq(FormError("sortCode", "bankDetails.bars.validation.rollRequired"))
    }

    "return 400 (BAD REQUEST) when BARS BACS supported check fails" in {

      when(bankAccountReputationService.validate(any())(any())).thenReturn(
        Future.successful(barsBacsNotSupportedResult)
      )
      val result = controller.onSubmit()(validRequest)
      status(result) mustEqual BAD_REQUEST

      theResponseForm.errors mustBe Seq(FormError("sortCode", "bankDetails.bars.validation.bacsNotSupported"))
    }

    "return 400 (BAD REQUEST) when invalid data posted" in {

      val result = controller.onSubmit()(postRequest())
      status(result) mustEqual BAD_REQUEST
    }
  }
}
