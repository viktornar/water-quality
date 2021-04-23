package com.github.viktornar.wq

import org.apache.spark.sql.SparkSession

object WaterQualityApp {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession
      .builder
      .appName("WaterQualityApp")
      .getOrCreate()

    if (args.length < 1) {
      print("Usage: WaterQualityApp <file_dataset>")
      sys.exit(1)
    }

    val datasetFile = args(0)
  }
}
