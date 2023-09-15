package bb.cc

import cats.*
import cats.implicits.given
import cats.effect.*
import org.apache.pekko.Done

import scala.concurrent.{ExecutionContext, Future, Promise}

trait CatchKeybordImpl:

  def inputKeyAndNext(charInstance: Char): Unit = catchFunc.trySuccess(charInstance)

  protected val catchFunc: Promise[Char] = Promise[Char]
  protected val toFuture: Future[Char]   = catchFunc.future
  def toIO: IO[Char]                     = IO.fromFuture(IO(toFuture))
  val tail: Future[CatchKeybordImpl]

end CatchKeybordImpl

object CatchKeybordImpl:

  def gen(using ExecutionContext): CatchKeybordImpl =
    new CatchKeybordImpl:
      self =>
      override val tail: Future[CatchKeybordImpl] = for _: Char <- self.toFuture yield gen
    end new
  end gen

end CatchKeybordImpl
