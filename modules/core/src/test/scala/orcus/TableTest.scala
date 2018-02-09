package orcus

import java.io.IOException

import cats.instances.either._
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.hbase.TableName
import org.apache.hadoop.hbase.client._
import org.apache.hadoop.hbase.util.Bytes
import org.scalatest.{FunSpec, Matchers}
import org.scalatest.mockito.MockitoSugar
import org.mockito.Mockito._

class TableTest extends FunSpec with MockitoSugar with Matchers {
  type F[A] = Either[Throwable, A]

  describe("getName") {
    it("should return table name") {
      val m = mock[Table]
      val n = "1"
      val q = Bytes.toBytes(n)
      val t = TableName.valueOf(q)
      when(m.getName).thenReturn(t)

      val Right(v) = table.getName[F](m)
      assert(t === v)
      verify(m).getName
    }
  }

  describe("getConfiguration") {
    it("should return configuration") {
      val m = mock[Table]
      val c = new Configuration(false)
      when(m.getConfiguration).thenReturn(c)

      val Right(v) = table.getConfiguration[F](m)
      assert(c === v)
      verify(m).getConfiguration
    }
  }

  describe("getTableDescriptor") {
    it("should return TableDescriptor") {
      val m = mock[Table]
      val c = TableDescriptorBuilder.newBuilder(TableName.valueOf("1")).build()
      when(m.getDescriptor).thenReturn(c)

      val Right(v) = table.getDescriptor[F](m)
      assert(c === v)
      verify(m).getDescriptor
    }

    it("should return error when Table.getTableDescriptor throws IOException") {
      val m = mock[Table]

      val ex = new IOException("Oops")

      when(m.getDescriptor).thenThrow(ex)

      val Left(e) = table.getDescriptor[F](m)
      assert(e === ex)
      verify(m).getDescriptor
    }
  }

  describe("exists") {
    it("should return value obtained from Table.exists(Get) as-is") {
      val m   = mock[Table]
      val n   = "1"
      val rk  = Bytes.toBytes(n)
      val get = new Get(rk)

      when(m.exists(get)).thenReturn(true)

      val Right(v) = table.exists[F](m, get)
      assert(v === true)
      verify(m).exists(get)
    }
    it("should return error when Table.exists(Get) throws IOException") {
      val m   = mock[Table]
      val n   = "1"
      val rk  = Bytes.toBytes(n)
      val get = new Get(rk)

      val ex = new IOException("Oops")
      when(m.exists(get)).thenThrow(ex)

      val Left(e) = table.exists[F](m, get)
      assert(e === ex)
      verify(m).exists(get)
    }
  }

  describe("get") {
    it("should return value obtained from Table.get(Get) as-is") {
      val m   = mock[Table]
      val n   = "1"
      val rk  = Bytes.toBytes(n)
      val get = new Get(rk)
      val res = mock[Result]

      when(m.get(get)).thenReturn(res)

      val Right(v) = table.get[F](m, get)
      assert(v === res)
      verify(m).get(get)
    }
    it("should return error when Table.get(Get) throws IOException") {
      val m   = mock[Table]
      val n   = "1"
      val rk  = Bytes.toBytes(n)
      val get = new Get(rk)

      val ex = new IOException("Oops")
      when(m.get(get)).thenThrow(ex)

      val Left(e) = table.get[F](m, get)
      assert(e === ex)
      verify(m).get(get)
    }
  }

  describe("put") {
    it("should return Unit obtained from Table.put(Put) as-is") {
      val m   = mock[Table]
      val n   = "1"
      val rk  = Bytes.toBytes(n)
      val put = new Put(rk)

      doNothing().when(m).put(put)

      val Right(v) = table.put[F](m, put)
      assert(v.isInstanceOf[Unit])
      verify(m).put(put)
    }
    it("should return error when Table.put(Put) throws IOException") {
      val m   = mock[Table]
      val n   = "1"
      val rk  = Bytes.toBytes(n)
      val put = new Put(rk)

      val ex = new IOException("Oops")
      when(m.put(put)).thenThrow(ex)

      val Left(e) = table.put[F](m, put)
      assert(e === ex)
      verify(m).put(put)
    }
  }

