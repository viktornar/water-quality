package com.github.viktornar.wq

import org.scalatest.funspec.AnyFunSpec
import org.apache.spark.sql.types._
import org.apache.spark.sql.functions._
import org.apache.spark.sql.Row
import com.github.mrpowers.spark.fast.tests.DataFrameComparer

// Some test example with udf. Just for POC that it possible to use UNIT testing with spark locally
class NumberFunSpec
  extends AnyFunSpec
    with DataFrameComparer
    with SparkContextTestWrapper {

  import spark.implicits._

  it("appends an is_even column to a Dataframe") {
    val sourceDF = Seq(
      (1),
      (8),
      (12)
    ).toDF("number")

    val actualDF = sourceDF
      .withColumn("is_even", NumberFun.isEvenUDF(col("number")))

    val expectedSchema = List(
      StructField("number", IntegerType, false),
      StructField("is_even", BooleanType, false)
    )

    val expectedData = Seq(
      Row(1, false),
      Row(8, true),
      Row(12, true)
    )

    val expectedDF = spark.createDataFrame(
      spark.sparkContext.parallelize(expectedData),
      StructType(expectedSchema)
    )

    assertSmallDataFrameEquality(actualDF, expectedDF)
  }

  describe(".isEven") {
    it("returns true for even numbers") {
      assert(NumberFun.isEven(4) === true)
    }

    it("returns false for odd numbers") {
      assert(NumberFun.isEven(3) === false)
    }
  }

  object NumberFun {
    def isEven(n: Integer): Boolean = {
      n % 2 == 0
    }

    val isEvenUDF = udf[Boolean, Integer](isEven)
  }
}
