package treelogger

import cats.implicits._
import cats.Monad
import cats.data.State
import utest._

object TreeLoggerTest extends TestSuite {
  def stateTreeLogger: TreeLogger[State[TreeLoggerBuilder[String], ?], String] =
    new TreeLogger[State[TreeLoggerBuilder[String], ?], String] {
      override def leaf(value: => String): State[TreeLoggerBuilder[String], Unit] = State.modify(_.leaf(value))
      override protected def downBranch(value: => String): State[TreeLoggerBuilder[String], Unit] = State.modify(_.downBranch(value))
      override protected def upBranch: State[TreeLoggerBuilder[String], Unit] = State.modify(_.upBranch)
    }

  val tests = Tests {
    "example" - {
      def tester[F[_]: Monad](TLog: TreeLogger[F, String]) =
        TLog.branch("root") {
          for {
            _ <- TLog.leaf("foo")
            _ <- TLog.branch("downhere")(TLog.leaf("baz"))
            _ <- TLog.leaf("bar")
          } yield ()
        }

      assert(
        tester(stateTreeLogger)
          .runS(TreeLoggerBuilder.empty)
          .value
          .complete == List(Branch("root", List(Leaf("foo"), Branch("downhere", List(Leaf("baz"))), Leaf("bar")))))
      assert(
        tester(TreeLogger.noOp[State[TreeLoggerBuilder[String], ?], String])
          .runS(TreeLoggerBuilder.empty)
          .value
          .complete == Nil)
    }
  }
}
