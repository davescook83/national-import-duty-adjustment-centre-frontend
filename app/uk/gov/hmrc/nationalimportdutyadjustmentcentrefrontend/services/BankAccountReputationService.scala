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

package uk.gov.hmrc.nationalimportdutyadjustmentcentrefrontend.services

import javax.inject.Inject
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.nationalimportdutyadjustmentcentrefrontend.connectors.BARSConnector
import uk.gov.hmrc.nationalimportdutyadjustmentcentrefrontend.models.bars.{AssessBusinessBankDetailsRequest, BARSResult}
import uk.gov.hmrc.nationalimportdutyadjustmentcentrefrontend.models.create.BankDetails

import scala.concurrent.{ExecutionContext, Future}

class BankAccountReputationService @Inject() (connector: BARSConnector)(implicit ec: ExecutionContext) {

  def validate(bankDetails: BankDetails)(implicit hc: HeaderCarrier): Future[BARSResult] =
    connector.sortcodeMetadata(bankDetails.sortCode) flatMap {
      case Some(metadata) if metadata.acceptsBacsPayments =>
        connector.assessBusinessBankDetails(AssessBusinessBankDetailsRequest(bankDetails)) map { assessment =>
          BARSResult(metadata, assessment)
        }
      case Some(metadata) => Future(BARSResult.apply(metadata))
      case None           => Future(BARSResult.notFound)
    }

}
