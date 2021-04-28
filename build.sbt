name := "water-quality"

version := "0.1"

scalaVersion := "2.12.13"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "3.1.1",
  "org.apache.spark" %% "spark-sql" % "3.1.1",
  "org.apache.spark" %% "spark-avro" % "3.1.1",
  // Not sure that I need those dependencies when using structured stream
  "org.apache.spark" %% "spark-streaming-kafka-0-10" % "3.1.1",
  "org.apache.spark" %% "spark-sql-kafka-0-10" % "3.1.1",
  "com.github.mrpowers" %% "spark-fast-tests" % "1.0.0" % "test",
  "org.scalatest" %% "scalatest" % "3.1.1" % "test"
)

fork in Test := true
javaOptions ++= Seq("-Xms512M", "-Xmx2048M", "-XX:+CMSClassUnloadingEnabled")
