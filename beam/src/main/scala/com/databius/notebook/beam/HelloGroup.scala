package com.databius.notebook.beam

import java.lang

import org.apache.beam.sdk.Pipeline
import org.apache.beam.sdk.options.PipelineOptionsFactory
import org.apache.beam.sdk.transforms.{
  Create,
  GroupByKey,
  MapElements,
  SimpleFunction
}
import org.apache.beam.sdk.values.{KV, PCollection}
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.JavaConverters._

object HelloGroup extends App {
  private val LOG: Logger = LoggerFactory.getLogger(this.getClass)

  val options = PipelineOptionsFactory
    .fromArgs(args: _*)
    .withValidation()
    .as(classOf[WordCountOptions])

  val pipeline: Pipeline = Pipeline.create(options)
  val m = List
    .range(0, 16)
    .map(i => KV.of(i % 2, s"This is $i"))
    .asJava
  val c = pipeline
    .apply("Create from List", Create.of(m))

  c.apply("Print", MapElements.via(new SimpleFunction[KV[Int, String], Unit]() {
    override def apply(input: KV[Int, String]): Unit = {
      LOG.info(s"$input")
    }
  }))

  val d: PCollection[KV[Int, lang.Iterable[String]]] =
    c.apply(GroupByKey.create())
  d.apply(
    "Print",
    MapElements.via(new SimpleFunction[KV[Int, lang.Iterable[String]], Unit]() {
      override def apply(input: KV[Int, lang.Iterable[String]]): Unit = {
        LOG.info(s"$input")
      }
    })
  )

  pipeline.run() //.waitUntilFinish()
}
