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

import javax.inject.Inject
import play.api.libs.json.{Json, OFormat, Reads, Writes}
import play.mvc.Http.HeaderNames
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import uk.gov.hmrc.nationalimportdutyadjustmentcentrefrontend.config.AppConfig
import uk.gov.hmrc.nationalimportdutyadjustmentcentrefrontend.controllers
import uk.gov.hmrc.nationalimportdutyadjustmentcentrefrontend.models.JourneyId
import uk.gov.hmrc.nationalimportdutyadjustmentcentrefrontend.models.upscan.{
  UpscanFileReference,
  UpscanInitiateResponse
}

import scala.concurrent.{ExecutionContext, Future}

sealed trait UpscanInitiateRequest

case class UpscanInitiateRequestV2(
  callbackUrl: String,
  successRedirect: Option[String],
  errorRedirect: Option[String],
  minimumFileSize: Option[Int] = Some(1),
  maximumFileSize: Option[Int],
  expectedContentType: Option[String]
) extends UpscanInitiateRequest

case class UploadForm(href: String, fields: Map[String, String])

case class Reference(value: String)

object Reference {
  implicit val referenceReader: Reads[Reference] = Reads.StringReads.map(Reference(_))
}

case class PreparedUpload(reference: Reference, uploadRequest: UploadForm)

object UpscanInitiateRequestV2 {
  implicit val format: OFormat[UpscanInitiateRequestV2] = Json.format[UpscanInitiateRequestV2]
}

object PreparedUpload {

  implicit val uploadFormFormat: Reads[UploadForm] = Json.reads[UploadForm]

  implicit val format: Reads[PreparedUpload] = Json.reads[PreparedUpload]
}

class UpscanInitiateConnector @Inject() (httpClient: HttpClient, appConfig: AppConfig)(implicit ec: ExecutionContext) {

  private val headers = Map(HeaderNames.CONTENT_TYPE -> "application/json")

  def initiateV2(journeyId: JourneyId, redirectOnSuccess: Option[String], redirectOnError: Option[String])(implicit
    hc: HeaderCarrier
  ): Future[UpscanInitiateResponse] = {
    val request = UpscanInitiateRequestV2(
      callbackUrl =
        appConfig.upscan.callbackBase + controllers.routes.UploadCallbackController.callback(journeyId).url,
      successRedirect = redirectOnSuccess,
      errorRedirect = redirectOnError,
      maximumFileSize = Some(appConfig.upscan.maxFileSizeMb * 1024 * 1024),
      expectedContentType = Some(appConfig.upscan.approvedFileTypes)
    )
    initiate(appConfig.upscanInitiateV2Url, request)
  }

  private def initiate[T](url: String, request: T)(implicit
    hc: HeaderCarrier,
    wts: Writes[T]
  ): Future[UpscanInitiateResponse] =
    for {
      response <- httpClient.POST[T, PreparedUpload](url, request, headers.toSeq)
      fileReference = UpscanFileReference(response.reference.value)
      postTarget    = response.uploadRequest.href
      formFields    = response.uploadRequest.fields
    } yield UpscanInitiateResponse(fileReference, postTarget, formFields)

}
