package com.databius.notebook.scalaz.rws

import scalaz.Scalaz.Id
import scalaz.{RWS, ReaderWriterState}
import scalaz._
import Scalaz._

object RWSExample extends App {
  case class Config(port: Int)

  def log[R, S](msg: String): ReaderWriterState[R, List[String], S, Unit] =
    ReaderWriterStateT {
      case (r, s) => (msg.format(r, s) :: Nil, (), s).point[Id]
    }

  def invokeService: ReaderWriterState[Config, List[String], Int, Int] =
    ReaderWriterStateT {
      case (cfg, invocationCount) =>
        (List("Invoking service with port " + cfg.port),
         scala.util.Random.nextInt(100),
         invocationCount + 1).point[Id]
    }

  val program: RWS[Config, List[String], Int, Int] = for {
    _   <- log("Start - r: %s, s: %s")
    res <- invokeService
    _   <- log("Between - r: %s, s: %s")
    _   <- invokeService
    _   <- log("Done - r: %s, s: %s")
  } yield res

  val (logMessages, result, invocationCount) = program run (Config(443), 0)
  println("Result: " + result)
  println("Service invocations: " + invocationCount)
  println("Log: %n%s".format(logMessages.mkString("\t", "%n\t".format(), "")))
}
