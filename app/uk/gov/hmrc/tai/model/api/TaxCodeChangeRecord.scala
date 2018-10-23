/*
 * Copyright 2018 HM Revenue & Customs
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

package uk.gov.hmrc.tai.model.api

import org.joda.time.LocalDate
import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.tai.util.DateTimeHelper.dateTimeOrdering

case class TaxCodeChangeRecord(taxCode: String,
                               basisOfOperation: String,
                               startDate: LocalDate,
                               endDate: LocalDate,
                               employerName: String,
                               payrollNumber: Option[String],
                               pensionIndicator: Boolean,
                               primary: Boolean)

object TaxCodeChangeRecord {
  implicit val format: OFormat[TaxCodeChangeRecord] = Json.format[TaxCodeChangeRecord]
}

case class TaxCodeChange(current: Seq[TaxCodeChangeRecord], previous: Seq[TaxCodeChangeRecord]) {
  def latestTaxCodeChangeDate: LocalDate = current.map(_.startDate).min

  def primaryCurrentTaxCode: String = primaryTaxCode(current)
  def secondaryCurrentTaxCodes: Seq[String] = secondaryTaxCode(current)
  def primaryPreviousTaxCode: String = primaryTaxCode(previous)
  def secondaryPreviousTaxCodes: Seq[String] = secondaryTaxCode(previous)

  def primaryCurrentPayrollNumber: Option[String] = primaryPayrollNumber(current)
  def secondaryCurrentPayrollNumbers: Seq[String] = secondaryPayrollNumbers(current)
  def primaryPreviousPayrollNumber: Option[String] = primaryPayrollNumber(previous)
  def secondaryPreviousPayrollNumbers: Seq[String] = secondaryPayrollNumbers(previous)

  private def primaryPayrollNumber(records: Seq[TaxCodeChangeRecord]) = primaryRecord(records).payrollNumber
  private def secondaryPayrollNumbers(records: Seq[TaxCodeChangeRecord]) = secondaryRecords(records).flatMap(_.payrollNumber)
  private def primaryTaxCode(records: Seq[TaxCodeChangeRecord]) = primaryRecord(records).taxCode
  private def secondaryTaxCode(records: Seq[TaxCodeChangeRecord]) = secondaryRecords(records).map(_.taxCode)

  private def primaryRecord(records: Seq[TaxCodeChangeRecord]) =
    records.find(_.primary).getOrElse(throw new RuntimeException("No primary tax code record found"))

  private def secondaryRecords(records: Seq[TaxCodeChangeRecord]) = records.filterNot(_.primary)
}

object TaxCodeChange {
  implicit val format: OFormat[TaxCodeChange] = Json.format[TaxCodeChange]
}