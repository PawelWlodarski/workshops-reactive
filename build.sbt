name := """jugreactive"""

version := "1.0"

scalaVersion := "2.11.8"

lazy val akkaVersion = "2.5.2"

val akka="com.typesafe.akka" %% "akka-actor" % akkaVersion
val akkaTest="com.typesafe.akka" %% "akka-testkit" % akkaVersion
val akkaStream="com.typesafe.akka" %% "akka-stream" % akkaVersion
val akkaStreamTestKit="com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % "test"
val akkaStreamsTest="com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % "test"
val akkaTyped="com.typesafe.akka" % "akka-typed-experimental_2.11" % "2.4.11"

val cats = "org.typelevel" %% "cats" % "0.8.0"

val scalaTest="org.scalatest" %% "scalatest" % "3.0.0" % "test"

libraryDependencies ++= Seq(
  akka,akkaTest,akkaStream,akkaStreamsTest,akkaTyped,akkaStreamTestKit,
  cats,
  scalaTest
)



parallelExecution in Test := false

//scalacOptions in ThisBuild ++= Seq("-Xfatal-warnings","-unchecked", "-deprecation")
