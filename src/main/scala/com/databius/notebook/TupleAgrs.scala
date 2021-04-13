package com.databius.notebook

object TupleAgrs extends App {
  def fn(i: Int, s: String): Unit = println(s"$i -> $s")

  val m = List.range(0, 10).map(i => (i -> s"Number $i"))

  m.foreach { i =>
    println(s"case 1: $i")
  }
  //Not work
  m.foreach { i =>
    {
      i match {
        case (_, _) => fn _
      }
    }
  }
  //Work
  m.foreach { i =>
    {
      i match {
        case (_: Int, _: String) => print(s"case 3: "); fn(_, _)
      }
    }
  }
  //Work
  m.foreach { i =>
    {
      print(s"case 4: "); fn _ tupled _
    }
  }

}
