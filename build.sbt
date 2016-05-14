name := """Feedbacker"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  jdbc,
//  evolutions,
//  cache,
//  ws,
  specs2 % Test
)

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-server"          % "2.5.3",
  "com.typesafe.play" %% "play-json"            % "2.5.3",
  "com.typesafe.play" %% "anorm" 				% "2.5.0",
  "org.postgresql"    % "postgresql" % "9.4-1201-jdbc41"
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator
