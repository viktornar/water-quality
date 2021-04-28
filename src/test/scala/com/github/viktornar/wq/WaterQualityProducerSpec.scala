package com.github.viktornar.wq

import com.github.mrpowers.spark.fast.tests.DataFrameComparer
import org.apache.spark.sql.functions.col
import org.scalatest.funspec.AnyFunSpec

import java.io.File
import java.nio.file.{Files, Paths}

class WaterQualityProducerSpec extends AnyFunSpec
  with DataFrameComparer
  with SparkContextTestWrapper {

  import WaterQualityProducer._

  describe("Water quality producer app ") {
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

    it("should normalize data frame") {
      val dataFrame = readCSV(spark, "data/initial/Waterbase_Test.csv")
      writeCSVToAvro("data/target/Waterbase_Test.avro", dataFrame)
      val path = Paths.get("data/target/Waterbase_Test.avro")

      val normalizedDataFrame = normalizeDataFrame(dataFrame)

      assert(normalizedDataFrame.columns.contains("country"))
      assert(normalizedDataFrame.columns.contains("year"))
      assert(normalizedDataFrame.columns.contains("samples"))
      assert(normalizedDataFrame.columns.contains("depth"))
    }

    it("should return average samples by country") {
      val dataFrame = readCSV(spark, "data/initial/Waterbase_Test.csv")
      writeCSVToAvro("data/target/Waterbase_Test.avro", dataFrame)
      val path = Paths.get("data/target/Waterbase_Test.avro")

      val normalizedDataFrame = normalizeDataFrame(dataFrame)
      val avgDataFrame = averageSamplesByCountry(normalizedDataFrame)

      val row = avgDataFrame.select(col("country"), col("avg_samples_depth")).first()
      assert(row.get(0) == "UK")
      assert(row.get(1) == 1.0)
    }
  }


  def delete(file: File): Array[(String, Boolean)] = {
    Option(file.listFiles).map(_.flatMap(f => delete(f))).getOrElse(Array()) :+ (file.getPath -> file.delete)
  }
}
