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

package uk.gov.hmrc.nationalimportdutyadjustmentcentrefrontend.base

import java.time.{LocalDateTime, ZonedDateTime}

import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.nationalimportdutyadjustmentcentrefrontend.connectors.Reference
import uk.gov.hmrc.nationalimportdutyadjustmentcentrefrontend.models.ClaimType.AntiDumping
import uk.gov.hmrc.nationalimportdutyadjustmentcentrefrontend.models.upscan._
import uk.gov.hmrc.nationalimportdutyadjustmentcentrefrontend.models.{ClaimType, UserAnswers}
import uk.gov.hmrc.nationalimportdutyadjustmentcentrefrontend.repositories.UploadDetails

trait TestData {

  // UserAnswers
  val emptyAnswers: UserAnswers = UserAnswers()

  val claimTypeAnswer: ClaimType = AntiDumping

  val uploadAnswer: UploadedFile =
    UploadedFile("reference", "/url", ZonedDateTime.now(), "checksum", "filename", "mime/type")

  val completeAnswers: UserAnswers = UserAnswers(claimType = Some(claimTypeAnswer), uploads = Some(Seq(uploadAnswer)))

  // Upscan
  val uploadId = UploadId.generate

  val upscanInitiateResponse: UpscanInitiateResponse =
    UpscanInitiateResponse(UpscanFileReference("file-ref"), "post-target", Map("field-hidden" -> "value-hidden"))

  def uploadResult(status: UploadStatus) =
    UploadDetails(BSONObjectID.generate(), uploadId, Reference("reference"), status, LocalDateTime.now)

  val uploadedFile =
    UploadedFile("reference", "downloadUrl", ZonedDateTime.now(), "checksum", "fileName", "fileMimeType")

}