name := "water-quality"

version := "0.1"

scalaVersion := "2.12.13"

idePackagePrefix := Some("com.github.viktornar.wq")

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "3.1.1",
  "org.apache.spark" %% "spark-sql"  % "3.1.1"
)
