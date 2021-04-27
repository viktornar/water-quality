package com.github.viktornar.wq

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.avro.functions._

object WaterQualityConsumer {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession
      .builder
      .appName("WaterQualityConsumer")
      .getOrCreate()

    if (args.length < 2) {
      print("Usage: WaterQualityConsumer <kafka_server> <topic>")
      sys.exit(1)
    }

    val kafkaServer = args(0)
    val topic = args(1)

    var averageSamplesDepthByCountrySchema =
      """{
  "type": "record",
  "name": "AvgSamplesByCountry",
  "namespace": "com.github.viktornar",
  "fields": [
    {"name": "country","type": ["string", "null"]},
    {"name": "avg_samples_depth","type": ["double", "null"]}
  ]
}
"""

    val df = spark
      .readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", kafkaServer)
      .option("subscribe", topic)
      .load()

    val data = df
      .select(from_avro(col("value"), averageSamplesDepthByCountrySchema))
      .alias("avgSamplesDepthByCountry")

    data.writeStream
      .outputMode("update")
      .format("console")
      .start()

    spark.streams.awaitAnyTermination(30 * 1000)
  }
}
