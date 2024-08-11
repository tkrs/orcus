package orcus.bigtable.codec

import orcus.bigtable.Row
import shapeless3.deriving.*

import scala.annotation.tailrec

trait DerivedRowDecoder[A] extends RowDecoder[A]

object DerivedRowDecoder:

  private val pure = [A] => (a: A) => Right(a)
  private val map  = [A, B] => (fa: Either[Throwable, A], f: A => B) => fa.map(f)
  private val tailRecM = [A, B] =>
    (a: A, f: A => Either[Throwable, Either[A, B]]) =>
      @tailrec def loop(a: A): Either[Throwable, B] = f(a) match
        case Left(err)       => Left(err)
        case Right(Left(a))  => loop(a)
        case Right(Right(b)) => Right(b)
      loop(a)

  given decoderGen[A](using
    inst: K0.ProductInstances[FamilyDecoder, A],
    labelling: Labelling[A]
  ): DerivedRowDecoder[A] with
    def apply(row: Row): Either[Throwable, A] =
      val labels = labelling.elemLabels.iterator
      val decode = [t] =>
        (f: FamilyDecoder[t]) =>
          val label = labels.next()
          val cells = row.families.getOrElse(label, null)
          f(cells)
      inst.constructM(decode)(pure, map, tailRecM)

  inline def derived[A](using K0.ProductGeneric[A]): DerivedRowDecoder[A] = decoderGen
