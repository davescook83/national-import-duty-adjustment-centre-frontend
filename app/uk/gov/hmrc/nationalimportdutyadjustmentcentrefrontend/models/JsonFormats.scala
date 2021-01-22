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

package uk.gov.hmrc.nationalimportdutyadjustmentcentrefrontend.models

import java.net.URL
import java.time.{Instant, LocalDateTime, ZoneOffset}

import play.api.libs.json._

import scala.util.Try

object JsonFormats {

  val formatLocalDateTime: OFormat[LocalDateTime] = new OFormat[LocalDateTime] {

    override def writes(datetime: LocalDateTime): JsObject =
      Json.obj("$date" -> datetime.atZone(ZoneOffset.UTC).toInstant.toEpochMilli)

    override def reads(json: JsValue): JsResult[LocalDateTime] =
      json match {
        case JsObject(map) if map.contains("$date") =>
          map("$date") match {
            case JsNumber(v) => JsSuccess(LocalDateTime.ofInstant(Instant.ofEpochMilli(v.longValue()), ZoneOffset.UTC))
            case _           => JsError("Unexpected Date Format. Expected a Number (Epoch Milliseconds)")
          }
        case _ => JsError("Unexpected Date Format. Expected an object containing a $date field.")
      }

  }

  implicit val formatURL: Format[URL] = new Format[URL] {

    override def reads(json: JsValue): JsResult[URL] =
      json match {
        case JsString(s) => parseUrl(s).map(JsSuccess(_)).getOrElse(invalidUrlError)
        case _           => invalidUrlError
      }

    private def parseUrl(s: String): Option[URL] = Try(new URL(s)).toOption

    private def invalidUrlError: JsError =
      JsError(Seq(JsPath() -> Seq(JsonValidationError("error.expected.url"))))

    override def writes(o: URL): JsValue = JsString(o.toString)
  }

}