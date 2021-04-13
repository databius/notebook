package com.databius.notebook.scalaz.rws

object ReaderWriterForPostCommits {

  import scalaz.Scalaz._
  import scalaz._

  type Key = String

  trait Transaction

  // A class to hold the post commit function
  case class PostCommit(f: () => Unit)

  /* Work represents some work to do on the Database
   * It is a Reader that takes a Transaction and returns a result
   * It is a Writer that records post commit actions in a List
   * It is also a State which is ignored here
   * ReaderWriterState's type args are:
   *   the Reader type, Writer type, State type and A
   */
  type Work[A] =
    ReaderWriterState[Transaction, List[PostCommit], Unit, A]

  // helper to create Work for some Transaction => T
  def work[T](f: Transaction => T): Work[T] =
    ReaderWriterState { (trans, ignored) =>
      (Nil, f(trans), ())
    }

  // helper to create Work for a post commit,
  // PostCommits are added to the written value
  def postCommit(f: () => Unit): Work[Unit] =
    ReaderWriterState { (trans, ignored) =>
      (List(PostCommit(f)), (), ())
    }

  object Database {

    object MyTransaction extends Transaction

    // a convenient method to drop the state part of the result
    // and also could be used in tests to check post commits
    def runWork[T](work: Work[T]): (List[PostCommit], T) = {
      val results                             = work.run(MyTransaction, ())
      val (postCommits, result, ignoredState) = results

      (postCommits, result)
    }

    def run[T](work: Work[T]): \/[Throwable, T] =
      \/.fromTryCatchNonFatal {
        startTransaction()
        val (postCommits, result) = runWork(work)
        postCommits foreach addPostCommit
        commit()
        result
      }.leftMap(err => { rollback(); err })

    def addPostCommit(pc: PostCommit): Unit = {}
    def startTransaction()                  = {}
    def commit()                            = {}
    def rollback()                          = {}

    def put[A](key: Key, a: A): Work[Unit] =
      work(Transaction => {})

    def find[A](key: Key): Work[Option[A]] =
      work(Transaction => None)
  }

  // The program with a post commit
  val work2: Work[Option[String]] =
    for {
      _     <- Database.put("foo", "Bar")
      _     <- postCommit(() => println("wahey"))
      found <- Database.find[String]("foo")
    } yield found

  // note that the result type is now \/
  val result2: \/[Throwable, Option[String]] =
    Database.run(work2)
}
