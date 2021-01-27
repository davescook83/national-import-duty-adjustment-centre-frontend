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

package uk.gov.hmrc.nationalimportdutyadjustmentcentrefrontend.connectors

import uk.gov.hmrc.http.logging.RequestId

import javax.inject.Inject
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import uk.gov.hmrc.nationalimportdutyadjustmentcentrefrontend.config.AppConfig
import uk.gov.hmrc.nationalimportdutyadjustmentcentrefrontend.models.{CreateClaimRequest, CreateClaimResponse}

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

class NIDACConnector @Inject() (httpClient: HttpClient, appConfig: AppConfig)(implicit ec: ExecutionContext) {

  private val baseUrl = appConfig.nidacServiceBaseUrl

  def submitClaim(request: CreateClaimRequest)(implicit hc: HeaderCarrier): Future[CreateClaimResponse] =
    httpClient.POST[CreateClaimRequest, CreateClaimResponse](
      s"$baseUrl/create-claim",
      request,
      Seq("X-Correlation-Id" -> hc.requestId.map(_.value).getOrElse(UUID.randomUUID().toString))
    )

}
