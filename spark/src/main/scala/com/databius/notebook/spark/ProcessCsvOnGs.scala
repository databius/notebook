package com.databius.notebook.spark

import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession

object ProcessCsvOnGs {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf()
      .setAppName("appName")
      .setMaster("local[*]")
      //.set("spark.driver.host", "127.0.0.1")
      .set("spark.sql.caseSensitive", "true")
      .set("spark.hadoop.google.cloud.auth.service.account.enable", "true")
      .set(
        "spark.hadoop.google.cloud.auth.service.account.json.keyfile",
        "/home/peter.nguyen/.config/gcloud/legacy_credentials/peter.nguyen@grasshopperasia.com/adc.json"
      )
    val spark = SparkSession.builder
      .config(conf)
      .getOrCreate()

    val df = spark.read
      .option("header", "true")
//      .csv("gs://ghpr-corvil-us-central-2464306402/chimgmt-cne01/2020-03-02/2020-03-02_095500_md.csv")
      .csv("/home/peter.nguyen/Downloads/2020-03-31_035500_md.csv")

    df.show()
    println(s"Total rows: ${df.count()}")

    spark.stop()
  }
}
