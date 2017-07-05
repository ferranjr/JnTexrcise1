
name := "exercise1"

version := "1.0"

scalaVersion := "2.12.1"

name := "Exercise1"

libraryDependencies ++= Seq(
  "com.danielasfregola" %% "twitter4s"  % "5.1",
  "org.scalatest" %% "scalatest" % "3.0.1" % Test,
  "org.mockito" % "mockito-core" % "2.8.47" % Test
)