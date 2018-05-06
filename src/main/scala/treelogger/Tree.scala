package treelogger

import cats.Show
import treelogger.TreeLoggerBuilder.BranchBuilder

import scala.annotation.tailrec

sealed trait Tree[A]

object Tree {
  implicit def showInstance[A](implicit A: Show[A]): Show[Tree[A]] = {
    // TODO: make tail recursive
    def go(level: Int, tree: Tree[A], stringBuilder: StringBuilder): StringBuilder = {
      if (stringBuilder.nonEmpty) {
        stringBuilder + '\n'
      }

      tree match {
        case Leaf(a) =>
          stringBuilder ++= "  " * level
          stringBuilder ++= A.show(a)
        case Branch(a, children) =>
          stringBuilder ++= "  " * level
          stringBuilder ++= A.show(a)
          children.foreach(go(level + 1, _, stringBuilder))
          stringBuilder
      }
    }

    Show.show(go(0, _, StringBuilder.newBuilder).result())
  }
}

case class Leaf[A](value: A) extends Tree[A]
case class Branch[A](value: A, children: List[Tree[A]]) extends Tree[A]

class TreeLoggerBuilder[A] private (current: Option[BranchBuilder[A]], completed: List[Tree[A]]) {
  private def copy(current: Option[BranchBuilder[A]] = current, completed: List[Tree[A]] = completed): TreeLoggerBuilder[A] = new TreeLoggerBuilder[A](current, completed)

  def leaf(value: A): TreeLoggerBuilder[A] = current match {
    case Some(builder) => copy(current = Some(builder.leaf(value)))
    case None          => copy(completed = Leaf(value) :: completed)
  }

  def downBranch(value: A): TreeLoggerBuilder[A] = current match {
    case Some(builder) => copy(current = Some(builder.downBranch(value)))
    case None          => copy(current = Some(BranchBuilder.empty(value)))
  }

  def upBranch: TreeLoggerBuilder[A] = current match {
    case Some(builder) if builder.path.isEmpty => copy(current = None, completed = builder.upBranch.complete :: completed)
    case Some(builder)                         => copy(current = Some(builder.upBranch))
    case None                                  => this
  }

  def complete: List[Tree[A]] = current match {
    case Some(builder) => builder.complete :: completed.reverse
    case None          => completed.reverse
  }

  def modifyCurrent(f: A => A): TreeLoggerBuilder[A] =
    copy(current = current.map(builder => builder.copy(value = f(builder.value))))
}

object TreeLoggerBuilder {
  def empty[A]: TreeLoggerBuilder[A] = new TreeLoggerBuilder[A](None, Nil)

  private case class BranchBuilder[A](value: A, path: List[BranchBuilder[A]] = Nil, completed: List[Tree[A]] = Nil) {
    def leaf(value: A): BranchBuilder[A] = copy(completed = Leaf(value) :: completed)

    def downBranch(value: A): BranchBuilder[A] = BranchBuilder(value, path = this :: path)

    def upBranch: BranchBuilder[A] = path match {
      case parent :: _ => parent.copy(completed = Branch(value, completed.reverse) :: parent.completed)
      case Nil         => this
    }

    def complete: Branch[A] = {
      @tailrec
      def go(builder: BranchBuilder[A]): Branch[A] =
        if (builder.path.isEmpty) {
          Branch(builder.value, builder.completed.reverse)
        } else {
          go(builder.upBranch)
        }

      go(this)
    }
  }

  private object BranchBuilder {
    def empty[A](root: A) = BranchBuilder(root)
  }

}
