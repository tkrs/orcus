package orcus.codec

import org.apache.hadoop.hbase.client.Put
import org.apache.hadoop.hbase.util.Bytes
import shapeless._
import shapeless.labelled.FieldType

trait PutFamilyEncoder[A] {
  def apply(acc: Put, cf: Array[Byte], a: A, ts: Long): Option[Put]
}

object PutFamilyEncoder extends PutFamilyEncoder1 {

  def apply[A](implicit A: PutFamilyEncoder[A]): PutFamilyEncoder[A] = A

  implicit def mapPutFamilyEncoder[K, V](
      implicit
      H: ValueCodec[K],
      V: ValueCodec[V]
  ): PutFamilyEncoder[Map[K, V]] = new PutFamilyEncoder[Map[K, V]] {
    def apply(acc: Put, cf: Array[Byte], a: Map[K, V], ts: Long = Long.MaxValue): Option[Put] = {
      a.foreach {
        case (k, v) =>
          acc.addColumn(cf, H.encode(k), ts, V.encode(v))
      }
      if (a.isEmpty) None else Some(acc)
    }
  }
}

trait PutFamilyEncoder1 {

  implicit val hnilPutEncoder: PutFamilyEncoder[HNil] = new PutFamilyEncoder[HNil] {
    def apply(acc: Put, cf: Array[Byte], a: HNil, ts: Long): Option[Put] = Some(acc)
  }

  implicit def hlabelledConsPutFamilyEncoder[K <: Symbol, H, T <: HList](
      implicit
      K: Witness.Aux[K],
      H: ValueCodec[H],
      T: Lazy[PutFamilyEncoder[T]]
  ): PutFamilyEncoder[FieldType[K, H] :: T] = new PutFamilyEncoder[::[FieldType[K, H], T]] {
    def apply(acc: Put, cf: Array[Byte], a: FieldType[K, H] :: T, ts: Long): Option[Put] = a match {
      case h :: t =>
        for {
          hp <- Option(acc.addColumn(cf, Bytes.toBytes(K.value.name), ts, H.encode(h)))
          tp <- T.value(hp, cf, t, ts)
        } yield tp
    }
  }

  implicit def caseClassPutFamilyEncoder[A, R](
      implicit
      gen: LabelledGeneric.Aux[A, R],
      R: Lazy[PutFamilyEncoder[R]]
  ): PutFamilyEncoder[A] = new PutFamilyEncoder[A] {
    def apply(acc: Put, cf: Array[Byte], a: A, ts: Long = Long.MaxValue): Option[Put] = {
      R.value(acc, cf, gen.to(a), ts)
    }
  }
}
