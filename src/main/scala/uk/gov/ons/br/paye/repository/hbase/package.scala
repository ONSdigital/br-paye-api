package uk.gov.ons.br.paye.repository

import uk.gov.ons.br.repository.hbase.HBaseColumn

package object hbase {
  private val DataColumnFamily = "d"
  val ParentLinkColumn = HBaseColumn(family = DataColumnFamily, qualifier = "ubrn")
  private[hbase] val columnNameForQualifier: String => String = HBaseColumn.name(family = DataColumnFamily)
}
