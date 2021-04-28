package com.github.viktornar.wq

import com.github.mrpowers.spark.fast.tests.DataFrameComparer
import org.scalatest.funspec.AnyFunSpec

import java.io.{File, IOException}
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.{FileVisitResult, Files, Paths, SimpleFileVisitor}

class WaterQualityProducerSpec extends AnyFunSpec
  with DataFrameComparer
  with SparkContextTestWrapper {

  import WaterQualityProducer._

  it("should read csv test file with 19 records") {
    val dataFrame = readCSV(spark, "data/initial/Waterbase_Test.csv")
    val count = dataFrame.count()
    assert(count == 19)
  }

  it("should write to avro") {
    val dataFrame = readCSV(spark, "data/initial/Waterbase_Test.csv")
    writeCSVToAvro("data/target/Waterbase_Test.avro", dataFrame)
    val path = Paths.get("data/target/Waterbase_Test.avro")
    assert(Files.exists(path))

    delete(path.toFile)
  }

  def delete(file: File): Array[(String, Boolean)] = {
    Option(file.listFiles).map(_.flatMap(f => delete(f))).getOrElse(Array()) :+ (file.getPath -> file.delete)
  }
}
