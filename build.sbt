name := """jugreactive"""

version := "1.0"

scalaVersion := "2.12.2"

lazy val akkaVersion = "2.5.2"

val akka="com.typesafe.akka" %% "akka-actor" % akkaVersion
val akkaTest="com.typesafe.akka" %% "akka-testkit" % akkaVersion
val akkaStream="com.typesafe.akka" %% "akka-stream" % akkaVersion
val akkaStreamTestKit="com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % "test"
val akkaStreamsTest="com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % "test"
val akkaTyped="com.typesafe.akka" %% "akka-typed" % akkaVersion

val cats = "org.typelevel" %% "cats" % "0.9.0"

val scalaTest="org.scalatest" %% "scalatest" % "3.0.0" % "test"

libraryDependencies ++= Seq(
  akka,akkaTest,akkaStream,akkaStreamsTest,akkaTyped,akkaStreamTestKit,
  cats,
  scalaTest
)



parallelExecution in Test := false

resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"

//scalacOptions in ThisBuild ++= Seq("-Xfatal-warnings","-unchecked", "-deprecation")
