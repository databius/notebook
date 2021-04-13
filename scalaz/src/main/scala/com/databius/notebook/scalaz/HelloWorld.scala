package com.databius.notebook.scalaz

import org.slf4j.{Logger, LoggerFactory}

object HelloWorld {
  def main(args: Array[String]): Unit = {
    val log: Logger = LoggerFactory.getLogger(this.getClass)

    List.range(0, 1024).par.foreach { i =>
      log.info(i.toString)
    }
    log.info("Hi")
  }
}
