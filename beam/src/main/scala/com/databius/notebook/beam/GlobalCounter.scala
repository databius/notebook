package com.databius.notebook.beam

import java.util

import com.databius.notebook.beam.HelloWorld.pipeline
import org.apache.beam.sdk.Pipeline
import org.apache.beam.sdk.options.PipelineOptionsFactory
import org.apache.beam.sdk.transforms.DoFn.ProcessElement
import org.apache.beam.sdk.transforms.{
  Count,
  Create,
  DoFn,
  MapElements,
  ParDo,
  SimpleFunction
}
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.JavaConverters._

object GlobalCounter extends App {

  val options = PipelineOptionsFactory
    .fromArgs(args: _*)
    .withValidation()
    .as(classOf[WordCountOptions])

  val pipeline: Pipeline = Pipeline.create(options)
  val l = List.range(0, 1000000).map(_.toString).asJava
  pipeline
    .apply("Create from List", Create.of(l))
    .apply("CountRecords trade", Count.globally())
    .apply("", ParDo.of(new Log[java.lang.Long]))

  pipeline.run()
}
