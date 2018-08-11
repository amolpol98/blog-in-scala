name := "blog-in-scala"

version := "0.1"

scalaVersion := "2.12.6"

libraryDependencies ++= {
  val akkaVersion = "2.5.13"
  val akkaHttpVersion = "10.1.1"
  Seq(
    "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-stream" % akkaVersion,
    "com.typesafe.akka" %% "akka-actor"  % akkaVersion
  )
}

enablePlugins(SbtTwirl)
