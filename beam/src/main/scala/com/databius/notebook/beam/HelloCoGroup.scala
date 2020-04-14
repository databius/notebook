package com.databius.notebook.beam

import org.apache.beam.sdk.Pipeline
import org.apache.beam.sdk.options.PipelineOptionsFactory
import org.apache.beam.sdk.transforms.DoFn.ProcessElement
import org.apache.beam.sdk.transforms.join.{
  CoGbkResult,
  CoGroupByKey,
  KeyedPCollectionTuple
}
import org.apache.beam.sdk.transforms.{Create, DoFn, ParDo}
import org.apache.beam.sdk.values.{KV, PCollection, TupleTag}

import scala.collection.JavaConverters._

object HelloCoGroup extends App {
//  private val LOG: Logger = LoggerFactory.getLogger(this.getClass)

  val options = PipelineOptionsFactory
    .fromArgs(args: _*)
    .withValidation()
    .as(classOf[WordCountOptions])

  val pipeline: Pipeline = Pipeline.create(options)
  val m = List
    .range(0, 16)
  val c1 = pipeline
    .apply("Create from List",
           Create.of(m.map(i => KV.of(i % 2, s"This is $i")).asJava))
  val c2 = pipeline
    .apply("Create from List",
           Create.of(m.map(i => KV.of(i % 2, s"No, This is $i % 2")).asJava))

  final val tag1 = new TupleTag()
  val d: KeyedPCollectionTuple[Int] = KeyedPCollectionTuple
    .of("tag1", c1)
    .and("tag2", c2)
  val e: PCollection[KV[Int, CoGbkResult]] = d.apply(CoGroupByKey.create())
  e.apply(
    "",
    ParDo.of(new PrintCoGbkResult)
  )

  pipeline.run() //.waitUntilFinish()
}

class PrintCoGbkResult extends DoFn[KV[Int, CoGbkResult], Unit] {
  @ProcessElement
  def processElement(c: ProcessContext): Unit = {
    val k = c.element().getKey
    val v1 = c.element().getValue.getAll("tag1")
    val v2 = c.element().getValue.getAll("tag2")
    println(s"$k -> $v1 | $v2")
  }
}
