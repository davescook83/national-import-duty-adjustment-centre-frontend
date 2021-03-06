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

import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.nationalimportdutyadjustmentcentrefrontend.models.bars.AssessBusinessBankDetailsResponse.{
  indeterminate,
  no,
  yes
}

case class AssessBusinessBankDetailsResponse(
  accountNumberWithSortCodeIsValid: String,
  nonStandardAccountDetailsRequiredForBacs: String,
  accountExists: String,
  companyNameMatches: String
) {
  val validAccountAndSortCode: Boolean = accountNumberWithSortCodeIsValid == yes
  val rollNotRequired: Boolean         = nonStandardAccountDetailsRequiredForBacs == no
  val accountValid: Boolean            = Set(yes, indeterminate).contains(accountExists)
  val companyNameValid: Boolean        = Set(yes, indeterminate).contains(companyNameMatches)
}

object AssessBusinessBankDetailsResponse {
  implicit val format: OFormat[AssessBusinessBankDetailsResponse] = Json.format[AssessBusinessBankDetailsResponse]

  private val notApplicableValue = "n/a"

  private val yes           = "yes"
  private val no            = "no"
  private val indeterminate = "indeterminate"

  val notApplicable =
    AssessBusinessBankDetailsResponse(notApplicableValue, notApplicableValue, notApplicableValue, notApplicableValue)

}
