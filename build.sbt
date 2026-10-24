resolvers += Resolver.mavenLocal
resolvers += "Mobile Mind" at "https://raw.githubusercontent.com/mobilemindtech/m2/master"

import sbt.*
import Keys.*
import Versions.*

scalaVersion := "3.10.0"
organization := "br.com.mobilemind"

version := "0.0.3"

val toolsjs = (project in file("."))
  .enablePlugins(ScalaJSPlugin)
  .settings(
    name := "toolsjs",
    libraryDependencies ++= Seq(
      "com.raquo" %% "laminar" % laminarVersion,
      "org.querki" %% "jquery-facade" % jqueryVersion,
      "org.querki" %% "bootstrap-datepicker-facade" % jqueryDatepickerVersion,
      "com.lihaoyi" %% "upickle" % upickleVersion,
      "dev.laminext" %% "fetch" % fetchVersion,
      "io.github.cquiroz" %% "scala-java-time" % javaTimeVersion,
      "io.github.cquiroz" %% "scala-java-time-tzdb" % javaTimeVersion
    )
  )
