package uk.gov.ons.br.paye.repository.hbase


import org.slf4j.Logger
import uk.gov.ons.br.models.{Address, Lifespan, LinkToLegalUnit}
import uk.gov.ons.br.paye.models.{EmployeeSplits, Jobs, Paye, PayeRef}
import uk.gov.ons.br.repository.Field._
import uk.gov.ons.br.repository.hbase.HBaseRow.asFields
import uk.gov.ons.br.repository.hbase.{HBaseColumn, HBaseRow, HBaseRowMapper}

import scala.util.Try

/*
 * Use the support provided by uk.gov.ons.br.repository.Field to extract & parse fields in a declarative manner.
 *
 * Note that a mapping is not always 1:1.  For example, name & tradingStyle variables are assembled by concatenating
 * multiple database cell values.
 *
 * Note that for optional string fields we use '=' within the body of the for expression (as opposed to a generator
 * '<-') so that we capture the field as an Option value.  By convention we prefix such field values with opt to
 * highlight that they are Option values.  The situation is slightly different when dealing with numeric fields, which
 * return a Try[Option[T]].  We therefore use a generator '<-' so that the for expression is aborted if the value of
 * the field is non-numeric (the Try is a Failure).  Within the body of the for expression, the generated value is now
 * the inner option.
 *
 * Note that in some cases we have a sub-object that consists entirely of optional fields.  If none of the fields are
 * defined we do not want an instance of the sub-object.  The Field helper function 'whenExistsNonEmpty' handles this
 * for us.
 */
private[hbase] object Columns {
  val payeref = columnNameForQualifier("payeref")
  val legalStatus = columnNameForQualifier("legalstatus")
  val previousPayeref = columnNameForQualifier("prevpaye")
  val employerCategory = columnNameForQualifier("employer_cat")
  val stc = columnNameForQualifier("stc")
  val actionDate = columnNameForQualifier("actiondate")
  val name1 = columnNameForQualifier("nameline1")
  val name2 = columnNameForQualifier("nameline2")
  val name3 = columnNameForQualifier("nameline3")
  val tradingStyle1 = columnNameForQualifier("tradstyle1")
  val tradingStyle2 = columnNameForQualifier("tradstyle2")
  val tradingStyle3 = columnNameForQualifier("tradstyle3")

  object Lifespan {
    val birthDate = columnNameForQualifier("birthdate")
    val deathDate = columnNameForQualifier("deathdate")
    val deathCode = columnNameForQualifier("deathcode")
  }

  object Jobs {
    val mar = columnNameForQualifier("mar_jobs")
    val jun = columnNameForQualifier("june_jobs")
    val sep = columnNameForQualifier("sept_jobs")
    val dec = columnNameForQualifier("dec_jobs")
    val lastUpdated = columnNameForQualifier("jobs_lastupd")
  }

  object EmployeeSplits {
    val mfullemp = columnNameForQualifier("mfullemp")
    val msubemp = columnNameForQualifier("msubemp")
    val ffullemp = columnNameForQualifier("ffullemp")
    val fsubemp = columnNameForQualifier("fsubemp")
    val unclemp = columnNameForQualifier("unclemp")
    val unclsubemp = columnNameForQualifier("unclsubemp")
  }

  object Address {
    val line1 = columnNameForQualifier("address1")
    val line2 = columnNameForQualifier("address2")
    val line3 = columnNameForQualifier("address3")
    val line4 = columnNameForQualifier("address4")
    val line5 = columnNameForQualifier("address5")
    val postcode = columnNameForQualifier("postcode")
  }

  object Links {
    val ubrn = HBaseColumn.name(ParentLinkColumn)
  }
}

