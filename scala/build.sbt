name := "simplescala_experimentation"

version := "1.0"

scalaVersion := "2.11.11"

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")

libraryDependencies += "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.6"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.0" % "test"
