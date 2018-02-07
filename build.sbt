name := """jugreactive"""

version := "1.0"

scalaVersion := "2.12.4"

lazy val akkaVersion = "2.5.9"

val akka="com.typesafe.akka" %% "akka-actor" % akkaVersion
val akkaTest="com.typesafe.akka" %% "akka-testkit" % akkaVersion
val akkaStream="com.typesafe.akka" %% "akka-stream" % akkaVersion
val akkaStreamTestKit="com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % "test"
val akkaStreamsTest="com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % "test"
val akkaTyped="com.typesafe.akka" %% "akka-typed" % "2.5.8"

val cats = "org.typelevel" %% "cats-core" % "1.0.1"

val scalaTest="org.scalatest" %% "scalatest" % "3.0.4" % "test"

libraryDependencies ++= Seq(
  akka,akkaTest,akkaStream,akkaStreamsTest,akkaTyped,akkaStreamTestKit,
  cats,
  scalaTest
)



parallelExecution in Test := false

resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"

//scalacOptions in ThisBuild ++= Seq("-Xfatal-warnings","-unchecked", "-deprecation")
