addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.6.20")

/*
 * https://github.com/jrudolph/sbt-dependency-graph
 * show ascii dependency tree: sbt dependencyTree
 * show flat list of transitive dependencies: sbt dependencyList
 */
addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.9.2")

/*
 * https://github.com/scoverage/sbt-scoverage
 * run tests with coverage: sbt clean coverage test
 * generate coverage reports to target/scoverage-report: sbt coverageReport
 */
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.5.1")

/*
 * https://github.com/sksamuel/sbt-scapegoat
 * generate static analysis reports to target/scala-2.12/scapegoat-report: sbt scapegoat
 */
addSbtPlugin("com.sksamuel.scapegoat" %% "sbt-scapegoat" % "1.0.9")

/*
 * Kamon (for tracing)
 */
resolvers += Resolver.bintrayIvyRepo("kamon-io", "sbt-plugins")
addSbtPlugin("io.kamon" % "sbt-aspectj-runner-play-2.6" % "1.1.1")
addSbtPlugin("com.lightbend.sbt" % "sbt-javaagent" % "0.1.4")