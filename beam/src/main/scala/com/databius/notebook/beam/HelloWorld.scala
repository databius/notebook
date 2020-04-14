package com.databius.notebook.beam

import java.util

import org.apache.beam.sdk.Pipeline
import org.apache.beam.sdk.options.PipelineOptionsFactory
import org.apache.beam.sdk.transforms.DoFn.ProcessElement
import org.apache.beam.sdk.transforms._
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.JavaConverters._

object HelloWorld extends App {
  val options = PipelineOptionsFactory
    .fromArgs(args: _*)
    .withValidation()
    .as(classOf[WordCountOptions])

  val pipeline: Pipeline = Pipeline.create(options)
  val l: util.List[Int] = List.range(0, 16).asJava
  val c = pipeline
    .apply("Create from List", Create.of(l))
    .apply("increase", ParDo.of(new Increase))
    .apply(MapElements.via(new SimpleFunction[Int, String]() {
      override def apply(input: Int): String = {
        input.toString
      }
    }))
  c.apply("println", ParDo.of(new PrintString))

  pipeline.run() //.waitUntilFinish()
}

class Increase extends DoFn[Int, Int] {
  @ProcessElement
  def processElement(c: ProcessContext): Unit = {
    c.output(c.element() + 1)
  }
}

class Double extends DoFn[Int, Int] {
  @ProcessElement
  def processElement(c: ProcessContext): Unit = {
    c.output(c.element() + 1)
  }
}

class Print extends DoFn[Int, Unit] {
  private val LOG: Logger = LoggerFactory.getLogger(this.getClass)
  @ProcessElement
  def processElement(c: ProcessContext): Unit = {
    LOG.info(Thread.currentThread().getId + " -> " + c.element())
  }
}

class PrintString extends DoFn[String, Unit] {
  private val LOG: Logger = LoggerFactory.getLogger(this.getClass)
  @ProcessElement
  def processElement(c: ProcessContext): Unit = {
    LOG.info(" -> " + c.element())
  }
}

class PrintFloat extends DoFn[Float, Unit] {
  private val LOG: Logger = LoggerFactory.getLogger(this.getClass)
  @ProcessElement
  def processElement(c: ProcessContext): Unit = {
    LOG.info(" -> " + c.element())
  }
}
