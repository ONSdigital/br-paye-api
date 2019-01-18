package uk.gov.ons.br.paye.repository.hbase

import uk.gov.ons.br.paye.models.PayeRef
import uk.gov.ons.br.repository.hbase.RowKey

object PayeHBaseRowKey extends (PayeRef => RowKey) {
  def apply(payeRef: PayeRef): RowKey =
    payeRef.value
}
