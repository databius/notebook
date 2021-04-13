package com.databius.notebook.scalaz.rws

object TheProblem {

  type Key = String

  object Database {

    // Run a function in a transaction, rolling back on failure
    def run[T](f: => T): T =
      try {
        startTransaction()
        val result = f
        commit()
        result
      } catch {
        case whatever => rollback(); throw whatever
      }

    def startTransaction() = {}

    def commit() = {}

    def rollback() = {}

    def addPostCommit(f: () => Unit): Unit = {}

    def put[A](a: A): Unit = {}

    def find[A](key: String): Option[A] = None
  }

  val result: Option[String] = Database.run {
    Database.put("stuff")
    Database.addPostCommit(() => println("blah"))
    Database.find("foo")
  }
}
