package com.github.viktornar.wq

import org.apache.spark.sql.SparkSession
import org.apache.spark.{SparkConf, SparkContext, sql}


trait SparkContextTestWrapper {
  lazy val spark: sql.SparkSession = {
    val sc: SparkContext = {
      val sparkConfig = new SparkConf()
      sparkConfig.set("spark.broadcast.compress", "false")
      sparkConfig.set("spark.shuffle.compress", "false")
      sparkConfig.set("spark.shuffle.spill.compress", "false")
      new SparkContext("local[2]", "TestApp", sparkConfig)
    }
    sc.setLogLevel("ERROR")
    SparkSession.builder.config(sc.getConf).getOrCreate()
  }
}
