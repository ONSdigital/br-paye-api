package uk.gov.ons.br.paye.repository.hbase

import org.scalamock.scalatest.MockFactory
import org.slf4j.Logger
import uk.gov.ons.br.models.Lifespan
import uk.gov.ons.br.paye.models.{EmployeeSplits, Jobs}
import uk.gov.ons.br.paye.repository.hbase.PayeHBaseRowMapperSpec._
import uk.gov.ons.br.paye.test.SamplePaye.SamplePayeUnitWithOnlyMandatoryFields
import uk.gov.ons.br.repository.hbase.{HBaseCell, HBaseColumn, HBaseRow}
import uk.gov.ons.br.test.UnitSpec

class PayeHBaseRowMapperSpec extends UnitSpec with MockFactory {

  private trait Fixture {
    def samplePayeRowWith(cells: Seq[HBaseCell]): HBaseRow =
      HBaseRow(key = "unused", cells)

    implicit val logger: Logger = stub[Logger]
    val underTest = PayeHBaseRowMapper
  }

  "A Paye HBaseRowMapper" - {
    "can create a PAYE admin unit" - {
      "when all columns are defined" in new Fixture {
        underTest.fromRow(samplePayeRowWith(AllCells)) shouldBe Some(SamplePayeUnitWithAllFields)
      }

      "ignoring unrecognised columns" in new Fixture {
        underTest.fromRow(samplePayeRowWith(AllCells :+ UnusedCell)) shouldBe Some(SamplePayeUnitWithAllFields)
      }

      /*
       * By 'minimal' we mean the mandatory columns + one of the 'name' columns.
       */
      "when only minimal columns are defined" in new Fixture {
        val minimalPayeWithName = SamplePayeUnitWithOnlyMandatoryFields.copy(name = Name1Cell.value)

        underTest.fromRow(samplePayeRowWith(MandatoryCells :+ Name1Cell)) shouldBe Some(minimalPayeWithName)
      }

      "building the 'name' value from multiple columns" - {
        "using 'name1' when it is the only name column defined" in new Fixture {
          val minimalPayeWithName1 = SamplePayeUnitWithOnlyMandatoryFields.copy(name = Name1Cell.value)

          underTest.fromRow(samplePayeRowWith(MandatoryCells :+ Name1Cell)) shouldBe Some(minimalPayeWithName1)
        }

        "using 'name2' when it is the only name column defined" in new Fixture {
          val minimalPayeWithName2 = SamplePayeUnitWithOnlyMandatoryFields.copy(name = Name2Cell.value)

          underTest.fromRow(samplePayeRowWith(MandatoryCells :+ Name2Cell)) shouldBe Some(minimalPayeWithName2)
        }

        "using 'name3' when it is the only name column defined" in new Fixture {
          val minimalPayeWithName3 = SamplePayeUnitWithOnlyMandatoryFields.copy(name = Name3Cell.value)

          underTest.fromRow(samplePayeRowWith(MandatoryCells :+ Name3Cell)) shouldBe Some(minimalPayeWithName3)
        }

        "appending 'name2' to 'name1' when they are both defined and 'name3' is not" in new Fixture {
          val expectedName = Name1Cell.value + Name2Cell.value
          val minimalPayeWithName1and2 = SamplePayeUnitWithOnlyMandatoryFields.copy(name = expectedName)

          underTest.fromRow(samplePayeRowWith(MandatoryCells ++ Seq(Name1Cell, Name2Cell))) shouldBe Some(minimalPayeWithName1and2)
        }

        "appending 'name3' to 'name1' when they are both defined and 'name2' is not" in new Fixture {
          val expectedName = Name1Cell.value + Name3Cell.value
          val minimalPayeWithName1and3 = SamplePayeUnitWithOnlyMandatoryFields.copy(name = expectedName)

          underTest.fromRow(samplePayeRowWith(MandatoryCells ++ Seq(Name1Cell, Name3Cell))) shouldBe Some(minimalPayeWithName1and3)
        }

        "appending 'name3' to 'name2' when they are both defined and 'name1' is not" in new Fixture {
          val expectedName = Name2Cell.value + Name3Cell.value
          val minimalPayeWithName2and3 = SamplePayeUnitWithOnlyMandatoryFields.copy(name = expectedName)

          underTest.fromRow(samplePayeRowWith(MandatoryCells ++ Seq(Name2Cell, Name3Cell))) shouldBe Some(minimalPayeWithName2and3)
        }

        "concatenating 'name1' with 'name2' with 'name3' when they are all defined" in new Fixture {
          val expectedName = Name1Cell.value + Name2Cell.value + Name3Cell.value
          val minimalPayeWithName = SamplePayeUnitWithOnlyMandatoryFields.copy(name = expectedName)

          underTest.fromRow(samplePayeRowWith(MandatoryCells ++ Seq(Name1Cell, Name2Cell, Name3Cell))) shouldBe Some(minimalPayeWithName)
        }
      }

      "building the 'tradingStyle' value from multiple columns" - {
        "using 'tradstyle1' when it is the only trading style column defined" in new Fixture {
          val payeWithTradingStyle1 = SamplePayeUnitWithAllFields.copy(tradingStyle = Some(TradingStyle1Cell.value))
          val tradingStyle2and3 = Set(TradingStyle2Cell, TradingStyle3Cell).map(_.column)
          val noTradingStyle2and3 = AllCells.filterNot(cell => tradingStyle2and3.contains(cell.column))

          underTest.fromRow(samplePayeRowWith(noTradingStyle2and3)) shouldBe Some(payeWithTradingStyle1)
        }

        "using 'tradstyle2' when it is the only trading style column defined" in new Fixture {
          val payeWithTradingStyle2 = SamplePayeUnitWithAllFields.copy(tradingStyle = Some(TradingStyle2Cell.value))
          val tradingStyle1and3 = Set(TradingStyle1Cell, TradingStyle3Cell).map(_.column)
          val noTradingStyle1and3 = AllCells.filterNot(cell => tradingStyle1and3.contains(cell.column))

          underTest.fromRow(samplePayeRowWith(noTradingStyle1and3)) shouldBe Some(payeWithTradingStyle2)
        }

        "using 'tradstyle3' when it is the only trading style column defined" in new Fixture {
          val payeWithTradingStyle3 = SamplePayeUnitWithAllFields.copy(tradingStyle = Some(TradingStyle3Cell.value))
          val tradingStyle1and2 = Set(TradingStyle1Cell, TradingStyle2Cell).map(_.column)
          val noTradingStyle1and2 = AllCells.filterNot(cell => tradingStyle1and2.contains(cell.column))

          underTest.fromRow(samplePayeRowWith(noTradingStyle1and2)) shouldBe Some(payeWithTradingStyle3)
        }

        "appending 'tradstyle2' to 'tradstyle1' when they are both defined and 'tradstyle3' is not" in new Fixture {
          val payeWithTradingStyle1and2 = SamplePayeUnitWithAllFields.copy(tradingStyle =
            Some(TradingStyle1Cell.value + TradingStyle2Cell.value))
          val noTradingStyle3 = AllCells.filterNot(cell => cell.column == TradingStyle3Cell.column)

          underTest.fromRow(samplePayeRowWith(noTradingStyle3)) shouldBe Some(payeWithTradingStyle1and2)
        }

        "appending 'tradstyle3' to 'tradstyle1' when they are both defined and 'tradstyle2' is not" in new Fixture {
          val payeWithTradingStyle1and3 = SamplePayeUnitWithAllFields.copy(tradingStyle =
            Some(TradingStyle1Cell.value + TradingStyle3Cell.value))
          val noTradingStyle2 = AllCells.filterNot(cell => cell.column == TradingStyle2Cell.column)

          underTest.fromRow(samplePayeRowWith(noTradingStyle2)) shouldBe Some(payeWithTradingStyle1and3)
        }

        "appending 'tradstyle3' to 'tradstyle2' when they are both defined and 'tradstyle1' is not" in new Fixture {
          val payeWithTradingStyle2and3 = SamplePayeUnitWithAllFields.copy(tradingStyle =
            Some(TradingStyle2Cell.value + TradingStyle3Cell.value))
          val noTradingStyle1 = AllCells.filterNot(cell => cell.column == TradingStyle1Cell.column)

          underTest.fromRow(samplePayeRowWith(noTradingStyle1)) shouldBe Some(payeWithTradingStyle2and3)
        }

        // concatenation of all 3 tradingStyle columns is covered by the 'all columns' scenario
      }

      "when lifespan is only partially populated" in new Fixture {
        val cellsIncludingPartialLifespan = MandatoryCells ++ Seq(Name1Cell, BirthDateCell)
        val payeWithIncompleteLifespan = SamplePayeUnitWithOnlyMandatoryFields.copy(name = Name1Cell.value,
          lifespan = Some(Lifespan(
            birthDate = BirthDateCell.value,
            deathDate = None,
            deathCode = None))
        )

        underTest.fromRow(samplePayeRowWith(cellsIncludingPartialLifespan)) shouldBe Some(payeWithIncompleteLifespan)
      }

      "when jobs is only partially populated" in new Fixture {
        val cellsIncludingPartialJobs = MandatoryCells ++ Seq(Name1Cell, SepJobsCell, LastUpdatedJobsCell)
        val payeWithIncompleteJobs = SamplePayeUnitWithOnlyMandatoryFields.copy(name = Name1Cell.value,
          jobs = Some(Jobs(
            mar = None,
            jun = None,
            sep = Some(SepJobsCell.value.toInt),
            dec = None,
            lastUpdated = Some(LastUpdatedJobsCell.value)
          ))
        )

        underTest.fromRow(samplePayeRowWith(cellsIncludingPartialJobs)) shouldBe Some(payeWithIncompleteJobs)
      }

      "when employee splits is only partially populated" in new Fixture {
        val cellsIncludingPartialEmployeeSplits = MandatoryCells ++ Seq(Name1Cell, MfullempCell, FfullempCell)
        val payeWithIncompleteEmployeeSplits = SamplePayeUnitWithOnlyMandatoryFields.copy(name = Name1Cell.value,
          employeeSplits = Some(EmployeeSplits(
            mfullemp = Some(MfullempCell.value.toInt),
            msubemp = None,
            ffullemp = Some(FfullempCell.value.toInt),
            fsubemp = None,
            unclemp = None,
            unclsubemp = None
          ))
        )

        underTest.fromRow(samplePayeRowWith(cellsIncludingPartialEmployeeSplits)) shouldBe Some(payeWithIncompleteEmployeeSplits)
      }
    }

    "cannot create a PAYE admin unit" - {
      "when any mandatory column is missing" in new Fixture {
        MandatoryColumns.foreach { columnName =>
          withClue(s"with missing column [$columnName]") {
            val withMissingColumn = samplePayeRowWith(AllCells.filterNot(_.column == columnName))

            underTest.fromRow(withMissingColumn) shouldBe None
          }
        }
      }

      "when none of the name columns are defined" in new Fixture {
        val nameColumns = Set(Name1Cell, Name2Cell, Name3Cell).map(_.column)
        val withMissingName = samplePayeRowWith(AllCells.filterNot(cell => nameColumns.contains(cell.column)))

        underTest.fromRow(withMissingName) shouldBe None
      }

      "when any numeric column" - {
        "contains a non-numeric value" in new Fixture {
          NumericColumns.foreach { columnName =>
            withClue(s"with a non-numeric value for column [$columnName]") {
              val badCell = HBaseCell(column = columnName, value = "not-a-number")
              val withBadCell = samplePayeRowWith(AllCells.filterNot(_.column == columnName) :+ badCell)

              underTest.fromRow(withBadCell) shouldBe None
            }
          }
        }

        "contains a non-integral value" in new Fixture {
          NumericColumns.foreach { columnName =>
            withClue(s"with a non-integral value for column [$columnName]") {
              val badCell = HBaseCell(column = columnName, value = "3.14159")
              val withBadCell = samplePayeRowWith(AllCells.filterNot(_.column == columnName) :+ badCell)

              underTest.fromRow(withBadCell) shouldBe None
            }
          }
        }
      }
    }
  }
}

