package uk.gov.ons.br.paye.models

import play.api.libs.json.{Json, Writes}


case class EmployeeSplits(mfullemp: Option[Int],
                          msubemp: Option[Int],
                          ffullemp: Option[Int],
                          fsubemp: Option[Int],
                          unclemp: Option[Int],
                          unclsubemp: Option[Int])

object EmployeeSplits {
  implicit val writes: Writes[EmployeeSplits] = Json.writes[EmployeeSplits]
}
