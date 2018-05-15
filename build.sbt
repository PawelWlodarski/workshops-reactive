name := """jugreactive"""

version := "1.0"

scalaVersion := "2.12.4"

val akkaVersion = "2.5.11"
val slickVersion = "3.2.3"

val akka="com.typesafe.akka" %% "akka-actor" % akkaVersion
val akkaTest="com.typesafe.akka" %% "akka-testkit" % akkaVersion
val akkaStream="com.typesafe.akka" %% "akka-stream" % akkaVersion
val akkaStreamTestKit="com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % "test"
val akkaStreamsTest="com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % "test"
val akkaTyped="com.typesafe.akka" %% "akka-actor-typed" % akkaVersion
val akkaTypedTest = "com.typesafe.akka" %% "akka-testkit-typed" % akkaVersion % Test
val akkaPersistence = "com.typesafe.akka" %% "akka-persistence" % akkaVersion

val levelDBJournal = "org.fusesource.leveldbjni" % "leveldbjni-all" % "1.8"


val cats = "org.typelevel" %% "cats-core" % "1.0.1"

val scalaTest="org.scalatest" %% "scalatest" % "3.0.4" % "test"

val slick = Seq(
  "com.typesafe.slick" %% "slick" % slickVersion,
  "com.typesafe.slick" %% "slick-codegen" % slickVersion,
  "com.typesafe.slick" %% "slick-hikaricp" % slickVersion
)

val h2 = "com.h2database" % "h2" % "1.4.197"

libraryDependencies ++= Seq(
  akka,akkaTest,akkaStream,akkaStreamsTest,akkaStreamTestKit,
  akkaTyped,akkaTypedTest,
  akkaPersistence,levelDBJournal,
  cats,
  scalaTest,
  h2
) ++ slick



parallelExecution in Test := false

resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"

//scalacOptions in ThisBuild ++= Seq("-Xfatal-warnings","-unchecked", "-deprecation")