/*
 * Because we need to test that we can correctly concatenate name & tradingStyle fields from multiple HBase cells,
 * we explicitly define the individual cells here.  This implies that for consistency reasons, we cannot reference
 * the standard SamplePaye without adjusting for this.
 */
private object PayeHBaseRowMapperSpec {
  import uk.gov.ons.br.paye.test.SamplePaye
  import uk.gov.ons.br.paye.test.SamplePaye.Values

  val PayerefCell = HBaseCell(Columns.payeref, Values.Payeref)
  val Name1Cell = HBaseCell(Columns.name1, value = "Big Box")
  val Name2Cell = HBaseCell(Columns.name2, value = " Cereal")
  val Name3Cell = HBaseCell(Columns.name3, value = " Limited")
  val TradingStyle1Cell = HBaseCell(Columns.tradingStyle1, value = "Big")
  val TradingStyle2Cell = HBaseCell(Columns.tradingStyle2, value = " Box")
  val TradingStyle3Cell = HBaseCell(Columns.tradingStyle3, value = " Cereal")
  val LegalStatusCell = HBaseCell(Columns.legalStatus, Values.LegalStatus)
  val StcCell = HBaseCell(Columns.stc, Values.Stc)
  val PreviousPayerefCell = HBaseCell(Columns.previousPayeref, Values.PreviousPayeref)
  val EmployerCategoryCell = HBaseCell(Columns.employerCategory, Values.EmployerCategory)
  val ActionDateCell = HBaseCell(Columns.actionDate, Values.ActionDate)
  val BirthDateCell = HBaseCell(Columns.Lifespan.birthDate, Values.BirthDate)
  val DeathDateCell = HBaseCell(Columns.Lifespan.deathDate, Values.DeathDate)
  val DeathCodeCell = HBaseCell(Columns.Lifespan.deathCode, Values.DeathCode)
  val MarJobsCell = HBaseCell(Columns.Jobs.mar, Values.MarJobs.toString)
  val JunJobsCell = HBaseCell(Columns.Jobs.jun, Values.JunJobs.toString)
  val SepJobsCell = HBaseCell(Columns.Jobs.sep, Values.SepJobs.toString)
  val DecJobsCell = HBaseCell(Columns.Jobs.dec, Values.DecJobs.toString)
  val LastUpdatedJobsCell = HBaseCell(Columns.Jobs.lastUpdated, Values.LastUpdatedJobs)
  val MfullempCell = HBaseCell(Columns.EmployeeSplits.mfullemp, Values.Mfullemp.toString)
  val MsubempCell = HBaseCell(Columns.EmployeeSplits.msubemp, Values.Msubemp.toString)
  val FfullempCell = HBaseCell(Columns.EmployeeSplits.ffullemp, Values.Ffullemp.toString)
  val FsubempCell = HBaseCell(Columns.EmployeeSplits.fsubemp, Values.Fsubemp.toString)
  val UnclempCell = HBaseCell(Columns.EmployeeSplits.unclemp, Values.Unclemp.toString)
  val UnclsubempCell = HBaseCell(Columns.EmployeeSplits.unclsubemp, Values.Unclsubemp.toString)
  val AddressLine1Cell = HBaseCell(Columns.Address.line1, Values.AddressLine1)
  val AddressLine2Cell = HBaseCell(Columns.Address.line2, Values.AddressLine2)
  val AddressLine3Cell = HBaseCell(Columns.Address.line3, Values.AddressLine3)
  val AddressLine4Cell = HBaseCell(Columns.Address.line4, Values.AddressLine4)
  val AddressLine5Cell = HBaseCell(Columns.Address.line5, Values.AddressLine5)
  val PostcodeCell = HBaseCell(Columns.Address.postcode, Values.Postcode)
  val UbrnCell = HBaseCell(Columns.Links.ubrn, Values.Ubrn)
  val UnusedCell = HBaseCell(column = HBaseColumn.name(HBaseColumn(family = "cf", qualifier = "unused")), value = "unused")

