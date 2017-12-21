package orcus.codec

import org.apache.hadoop.hbase.client.Put
import org.apache.hadoop.hbase.util.Bytes
import shapeless._
import shapeless.labelled.FieldType

trait PutCFEncoder[A] {
  def apply(acc: Put, cf: Array[Byte], a: A): Option[Put]
}

object PutCFEncoder extends PutCFEncoder1 {

  def apply[A](implicit A: PutCFEncoder[A]): PutCFEncoder[A] = A
}

trait PutCFEncoder1 {

  implicit val hnilPutEncoder: PutCFEncoder[HNil] = new PutCFEncoder[HNil] {
    def apply(acc: Put, cf: Array[Byte], a: HNil): Option[Put] = Some(acc)
  }

  implicit def hlabelledConsPutCFEncoder[K <: Symbol, H, T <: HList](
      implicit
      K: Witness.Aux[K],
      H: ValueCodec[H],
      T: Lazy[PutCFEncoder[T]]
  ): PutCFEncoder[FieldType[K, H] :: T] = new PutCFEncoder[::[FieldType[K, H], T]] {
    def apply(acc: Put, cf: Array[Byte], a: FieldType[K, H] :: T): Option[Put] = a match {
      case h :: t =>
        for {
          hp <- Option(acc.addColumn(cf, Bytes.toBytes(K.value.name), H.encode(h)))
          tp <- T.value(hp, cf, t)
        } yield tp
    }
  }

  implicit def caseClassPutEncoder[A, R](
      implicit
      gen: LabelledGeneric.Aux[A, R],
      R: Lazy[PutCFEncoder[R]]
  ): PutCFEncoder[A] = new PutCFEncoder[A] {
    def apply(acc: Put, cf: Array[Byte], a: A): Option[Put] = {
      R.value(acc, cf, gen.to(a))
    }
  }
}
