import com.typesafe.sbt.packager.docker.{ExecCmd, Cmd}

name := """Feedbacker"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala, JavaAppPackaging)

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
  "com.typesafe.play" %% "anorm" 				        % "2.5.0",
  "com.typesafe.play" %% "play-mailer"          % "5.0.0-M1",
  "org.postgresql"    % "postgresql"            % "9.4-1201-jdbc41",
  "org.mindrot"       % "jbcrypt"               % "0.3m"
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator


dockerCommands := Seq(
  Cmd("FROM", "java:latest"),
  Cmd("WORKDIR", "/opt/docker"),
  Cmd("ADD", "opt", "/opt"),
  ExecCmd("ENTRYPOINT", "bin/feedbacker"),
  ExecCmd("CMD"),
  Cmd("EXPOSE", "8080")
)
