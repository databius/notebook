package com.databius.notebook.beam

import org.apache.beam.sdk.Pipeline
import org.apache.beam.sdk.coders.VarIntCoder
import org.apache.beam.sdk.options.PipelineOptionsFactory
import org.apache.beam.sdk.state.{StateSpec, StateSpecs, ValueState}
import org.apache.beam.sdk.transforms.DoFn.{ProcessElement, StateId}
import org.apache.beam.sdk.transforms.join.{
  CoGbkResult,
  CoGroupByKey,
  KeyedPCollectionTuple
}
import org.apache.beam.sdk.transforms.{Create, DoFn, ParDo}
import org.apache.beam.sdk.values.{KV, PCollection, TupleTag}

import scala.collection.JavaConverters._

object Stateful extends App {
  //  private val LOG: Logger = LoggerFactory.getLogger(this.getClass)

  val options =
    PipelineOptionsFactory.fromArgs("").create()

  val pipeline: Pipeline = Pipeline.create(options)
  val m = List
    .range(0, 4)
  val c = pipeline
    .apply(
      "Create from List",
      Create.of(m.map(i => KV.of(new Integer(i % 2), new Integer(i))).asJava))

  c.apply(
    "",
    ParDo.of(new PrintStateful)
  )

  pipeline.run() //.waitUntilFinish()
}

class PrintStateful extends DoFn[KV[Integer, Integer], Unit] {
  @StateId("index")
  val c1State: StateSpec[ValueState[Integer]] =
    StateSpecs.value(VarIntCoder.of())

  @ProcessElement
  def processElement(c: ProcessContext,
                     @StateId("index") index: ValueState[Integer]): Unit = {

    val x: Integer = if (index.read() != null) index.read() else 0
    val y: Integer = c.element().getKey
    val z: Integer = c.element().getValue
    index.write(z)
    println(s"$x -> $y -> $z")
  }
}
