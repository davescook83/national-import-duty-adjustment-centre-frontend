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

package uk.gov.hmrc.nationalimportdutyadjustmentcentrefrontend.models.bars

import uk.gov.hmrc.nationalimportdutyadjustmentcentrefrontend.base.{BarsTestData, UnitSpec}

class BARSResultSpec extends UnitSpec with BarsTestData {

  "BARSResult" should {

    "be valid if metadata and assess calls are valid" in {

      BARSResult(validMetaDataResponse, validAssessResponse).isValid mustBe true
    }

    "be invalid if sort code does not match account" in {

      BARSResult(
        validMetaDataResponse,
        validAssessResponse.copy(accountNumberWithSortCodeIsValid = "no")
      ).isValid mustBe false
    }

    "be invalid if it is indeterminate if sort code and account match" in {

      BARSResult(
        validMetaDataResponse,
        validAssessResponse.copy(accountNumberWithSortCodeIsValid = "indeterminate")
      ).isValid mustBe false
    }

    "be invalid if roll IS required" in {

      BARSResult(
        validMetaDataResponse,
        validAssessResponse.copy(nonStandardAccountDetailsRequiredForBacs = "yes")
      ).isValid mustBe false
    }

    "be invalid if bacs is not supported" in {

      BARSResult(validMetaDataResponse.copy(bacsOfficeStatus = "N"), validAssessResponse).isValid mustBe false
    }

    "be invalid if bacs credits is not supported" in {

      BARSResult(
        validMetaDataResponse.copy(disallowedTransactions = Seq("CR")),
        validAssessResponse
      ).isValid mustBe false
    }
  }

}
