package com.databius.notebook.beam

import org.apache.beam.sdk.Pipeline
import org.apache.beam.sdk.options.PipelineOptionsFactory
import org.apache.beam.sdk.transforms.Partition.PartitionFn
import org.apache.beam.sdk.transforms.{
  Create,
  MapElements,
  Partition,
  SimpleFunction
}
import org.apache.beam.sdk.values.{PCollection, PCollectionList}
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.JavaConverters._

object HelloPatition extends App {
  private val LOG: Logger = LoggerFactory.getLogger(this.getClass)

  val options = PipelineOptionsFactory
    .fromArgs(args: _*)
    .withValidation()
    .as(classOf[WordCountOptions])

  val pipeline: Pipeline = Pipeline.create(options)
  val m = List
    .range(0, 10)
    .asJava
  val c: PCollection[Int] = pipeline
    .apply("Create from List", Create.of(m))

  c.apply("Print", MapElements.via(new SimpleFunction[Int, Unit]() {
    override def apply(input: Int): Unit = {
      LOG.info(s"$input")
    }
  }))

  val d: PCollectionList[Int] = c.apply(Partition.of(10, new MyPartitionFn))

  d.get(2)
    .apply("Print", MapElements.via(new SimpleFunction[Int, Unit]() {
      override def apply(input: Int): Unit = {
        LOG.info(s"$input")
      }
    }))

  pipeline.run() //.waitUntilFinish()
}

class MyPartitionFn extends PartitionFn[Int] {
  override def partitionFor(input: Int, numPartitions: Int): Int = {
    input
  }
}