object PayeHBaseRowMapper extends HBaseRowMapper[Paye] {
  /*
   * Return None if a valid Paye unit cannot be constructed from the HBaseRow.
   */
  override def fromRow(row: HBaseRow)(implicit logger: Logger): Option[Paye] = {
    import Columns._
    val fields = asFields(row)
    for {
      payeref <- mandatoryStringNamed(payeref).apply(fields)
      name <- mandatoryConcatenatedStringFrom(name1, name2, name3).apply(fields)
      optTradingStyle = optionalConcatenatedStringFrom(tradingStyle1, tradingStyle2, tradingStyle3).apply(fields)
      legalStatus <- mandatoryStringNamed(legalStatus).apply(fields)
      optStc = optionalStringNamed(stc).apply(fields)
      optPreviousPayeref = optionalStringNamed(previousPayeref).apply(fields)
      optEmployerCategory = optionalStringNamed(employerCategory).apply(fields)
      optActionDate = optionalStringNamed(actionDate).apply(fields)
      optLifespan = toLifespan(fields)
      optJobs <- tryToJobs(fields).toOption
      optEmployeeSplits <- tryToEmployeeSplits(fields).toOption
      address <- toAddress(fields)
      optLinks = toLinks(fields)
    } yield Paye(
      PayeRef(payeref),
      name,
      optTradingStyle,
      legalStatus,
      optStc,
      optPreviousPayeref,
      optEmployerCategory,
      optActionDate,
      optLifespan,
      optJobs,
      optEmployeeSplits,
      address,
      optLinks
    )
  }

  private def toLifespan(fields: Map[String, String]): Option[Lifespan] = {
    import Columns.Lifespan._
    for {
      birthDate <- optionalStringNamed(birthDate).apply(fields)
      optDeathDate = optionalStringNamed(deathDate).apply(fields)
      optDeathCode = optionalStringNamed(deathCode).apply(fields)
    } yield Lifespan(birthDate, optDeathDate, optDeathCode)
  }

  private def tryToJobs(fields: Map[String ,String])(implicit logger: Logger): Try[Option[Jobs]] = {
    import Columns.Jobs._
    for {
      optMar <- optionalIntNamed(mar).apply(fields)
      optJun <- optionalIntNamed(jun).apply(fields)
      optSep <- optionalIntNamed(sep).apply(fields)
      optDec <- optionalIntNamed(dec).apply(fields)
      optLastUpdated = optionalStringNamed(lastUpdated).apply(fields)
    } yield whenExistsNonEmpty(optMar, optJun, optSep, optDec, optLastUpdated)(Jobs.apply)
  }

  private def tryToEmployeeSplits(fields: Map[String, String])(implicit logger: Logger): Try[Option[EmployeeSplits]] = {
    import Columns.EmployeeSplits._
    for {
      optMfullemp <- optionalIntNamed(mfullemp).apply(fields)
      optMsubemp <- optionalIntNamed(msubemp).apply(fields)
      optFfullemp <- optionalIntNamed(ffullemp).apply(fields)
      optFsubemp <- optionalIntNamed(fsubemp).apply(fields)
      optUnclemp <- optionalIntNamed(unclemp).apply(fields)
      optUnclsubemp <- optionalIntNamed(unclsubemp).apply(fields)
    } yield whenExistsNonEmpty(optMfullemp, optMsubemp, optFfullemp, optFsubemp, optUnclemp, optUnclsubemp)(EmployeeSplits.apply)
  }

  private def toAddress(fields: Map[String, String])(implicit logger: Logger): Option[Address] = {
    import Columns.Address._
    for {
      line1 <- mandatoryStringNamed(line1).apply(fields)
      optLine2 = optionalStringNamed(line2).apply(fields)
      optLine3 = optionalStringNamed(line3).apply(fields)
      optLine4 = optionalStringNamed(line4).apply(fields)
      optLine5 = optionalStringNamed(line5).apply(fields)
      postcode <- mandatoryStringNamed(postcode).apply(fields)
    } yield Address(line1, optLine2, optLine3, optLine4, optLine5, postcode)
  }

  private def toLinks(fields: Map[String, String]): Option[LinkToLegalUnit] = {
    import Columns.Links._
    optionalStringNamed(ubrn).apply(fields).map {
      LinkToLegalUnit(_)
    }
  }
}
