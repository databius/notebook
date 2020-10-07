package com.databius.notebook.spark

import org.apache.spark.SparkConf
import org.apache.spark.sql.catalyst.expressions.objects.AssertNotNull
import org.apache.spark.sql.{Column, SparkSession}
import org.apache.spark.sql.functions.{col, input_file_name}
import org.apache.spark.sql.types.{
  DoubleType,
  IntegerType,
  LongType,
  StringType,
  StructField,
  StructType
}

object ReadWriteCsvOnlocal extends App {
  val conf = new SparkConf()
    .setAppName(this.getClass.getSimpleName)
    .setMaster("local[*]")
  //      .set("spark.driver.host", "127.0.0.1")
  val spark = SparkSession.builder.config(conf).getOrCreate()

  val customSchema = StructType(
    Array(
      StructField("timestamp", LongType, nullable = true),
      StructField("type", StringType, nullable = true),
      StructField("bid_price0", DoubleType, nullable = true),
      StructField("bid_volume0", IntegerType, nullable = true),
      StructField("bid_price1", DoubleType, nullable = true),
      StructField("bid_volume1", LongType, nullable = true),
      StructField("bid_price2", DoubleType, nullable = true),
      StructField("bid_volume2", LongType, nullable = true),
      StructField("bid_price3", DoubleType, nullable = true),
      StructField("bid_volume3", LongType, nullable = true),
      StructField("bid_price4", DoubleType, nullable = true),
      StructField("bid_volume4", LongType, nullable = true),
      StructField("ask_price0", DoubleType, nullable = true),
      StructField("ask_volume0", LongType, nullable = true),
      StructField("ask_price1", DoubleType, nullable = true),
      StructField("ask_volume1", LongType, nullable = true),
      StructField("ask_price2", DoubleType, nullable = true),
      StructField("ask_volume2", LongType, nullable = true),
      StructField("ask_price3", DoubleType, nullable = true),
      StructField("ask_volume3", LongType, nullable = true),
      StructField("ask_price4", DoubleType, nullable = true),
      StructField("ask_volume4", LongType, nullable = true),
      StructField("limit_down_price", DoubleType, nullable = true),
      StructField("limit_up_price", DoubleType, nullable = true),
      StructField("turnover_value", DoubleType, nullable = true),
      StructField("turnover_volume", LongType, nullable = true),
      StructField("stream_id", LongType, nullable = true),
      StructField("event_time", LongType, nullable = true)
    ))
  val filename: String => String = _.split("/").last
    .replace("_", ":")
  import org.apache.spark.sql.functions.udf
  val filenameUDF = udf(filename)

  val df = spark.read
    .format("csv")
    .option("header", "true") //first line in file has headers
    .option("mode", "DROPMALFORMED")
    .option("nullValue", "nan")
    .schema(customSchema)
    .load(s"./data/*.csv")
    .withColumn("filename", filenameUDF(input_file_name))
    .limit(1024)
    .selectExpr(
      "DATE(FROM_UTC_TIMESTAMP(FROM_UNIXTIME(IFNULL(timestamp, 0)/1000000000), 'Asia/Singapore')) date",
      "timestamp",
      "type",
      "bid_price0",
      "bid_volume0",
      "bid_price1",
      "bid_volume1",
      "bid_price2",
      "bid_volume2",
      "bid_price3",
      "bid_volume3",
      "bid_price4",
      "bid_volume4",
      "ask_price0",
      "ask_volume0",
      "ask_price1",
      "ask_volume1",
      "ask_price2",
      "ask_volume2",
      "ask_price3",
      "ask_volume3",
      "ask_price4",
      "ask_volume4",
      "limit_down_price",
      "limit_up_price",
      "turnover_value",
      "turnover_volume",
      "stream_id",
      "event_time",
      "filename"
    )
    .withColumn("date", new Column(AssertNotNull(col("date").expr)))

  df.createOrReplaceTempView("temp")
  df.printSchema()
  df.show()

  spark
    .sql("SELECT * FROM temp")
    .show()

  df.write
    .format("csv")
    .option("header", "true")
    .mode("overwrite")
    .save("./data/result")
}
