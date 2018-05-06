package treelogger

import cats.{Applicative, Monad}
import cats.implicits._

trait TreeLogger[F[_], A] {

  def leaf(value: => A): F[Unit]

  protected def downBranch(value: => A): F[Unit]

  protected def upBranch: F[Unit]

  final def branch[B](value: => A)(inner: F[B])(implicit F: Monad[F]): F[B] =
    for {
      _ <- downBranch(value)
      result <- inner
      _ <- upBranch
    } yield result

}

object TreeLogger {
  def apply[F[_], A](implicit treeLogger: TreeLogger[F, A]): TreeLogger[F, A] = treeLogger

  def noOp[F[_], A](implicit F: Applicative[F]): TreeLogger[F, A] = new TreeLogger[F, A] {
    override def leaf(value: => A): F[Unit] = F.unit
    override protected def downBranch(value: => A): F[Unit] = F.unit
    override protected def upBranch: F[Unit] = F.unit
  }

}
