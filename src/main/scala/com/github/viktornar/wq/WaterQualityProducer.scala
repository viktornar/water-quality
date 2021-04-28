package com.github.viktornar.wq

import org.apache.spark.sql._
import org.apache.spark.sql.avro.functions.to_avro
import org.apache.spark.sql.functions.{avg, col, column, struct}
import java.io.{File, PrintWriter}

object WaterQualityProducer {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession
      .builder
      .appName("WaterQualityProducer")
      .getOrCreate()

    spark.sparkContext.setLogLevel("ERROR")

    if (args.length < 4) {
      print("Usage: WaterQualityProducer <input_dataset> <output_dataset> <kafka_server> <topic>")
      sys.exit(1)
    }

    val inputDataset = args(0)
    val outputDataset = args(1)
    val kafkaServer = args(2)
    val topic = args(3)

    val waterCSVDataFrame: DataFrame = readCSV(spark, inputDataset)

    writeCSVToAvro(outputDataset, waterCSVDataFrame)

    val waterAvroDataFrame = spark
      .read
      .format("avro")
      .load(outputDataset)

    val avgSamples = averageSamplesByCountry(
      normalizeDataFrame(waterAvroDataFrame)
    )

    saveToKafkaTopic(kafkaServer, topic, avgSamples)
  }

  def readCSV(spark: SparkSession, inputDataset: String): DataFrame = {
    val waterCSVDataFrame = spark
      .read
      .options(Map("inferSchema" -> "true", "delimiter" -> ",", "header" -> "true"))
      .csv(inputDataset)
    waterCSVDataFrame
  }

  def averageSamplesByCountry(normalizedWaterDataFrame: DataFrame, startYear: Int = 2011): Dataset[Row] = {
    val avgSamplesByCountry = normalizedWaterDataFrame
      .filter(col("year") > startYear && col("depth").isNotNull)
      .groupBy("country")
      .agg(avg("depth").as("avg_samples_depth"))
      .sort(col("country"))
    avgSamplesByCountry
  }

  def normalizeDataFrame(waterAvroDataFrame: DataFrame): DataFrame = {
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

  def writeCSVToAvro(outputDataset: String, dataFrame: DataFrame): Unit = {
    dataFrame
      .write
      .format("avro")
      .mode(SaveMode.Overwrite)
      .save(outputDataset)

    val schemaToWrite = s"${outputDataset.split("\\.")(0)}.avsc"
    val waterJsonSchema = dataFrame.schema.json
    val writer = new PrintWriter(new File(schemaToWrite))

    writer.write(waterJsonSchema)
    writer.close()
  }

  def saveToKafkaTopic(kafkaServer: String, topic: String, data: Dataset[Row]): Unit = {
    val df = data.toDF()
    df.select(to_avro(struct(df.columns.map(column): _*)).alias("value"))
      .write
      .format("kafka")
      .option("kafka.bootstrap.servers", kafkaServer)
      .option("topic", topic)
      .save()
  }
}
