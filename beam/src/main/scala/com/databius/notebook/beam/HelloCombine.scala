package com.databius.notebook.beam

import org.apache.beam.sdk.Pipeline
import org.apache.beam.sdk.options.PipelineOptionsFactory
import org.apache.beam.sdk.transforms._
import org.apache.beam.sdk.values.PCollection
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.JavaConverters._

object HelloCombine extends App {
  private val LOG: Logger = LoggerFactory.getLogger(this.getClass)

  val options = PipelineOptionsFactory
    .fromArgs(args: _*)
    .withValidation()
    .as(classOf[WordCountOptions])

  val pipeline: Pipeline = Pipeline.create(options)
  val m = List
    .range(0, 16)
    .asJava
  val c: PCollection[Int] = pipeline
    .apply("Create from List", Create.of(m))

  c.apply("Print", MapElements.via(new SimpleFunction[Int, Unit]() {
    override def apply(input: Int): Unit = {
      LOG.error(s"c --> $input")
    }
  }))

  val d: PCollection[Int] = c.apply("Combine", Combine.globally(new SumFn))
  d.apply("Print", MapElements.via(new SimpleFunction[Int, Unit]() {
    override def apply(input: Int): Unit = {
      LOG.error(s"d --> $input")
    }
  }))

  pipeline.run() //.waitUntilFinish()
}

class SumFn extends SerializableFunction[java.lang.Iterable[Int], Int] {
  override def apply(input: java.lang.Iterable[Int]): Int = {
    var sum = 0
    for (item <- input.asScala) {
      sum += item
    }
    sum
  }
}
