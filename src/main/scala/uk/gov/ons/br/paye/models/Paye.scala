package uk.gov.ons.br.paye.models


import play.api.libs.json.{Json, Writes}
import uk.gov.ons.br.models.{Address, Lifespan, LinkToLegalUnit}

/*
 * previousPayeref is modelled as a String to highlight that it is an unvalidated pass-through.
 * Only internally validated PAYE references should be promoted to the PayeRef type.
 */
case class Paye(payeref: PayeRef,
                name: String,
                tradingStyle: Option[String],
                legalStatus: String,
                stc: Option[String],
                previousPayeref: Option[String],
                employerCategory: Option[String],
                actionDate: Option[String],
                lifespan: Option[Lifespan],
                jobs: Option[Jobs],
                employeeSplits: Option[EmployeeSplits],
                address: Address,
                links: Option[LinkToLegalUnit])

object Paye {
  implicit val writes: Writes[Paye] = Json.writes[Paye]
}
