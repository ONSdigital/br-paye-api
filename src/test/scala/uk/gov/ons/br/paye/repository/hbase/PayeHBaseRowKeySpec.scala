package uk.gov.ons.br.paye.repository.hbase

import uk.gov.ons.br.paye.test.SamplePaye.SamplePayeRef
import uk.gov.ons.br.test.UnitSpec

class PayeHBaseRowKeySpec extends UnitSpec {

  "The HBase RowKey for PAYE admin units" - {
    "is the PAYE reference" in {
      PayeHBaseRowKey(SamplePayeRef) shouldBe SamplePayeRef.value
    }
  }
}
