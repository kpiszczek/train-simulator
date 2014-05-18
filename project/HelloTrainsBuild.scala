import sbt._
import Keys._
import akka.sbt.AkkaKernelPlugin
import akka.sbt.AkkaKernelPlugin.{Dist, outputDirectory, distJvmOptions}

object HelloTrainsBuild extends Build {
  lazy val HelloTrains = Project (
    id = "hello-trains",
    base = file("."),
    settings = defaultSettings ++ AkkaKernelPlugin.distSettings ++ Seq(
        libraryDependencies ++= Dependencies.helloTrains,
        distJvmOptions in Dist := "-Xms256M -Xmx1024M",
        outputDirectory in Dist := file("target/trainsDist")
      )
    )

  lazy val buildSettings = Defaults.defaultSettings ++ Seq(
      organization := "kubap",
      version := "0.1-SNAPSHOT",
      scalaVersion := "2.10.4",
      crossPaths := false,
      organizationName := "Kuba Piszczek",
      organizationHomepage := None
      )
  lazy val defaultSettings = buildSettings ++ Seq(
    resolvers += "Typesafe Simple Repository" at "http://repo.typesafe.com/typesafe/simple/maven-releases/",
    scalacOptions ++= Seq("-encoding", "UTF-8", "-deprecation", "-unchecked"),
    javacOptions ++= Seq("-Xlint:unchecked", "-Xlint:deprecation")
  )
}

object Dependencies {
  import Dependency._
  val helloTrains = Seq(akkaActor, akkaKernel)
}

object Dependency {
  object V {
    val Akka = "2.2.1"
  }

    val commonsCodec = "commons-codec" %"commons-codec"% "1.4"
    val commonsIo = "commons-io" % "commons-io" % "2.0.1"
    val commonsNet = "commons-net" % "commons-net" % "3.1"
    val akkaActor = "com.typesafe.akka" %% "akka-actor" % V.Akka
    val akkaKernel = "com.typesafe.akka" %% "akka-kernel" % V.Akka
}
