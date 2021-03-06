import scala.sys.process._

name := "blog-in-scala"

version := "0.1"

scalaVersion := "2.12.6"

libraryDependencies ++= {
  val akkaVersion = "2.5.13"
  val akkaHttpVersion = "10.1.1"
  Seq(
    "com.typesafe.akka" %% "akka-stream" % akkaVersion,
    "com.typesafe.akka" %% "akka-actor"  % akkaVersion,
    "com.typesafe.akka" %% "akka-slf4j"  % akkaVersion,
    "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
    "ch.qos.logback" % "logback-classic" % "1.2.3",
    "org.apache.commons" % "commons-pool2" % "2.4.3",
    "com.github.pureconfig" %% "pureconfig" % "0.8.0",
    "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % "test",
    "org.scalatest" %% "scalatest" % "3.0.4" % "test"
  )
}

resourceGenerators.in(Compile) += buildFrontEndResource.init

enablePlugins(SbtTwirl)

val command = if (sys.props("os.name").contains("Windows")) Seq("cmd", "/c") else Seq ("bash", "-c")

lazy val installFrontendLib = taskKey[Unit]("Install front-end lib by using npm") := {
  val task = streams.value
  val npmInstallCmd = command :+ "npm install"
  val npmInstall = Process(npmInstallCmd, sourceDirectory.value / "main/node")
  task.log.info("Installing front-end lib")
  if ((npmInstall ! task.log) != 0) {
    throw new IllegalStateException("Front-end build failed: Cannot install lib from npm")
  }
}

lazy val compileFrontend = taskKey[Unit]("Compile all fron-end resources") := {
  installFrontendLib.init.value
  val task = streams.value
  val webpackCmd = command :+ s"${sourceDirectory.value}/main/node/node_modules/.bin/webpack --bail --config ${sourceDirectory.value}/main/node/webpack/webpack.config.js"
  val webpack = webpackCmd
  task.log.info("Compiling front-end resources")
  if ((webpack ! task.log) != 0) {
    throw new IllegalStateException("Front-end build failed: Compilation failed!")
  }
}

lazy val copyJSEnginePolyfill = taskKey[Unit]("Copy polyfill for Nashorn javascript engine") := {
  val source = sourceDirectory.value / "main/frontend/js/polyfill"
  IO.copyDirectory(source, sourceDirectory.value / "main/webapp/js/polyfill")
}

lazy val buildFrontEndResource = taskKey[Seq[File]]("Generate front-end resources") := {
  compileFrontend.init.value
  copyJSEnginePolyfill.init.value
  val webapp = sourceDirectory.value / "main" / "webapp"
  val managed = resourceManaged.value
  for {
    (from, to) <- webapp ** "*" pair Path.rebase(webapp, managed / "main" / "webapp")
  } yield {
    Sync.copy(from, to)
    to
  }
}
