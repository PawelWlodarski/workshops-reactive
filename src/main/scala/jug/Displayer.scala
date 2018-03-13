package jug

trait WorkshopDisplayer{
  def header(s:String) = println(s"\n *** $s *** \n")
  def title(s:String) = println(s" *** $s ***")
  def section(s:String) = println(s"    *** $s")
  def section[A](s:String,example:A) = println(s"    *** $s : "+example)
}

object WorkshopDisplayer extends WorkshopDisplayer

trait ThreadInfo {
  def threadName = Thread.currentThread().getName
}

object ThreadInfo extends ThreadInfo