  describe("getScanner") {
    it("should return ResultScanner obtained from Table.getScanner(Scan) as-is") {
      val m    = mock[Table]
      val n    = "1"
      val rk   = Bytes.toBytes(n)
      val scan = new Scan().withStartRow(rk)
      val r    = mock[ResultScanner]

      when(m.getScanner(scan)).thenReturn(r)

      val Right(v) = table.getScanner[F](m, scan)
      assert(v === r)
      verify(m).getScanner(scan)
    }
    it("should return error when Table.put(Put) throws IOException") {
      val m    = mock[Table]
      val n    = "1"
      val rk   = Bytes.toBytes(n)
      val scan = new Scan().withStartRow(rk)

      val ex = new IOException("Oops")
      when(m.getScanner(scan)).thenThrow(ex)

      val Left(e) = table.getScanner[F](m, scan)
      assert(e === ex)
      verify(m).getScanner(scan)
    }
  }

  describe("delete") {
    it("should return Unit obtained from Table.delete(Delete) as-is") {
      val m      = mock[Table]
      val n      = "1"
      val rk     = Bytes.toBytes(n)
      val delete = new Delete(rk)

      doNothing().when(m).delete(delete)

      val Right(v) = table.delete[F](m, delete)
      assert(v.isInstanceOf[Unit])
      verify(m).delete(delete)
    }
    it("should return error when Table.delete(Delete) throws IOException") {
      val m      = mock[Table]
      val n      = "1"
      val rk     = Bytes.toBytes(n)
      val delete = new Delete(rk)

      val ex = new IOException("Oops")
      when(m.delete(delete)).thenThrow(ex)

      val Left(e) = table.delete[F](m, delete)
      assert(e === ex)
      verify(m).delete(delete)
    }
  }

  describe("append") {
    it("should return Result obtained from Table.append(Append) as-is") {
      val m      = mock[Table]
      val n      = "1"
      val rk     = Bytes.toBytes(n)
      val append = new Append(rk)
      val res    = mock[Result]

      when(m.append(append)).thenReturn(res)

      val Right(v) = table.append[F](m, append)
      assert(v === res)
      verify(m).append(append)
    }
    it("should return error when Table.append(Append) throws IOException") {
      val m      = mock[Table]
      val n      = "1"
      val rk     = Bytes.toBytes(n)
      val append = new Append(rk)

      val ex = new IOException("Oops")
      when(m.append(append)).thenThrow(ex)

      val Left(e) = table.append[F](m, append)
      assert(e === ex)
      verify(m).append(append)
    }
  }

  describe("increment") {
    it("should return Result obtained from Table.increment(Increment) as-is") {
      val m         = mock[Table]
      val n         = "1"
      val rk        = Bytes.toBytes(n)
      val increment = new Increment(rk)
      val res       = mock[Result]

      when(m.increment(increment)).thenReturn(res)

      val Right(v) = table.increment[F](m, increment)
      assert(v === res)
      verify(m).increment(increment)
    }
    it("should return error when Table.increment(Increment) throws IOException") {
      val m         = mock[Table]
      val n         = "1"
      val rk        = Bytes.toBytes(n)
      val increment = new Increment(rk)

      val ex = new IOException("Oops")
      when(m.increment(increment)).thenThrow(ex)

      val Left(e) = table.increment[F](m, increment)
      assert(e === ex)
      verify(m).increment(increment)
    }
  }

  describe("close") {
    it("should return Unit obtained from Table.close() as-is") {
      val m = mock[Table]

      doNothing().when(m).close()

      val Right(v) = table.close[F](m)
      assert(v.isInstanceOf[Unit])
      verify(m).close()
    }
    it("should return error when Table.close() throws IOException") {
      val m = mock[Table]

      val ex = new IOException("Oops")
      doThrow(ex).when(m).close()

      val Left(e) = table.close[F](m)
      assert(e === ex)
      verify(m).close()
    }
  }
}
