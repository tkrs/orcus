package orcus.bigtable.codec

import com.google.cloud.bigtable.data.v2.models.RowCell
import shapeless3.deriving.*

import scala.annotation.tailrec

trait DerivedFamilyDecoder[A] extends FamilyDecoder[A]

object DerivedFamilyDecoder extends DerivedFamilyDecoder1

private[codec] trait DerivedFamilyDecoder1:

  private val pure = [A] => (a: A) => Right(a)
  private val map  = [A, B] => (fa: Either[Throwable, A], f: A => B) => fa.map(f)
  private val tailRecM = [A, B] =>
    (a: A, f: A => Either[Throwable, Either[A, B]]) =>
      @tailrec def loop(a: A): Either[Throwable, B] = f(a) match
        case Left(err)       => Left(err)
        case Right(Left(a))  => loop(a)
        case Right(Right(b)) => Right(b)
      loop(a)

  given familyDecoderGen[A](using
    inst: K0.ProductInstances[ValueDecoder, A],
    labelling: Labelling[A]
  ): DerivedFamilyDecoder[A] with
    def apply(family: List[RowCell]): Either[Throwable, A] =
      val labels = labelling.elemLabels.iterator
      val decode = [t] =>
        (f: ValueDecoder[t]) =>
          val label = labels.next()
          val cells = family.collect { case r if r.getQualifier.toStringUtf8 == label => r }
          f(cells)
      inst.constructM(decode)(pure, map, tailRecM)

  inline def derived[A](using K0.ProductGeneric[A]): DerivedFamilyDecoder[A] = familyDecoderGen
