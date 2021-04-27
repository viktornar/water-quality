package com.github.viktornar.wq

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.avro.functions._
import org.apache.spark.sql.functions._

object WaterQualityConsumer {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession
      .builder
      .appName("WaterQualityConsumer")
      .getOrCreate()

    spark.sparkContext.setLogLevel("ERROR")

    if (args.length < 3) {
      print("Usage: WaterQualityConsumer <kafka_server> <topic> <timeout>")
      sys.exit(1)
    }

    val kafkaServer = args(0)
    val topic = args(1)
    val timeout = Integer.parseInt(args(2))

    val avgSamplesDepthByCountrySchema =
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
      .option("startingOffsets", "latest")
      .load()

    val data = df
      .select(from_avro(col("value"), avgSamplesDepthByCountrySchema)
        .alias("avgSamplesDepthByCountry"))

    val query = data.writeStream
      .outputMode("update")
      .format("console")
      .start()

    query.awaitTermination(timeout * 1000)
  }
}
