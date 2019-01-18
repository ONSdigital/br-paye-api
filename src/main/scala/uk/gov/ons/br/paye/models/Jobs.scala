package uk.gov.ons.br.paye.models


import play.api.libs.json.{Json, Writes}

case class Jobs(mar: Option[Int],
                jun: Option[Int],
                sep: Option[Int],
                dec: Option[Int],
                lastUpdated: Option[String])

object Jobs {
  implicit val writes: Writes[Jobs] = Json.writes[Jobs]
}