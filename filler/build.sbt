ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.10"

lazy val root = (project in file("."))
  .settings(
    name := "filler",
//    assemblyPackageScala / assembleArtifact := false,
//    assemblyPackageDependency / assembleArtifact := false,
  )

libraryDependencies ++= Seq(
  "com.rometools" % "rome" % "1.18.0",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5", // log
  "ch.qos.logback" % "logback-classic" % "1.4.5", // log
  "com.lihaoyi" %% "upickle" % "1.6.0", // file-io

  "net.liftweb" %% "lift-json" % "3.5.0", // json
  "org.postgresql" % "postgresql" % "42.5.1", // spark + postgresql
  "org.apache.spark" %% "spark-sql" % "3.2.3" % "provided", // spark
  "org.apache.spark" %% "spark-core" % "3.2.3" % "provided", //
)

// ! useless
//unmanagedJars in Compile += file("./jarlib")

ThisBuild / assemblyMergeStrategy := {
//  case PathList("about.html") => MergeStrategy.rename
//  case PathList(ps@_*) if ps.last endsWith "StaticMDCBinder.class" => MergeStrategy.first
  case PathList("module-info.class") => MergeStrategy.discard
  case x if x.endsWith("/module-info.class") => MergeStrategy.discard
  case x =>
    val oldStrategy = (ThisBuild / assemblyMergeStrategy).value
    oldStrategy(x)
}

//assemblyMergeStrategy in assembly := {
//  case x if x.contains("io.netty") => MergeStrategy.discard
//  case x =>
//    val oldStrategy = (assemblyMergeStrategy in assembly).value
//    oldStrategy(x)
//}

//assemblyMergeStrategy in assembly := {
//  case x if x.contains("netty") => MergeStrategy.first
//  case x if x.contains("SimpleLog") => MergeStrategy.first
//  case x if x.contains("NoOpLog") => MergeStrategy.first
//  case x =>
//    if (x.toString.contains("netty")) println(x.toString)
////    print(x.toString)
//      val oldStrategy = (assemblyMergeStrategy in assembly).value
//      oldStrategy(x)
//
//}

//assemblyShadeRules in assembly := Seq(
//
//    ShadeRule.rename("com.lihaoyi.**" -> "crdaa.@1")
//  .inLibrary("com.lihaoyi" %% "upickle" % "1.6.0"),
//
//      ShadeRule.rename("ch.qos.logback.**" -> "crdbb.@1")
//  .inLibrary("ch.qos.logback" % "logback-classic" % "1.4.5"),
//
//        ShadeRule.rename("com.typesafe.**" -> "crdcc.@1")
//  .inLibrary("com.typesafe.scala-logging" %% "scala-logging" % "3.9.5")  ,
//
//  ShadeRule.rename("org.apache.spark.spark-sql.**" -> "crddd.@1")
//  .inLibrary("org.apache.spark" %% "spark-sql" % "3.2.2")  ,
//
//   ShadeRule.rename("org.apache.spark.spark-core.**" -> "crdee.@1")
//  .inLibrary("org.apache.spark" %% "spark-core" % "3.2.2") ,
//
//     ShadeRule.rename("com.rometools.**" -> "crdff.@1")
//  .inLibrary("com.rometools" % "rome" % "1.18.0"),
//
//       ShadeRule.rename("org.postgresql.postgresql.**" -> "crdgg.@1")
//  .inLibrary("org.postgresql" % "postgresql" % "42.5.1") ,
//
//  ShadeRule.rename("net.liftweb.**" -> "crdhh.@1")
//    .inLibrary("net.liftweb" %% "lift-json" % "3.5.0")    ,
//)


//assemblyMergeStrategy in assembly := {
//  case PathList("javax", "servlet", xs@_*) => MergeStrategy.first
//  case PathList(ps@_*) if ps.last endsWith ".html" => MergeStrategy.first
//  case PathList(ps@_*) if ps.last endsWith "StaticLoggerBinder.class" => MergeStrategy.first
//  case PathList(ps@_*) if ps.last endsWith "StaticMDCBinder.class" => MergeStrategy.first
//  case PathList(ps@_*) if ps.last endsWith "StaticMarkerBinder.class" => MergeStrategy.first
//  case PathList(ps@_*) if ps.last endsWith "io.netty.versions.properties" => MergeStrategy.first
//  case PathList(ps@_*) if ps.last endsWith  "io.netty.util.internal.shaded" => MergeStrategy.first
//  case PathList(ps@_*) if ps.last endsWith  "PlatformDependent" => MergeStrategy.first
//  case PathList("io","netty","util","internal","shaded") => MergeStrategy.first
//  case PathList(ps@_*) if ps.last endsWith "BUILD" => MergeStrategy.first
//  case "application.conf" => MergeStrategy.concat
//  case "module-info.class" => MergeStrategy.discard
//  case x =>
////    println(ps@_*)
//    val oldStrategy = (assemblyMergeStrategy in assembly).value
//    oldStrategy(x)
//}