  // we generate the Seq via a Set to guarantee that we do not have any cell ordering dependency
  val AllCells = Set(
    PayerefCell, Name1Cell, Name2Cell, Name3Cell, TradingStyle1Cell, TradingStyle2Cell, TradingStyle3Cell,
    LegalStatusCell, StcCell, PreviousPayerefCell, EmployerCategoryCell, ActionDateCell, BirthDateCell,
    DeathDateCell, DeathCodeCell, MarJobsCell, JunJobsCell, SepJobsCell, DecJobsCell, LastUpdatedJobsCell,
    MfullempCell, MsubempCell, FfullempCell, FsubempCell, UnclempCell, UnclsubempCell, AddressLine1Cell,
    AddressLine2Cell, AddressLine3Cell, AddressLine4Cell, AddressLine5Cell, PostcodeCell, UbrnCell
  ).toSeq

  // name is a mandatory field - but because it is derived from multiple columns it is not included here
  val MandatoryColumns = Set(Columns.payeref, Columns.legalStatus, Columns.Address.line1, Columns.Address.postcode)

  // at least one of the name cells must be added to this in order to generate a valid Paye
  val MandatoryCells = AllCells.filter { cell => MandatoryColumns.contains(cell.column) }

  val NumericColumns = Set(Columns.Jobs.mar, Columns.Jobs.jun, Columns.Jobs.sep, Columns.Jobs.dec,
    Columns.EmployeeSplits.mfullemp, Columns.EmployeeSplits.msubemp, Columns.EmployeeSplits.ffullemp,
    Columns.EmployeeSplits.fsubemp, Columns.EmployeeSplits.unclemp, Columns.EmployeeSplits.unclsubemp)

  val SamplePayeUnitWithAllFields = SamplePaye.SamplePayeUnitWithAllFields.copy(
    name = "Big Box Cereal Limited", tradingStyle = Some("Big Box Cereal"))
}
