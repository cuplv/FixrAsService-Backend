import Dependencies._

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      // Scalac
      scalacOptions ++= Seq(
        "-language:implicitConversions",
        "-encoding", "UTF-8", // check encoding
        "-feature", // warn on features that should be imported explicitly
        "-explaintypes", // explain type errors in more detail
        "-deprecation", // warn on deprecated APIs
        "-unchecked", // detailed erasure warnings
        "-Xfuture", // future language features
        //"-Xfatal-warnings", // fail on warnings
        //"-Xlint",
        //"-Ywarn-adapted-args", // warn if an argument list is modified to match the receiver
        //"-Ywarn-numeric-widen", // warn when numerics are widened
        "-Ywarn-inaccessible", // warn about inaccessible types in method signatures
        "-Ywarn-infer-any", // warn when a type argument is inferred to be `Any`
        "-Ywarn-nullary-override", // warn when non-nullary `def f()' overrides nullary `def f'
        "-Ywarn-nullary-unit", // warn when nullary methods return Unit
        "-Ywarn-unused", // warn when local and private vals, vars, defs, and types are unused
        "-Ywarn-unused-import", // warn when imports are unused
        "-Ywarn-value-discard" // warn when non-Unit expression results are unused
      ),

      // Scaladoc
      autoAPIMappings := true,

      organization := "edu.colorado.plv",
      scalaVersion := "2.12.1",
      version      := "0.1.0-SNAPSHOT"
    )),

    resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",

    // Dependencies
    libraryDependencies ++= Seq(
      scalaTest % Test,
      scalaCheck % Test,
      "com.typesafe.akka" %% "akka-http-core" % "10.0.4",
      "com.typesafe.akka" %% "akka-http" % "10.0.4",
      "com.typesafe.akka" %% "akka-http-testkit" % "10.0.4",
      "com.typesafe.akka" %% "akka-http-jackson" % "10.0.4",
      "com.typesafe.akka" %% "akka-http-xml" % "10.0.4",
      "org.apache.solr" % "solr-solrj" % "6.4.1",
      "com.google.code.gson" %  "gson" % "2.8.0",
      "com.typesafe.play" %% "play-json" % "2.6.0-M3"
    ),

    // Name
    name := "fixrservice-backend"

  )
