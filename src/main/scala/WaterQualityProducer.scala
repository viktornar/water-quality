package com.github.viktornar.wq

import org.apache.spark.sql._
import org.apache.spark.sql.functions._

import java.io._

object WaterQualityProducer {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession
      .builder
      .appName("WaterQualityApp")
      .getOrCreate()

    if (args.length < 4) {
      print("Usage: WaterQualityApp <input_dataset> <output_dataset> <kafka_server> <topic>")
      sys.exit(1)
    }

    val inputDataset = args(0)
    val outputDataset = args(1)
    val kafkaServer = args(2)
    val topic = args(3)

    val waterCSVDataFrame = spark
      .read
      .options(Map("inferSchema" -> "true", "delimiter" -> ",", "header" -> "true"))
      .csv(s"$inputDataset")

    writeCSVToAvro(outputDataset, waterCSVDataFrame)

    val waterAvroDataFrame = spark
      .read
      .format("avro")
      .load(s"$outputDataset")

    val avgSamples = averageSamplesByCountry(
      normalizeDataFrame(waterAvroDataFrame)
    )

    saveToKafkaTopic(kafkaServer, topic, avgSamples)
  }

  private def averageSamplesByCountry(normalizedWaterDataFrame: DataFrame, startYear: Int = 2011): Dataset[Row] = {
    val avgSamplesByCountry = normalizedWaterDataFrame
      .filter(col("year") > startYear && col("samples").isNotNull)
      .groupBy("country")
      .agg(avg("depth").as("avg_samples_depth"))
      .sort("country")
    avgSamplesByCountry
  }

  private def normalizeDataFrame(waterAvroDataFrame: DataFrame) = {
    val normalizedWaterDataFrame = waterAvroDataFrame
      .selectExpr(
        "substr(monitoringSiteIdentifier, 0, 2) AS country",
        "phenomenonTimeReferenceYear AS year",
        "parameterSamplingPeriod AS period",
        "resultNumberOfSamples AS samples",
        "parameterSampleDepth AS depth",
        "resultUom AS uom",
        "resultMinimumValue AS min",
        "resultMeanValue AS mean",
        "resultMaximumValue AS max",
        "resultStandardDeviationValue AS std",
        "resultObservationStatus as status",
        "Remarks as remarks")
    normalizedWaterDataFrame
  }

  private def writeCSVToAvro(outputDataset: String, dataFrame: DataFrame): Unit = {
    dataFrame
      .write
      .format("avro")
      .mode(SaveMode.Overwrite)
      .save(s"$outputDataset")

    val schemaToWrite = s"${outputDataset.split("\\.")(0)}.avsc"
    val waterJsonSchema = dataFrame.schema.json
    val writer = new PrintWriter(new File(s"$schemaToWrite"))

    writer.write(waterJsonSchema)
    writer.close()
  }

  private def saveToKafkaTopic(kafkaServer: String, topic: String, avgSamples: Dataset[Row]): Unit = {
    avgSamples
      .selectExpr("CAST(avg_samples_depth AS STRING) as value")
      .write
      .format("kafka")
      .option("kafka.bootstrap.servers", kafkaServer)
      .option("topic", topic)
      .save()
  }
}
