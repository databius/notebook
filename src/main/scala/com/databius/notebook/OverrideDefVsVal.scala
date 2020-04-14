package com.databius.notebook.scala

trait ParentA {
  val v: String
  val x: String = { println(s"${this.getClass} Assign x = $v"); v }
  val y: String = { println(s"${this.getClass} Assign y = $x"); x }
  println(s"${this.getClass} Run ParentA: v = $v, x = $x, y = $y")

  def fx(): Unit = println(x)
  def fy(): Unit = println(y)
}
trait ParentB {
  def v: String = ""
  val x: String = { println(s"${this.getClass} Assign x = $v"); v }
  val y: String = { println(s"${this.getClass} Assign y = $x"); x }
  println(s"${this.getClass} Run ParentB: v = $v, x = $x, y = $y")

  def fx(): Unit = println(x)
  def fy(): Unit = println(y)
}
trait ParentC {
  lazy val v: String = ""
  val x: String = { println(s"${this.getClass} Assign x = $v"); v }
  val y: String = { println(s"${this.getClass} Assign y = $x"); x }
  println(s"${this.getClass} Run ParentC: v = $v, x = $x, y = $y")

  def fx(): Unit = println(x)
  def fy(): Unit = println(y)
}

object ChildA extends ParentA {
  override val v = "A"
  override val x: String = { println(s"${this.getClass} Assign x = $v"); v }
  println(s"${this.getClass} Run ChildA: v = $v, x = $x, y = $y")
}

object ChildB extends ParentB {
  override def v = "B"
  println(s"${this.getClass} Run ChildB: v = $v, x = $x, y = $y")
}

object ChildC extends ParentC {
  override lazy val v = "C"
  println(s"${this.getClass} Run ChildC: v = $v, x = $x, y = $y")
}

//trait A {
//  val x1: String
//  val x2: String = "A: " + x1 + "mom"
//
//  println("A: " + x1 + ", " + x2)
//}
//class B extends A {
//  val x1: String = "hello"
//
//  println("B: " + x1 + ", " + x2)
//}
//class C extends B {
////  override val x2: String = "dad"
//
//  println("C: " + x1 + ", " + x2)
//}

trait A {
  val x1: String
  val x2: String = "mom"
  final val x3 = "goodbye"

  println("A: " + x1 + ", " + x2)
}
class B extends A {
  val x1: String = "hello"

  println("B: " + x1 + ", " + x2)
}
class C extends B {
  override val x2: String = "dad"

  println("C: " + x1 + ", " + x2)
}

object OverrideDefVsVal extends App {
  ChildA.fx()
  ChildA.fy()
  ChildB.fx()
  ChildB.fy()
  ChildC.fx()
  ChildC.fy()
//  new B
//  new C
}
