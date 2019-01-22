import Dependencies._
import DependencyOverrides._

lazy val root = (project in file(".")).
  enablePlugins(PlayScala, JavaAgent).  // JavaAgent required for Kamon monitoring
  disablePlugins(PlayLayoutPlugin).     // use a conventional sbt / maven project layout
  configs(IntegrationTest).             // adds predefined integration test configuration (it)
  settings(
    inThisBuild(List(
      organization := "uk.gov.ons",
      scalaVersion := "2.12.7",
      version      := "0.1.0-SNAPSHOT"
    )),
    name := "br-paye-api",
    resolvers += Resolver.bintrayRepo("ons", "ONS-Registers"),
    scalacOptions ++= Seq(
      "-target:jvm-1.8",
      "-encoding", "utf8",
      "-deprecation",
      "-feature",
      "-unchecked",
      "-Xcheckinit",
      "-Xlint:_",
      "-Xfatal-warnings",
      "-Yno-adapted-args",
      "-Ywarn-dead-code",
      "-Ywarn-value-discard",
      "-Ywarn-unused",
      "-P:silencer:pathFilters=[.*/Routes.scala]"
    ),
    coverageExcludedPackages := "router\\.*;" +
      ".*\\.controllers\\.javascript\\.*;" +
      ".*\\.controllers\\.Reverse.*Controller;" +
      "uk\\.gov\\.ons\\.br\\.paye\\.modules\\.*",
    scapegoatVersion in ThisBuild := "1.3.8",
    scapegoatIgnoredFiles := Seq(".*/Routes.scala", ".*/ReverseRoutes.scala", ".*/JavaScriptReverseRoutes.scala"),
    // configure IntegrationTest
    // note that we specify a custom application.conf file, and javaOptions only takes effect if fork is true
    Defaults.itSettings,
    fork in IntegrationTest := true,
    javaOptions in IntegrationTest += "-Dconfig.resource=it_application.conf",
    libraryDependencies ++= Seq(
      compilerPlugin(silencerPlugin),
      guice,
      kamonLogback,           // traceId in Mapped Diagnostic Context (MDC)
      kamonPlay,              // monitoring Play
      kamonZipkin,            // reporter
      registersApi,
      silencerLib % Provided,
      ws,
      
      // test dependencies
      registersApiTest % s"$Test,$IntegrationTest",
      scalaMock % s"$Test,$IntegrationTest",
      scalaTest % Test,
      scalaTestPlus % Test,
      wireMock % IntegrationTest
    ),
    dependencyOverrides ++= Seq(
      // ensure jetty is the version required by wiremock
      jettyHttp,
      jettyIo,
      jettyUtil
    ),
    // Kamon monitoring (requires JavaAgent plugin - see enablePlugins)
    javaAgents += "org.aspectj" % "aspectjweaver" % "1.9.2",
    javaOptions in Universal += "-Dorg.aspectj.tracing.factory=default"
  )