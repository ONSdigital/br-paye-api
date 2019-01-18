import sbt._

object Dependencies {
  private lazy val silencerVersion = "1.3.1"

  lazy val kamonLogback = "io.kamon" %% "kamon-logback" % "1.0.3"
  lazy val kamonPlay = "io.kamon" %% "kamon-play-2.6" % "1.1.1"
  lazy val kamonZipkin = "io.kamon" %% "kamon-zipkin" % "1.0.0"
  lazy val registersApi = "uk.gov.ons" %% "br-api-common" % "1.1"
  lazy val registersApiTest = "uk.gov.ons" %% "br-api-test-common" % "1.2"
  lazy val scalaMock = "org.scalamock" %% "scalamock" % "4.1.0"
  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.0.4"
  lazy val scalaTestPlus = "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2"
  lazy val silencerLib = "com.github.ghik" %% "silencer-lib" % silencerVersion
  lazy val silencerPlugin = "com.github.ghik" %% "silencer-plugin" % silencerVersion
}

/*
 * For explicit override of transitive dependency versions.
 */
object DependencyOverrides {
  // we are not using selenium - favour the jetty components needed by wiremock
  lazy val jettyHttp = "org.eclipse.jetty" % "jetty-http" % "9.2.24.v20180105"
  lazy val jettyIo = "org.eclipse.jetty" % "jetty-io" % "9.2.24.v20180105"
  lazy val jettyUtil = "org.eclipse.jetty" % "jetty-util" % "9.2.24.v20180105"
}