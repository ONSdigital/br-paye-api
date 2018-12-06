package uk.gov.ons.br.paye.models


import play.api.libs.json.Writes
import play.api.libs.json.Writes.StringWrites
import play.api.mvc.PathBindable
import play.api.mvc.PathBindable.bindableString

// private constructor dictates that an instance is only available via apply() - allowing us to validate if necessary.
final case class PayeRef private (value: String)

object PayeRef {
  private def underlying(payeRef: PayeRef): String =
    payeRef.value

  // write an instance of the model to JSON as a simple string value
  implicit val writes: Writes[PayeRef] =
    Writes((StringWrites.writes _).compose(underlying))

  /*
   * Support binding instances of the model as URL path parameters.
   * Unfortunately this mechanism relies on implicit resolution (an instance bound via guice for example is ignored).
   */
  implicit val pathBindable: PathBindable[PayeRef] =
    bindableString.transform(toB = PayeRef.apply, toA = underlying)
}
