package com.databius.notebook.spark

import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession

object HelloWorld {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("appName").setMaster("local[*]") //.set("com.databius.notebook.spark.driver.host", "127.0.0.1")
    val spark = SparkSession.builder
      .config(conf)
      .getOrCreate()

    spark.sparkContext.parallelize(List.range(0, 1024)).foreach(println)
  }
}
