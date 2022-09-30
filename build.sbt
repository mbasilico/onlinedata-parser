ThisBuild / scalaVersion := "2.13.8"

ThisBuild / version := "1.0-SNAPSHOT"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(
    name := """onlinedata-parser""",
    libraryDependencies ++= Seq(
      guice,
      "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % Test,
      "org.apache.tika" % "tika" %  "1.21",
      "org.apache.tika" % "tika-parsers" %  "1.21",
      "de.l3s.boilerpipe" % "boilerpipe" % "1.1.0",
      "org.typelevel" %% "cats-core" % "2.8.0",
      "org.apache.httpcomponents" % "httpclient" % "4.5.13",
      "net.logstash.logback" % "logstash-logback-encoder" % "7.2",
      "org.joda" % "joda-convert" % "2.2.2",
      "com.fasterxml.jackson.core" % "jackson-core" % "2.13.4",
      "com.fasterxml.jackson.core" % "jackson-databind" % "2.13.4",
      "com.fasterxml.jackson.module" % "jackson-module-scala_2.13" % "2.13.4"

    )
  )
