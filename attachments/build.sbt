import sbt._

import Versions._
import Artifactory._

version in ThisBuild := "0.0.1"
scalaVersion in ThisBuild := "2.12.8"
isSnapshot := true

lazy val parent = project
  .in(file("."))
  .settings(
    name := "attachments",
    publishArtifact in (Compile, packageDoc) := false,
    publishArtifact in (Compile, packageSrc) := false
  )
  .aggregate(`scala-codegen`, `application`)

// <doc-ref:modules>
lazy val `scala-codegen` = project
  .in(file("scala-codegen"))
  .settings(
    name := "scala-codegen",
    commonSettings,
    libraryDependencies ++= codeGenDependencies,
  )

lazy val `application` = project
  .in(file("application"))
  .settings(
    name := "application",
    commonSettings,
    assemblyMergeStrategy in assembly := {
     case PathList("META-INF", "MANIFEST.MF") => MergeStrategy.discard
     case "reference.conf" => MergeStrategy.concat
     case x => MergeStrategy.first
    },
    assemblyJarName in assembly := "application.jar",
    mainClass := Some("com.daml.attachments.AttachmentsMain"),
    libraryDependencies ++= codeGenDependencies ++ applicationDependencies,
  )
  .dependsOn(`scala-codegen`)

lazy val `file-client` = project
  .in(file("client"))
  .settings(
    name := "file-client",
    commonSettings,
    assemblyMergeStrategy in assembly := {
      case PathList("META-INF", "MANIFEST.MF") => MergeStrategy.discard
      case "reference.conf" => MergeStrategy.concat
      case x => MergeStrategy.first
    },
    assemblyJarName in assembly := "file-client.jar",
    mainClass := Some("com.daml.files.client.FileClient"),
    libraryDependencies ++= codeGenDependencies ++ applicationDependencies,
  )
  .dependsOn(`scala-codegen`, application)


lazy val commonSettings = Seq(
  scalacOptions ++= Seq(
    "-feature",
    "-target:jvm-1.8",
    "-deprecation",
    "-Xfatal-warnings",
    "-Xsource:2.13",
    "-unchecked",
    "-Xfuture",
    "-Xlint:_,-unused"
  ),
  resolvers ++= daResolvers,
  classpathTypes += "maven-plugin",
)

// <doc-ref:dependencies>
lazy val codeGenDependencies = Seq(
  "com.daml.scala" %% "bindings" % daSdkVersion,
)


val ScalatraVersion = "2.6.+"
lazy val applicationDependencies = Seq(
  "org.scalatra" %% "scalatra" % ScalatraVersion,
  "org.scalatra"            %% "scalatra-scalate"  % ScalatraVersion,
  "org.scalatra" %% "scalatra-json" % ScalatraVersion,
  "org.json4s"   %% "json4s-jackson" % "3.5.2",
  "org.json4s" %% "json4s-native" % "3.6.0",
  "org.eclipse.jetty"       %  "jetty-webapp"      % "9.4.7.v20170914",
  "com.softwaremill.sttp" %% "core" % "1.6.7",
  "com.softwaremill.sttp" %% "json4s" % "1.6.7",
  "ch.qos.logback"          % "logback-classic"    % "1.2.3",
  "com.daml.scala" %% "bindings-akka" % daSdkVersion,
)

