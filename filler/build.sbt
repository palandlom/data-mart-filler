ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.10"

lazy val root = (project in file("."))
  .settings(
    name := "filler"
  )

libraryDependencies ++= Seq(
  "com.rometools" % "rome" % "1.18.0",
  "org.postgresql" % "postgresql" % "42.5.1",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5", // log
  "ch.qos.logback" % "logback-classic" % "1.4.5", // log
  "com.lihaoyi" %% "upickle" % "1.6.0",  // file-io
  "net.liftweb" %% "lift-json" % "3.5.0", // json
  "org.apache.spark" %% "spark-sql" % "3.2.2", // spark
  "org.apache.spark" %% "spark-core" % "3.2.2", // spark
  "org.postgresql" % "postgresql" % "42.5.1", // spark + postgresql

)


//"com.typesafe.scala-logging" %% "scala-logging" % "3.9.4"
//, // log
//"ch.qos.logback" % "logback-classic" % "1.1.3"
//, // log
