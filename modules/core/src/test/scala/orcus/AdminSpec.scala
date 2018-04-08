package orcus

import java.util.concurrent.CompletableFuture
import java.util.regex.Pattern

import cats.instances.future._
import org.apache.hadoop.hbase.{NamespaceDescriptor, ServerName, TableName}
import org.apache.hadoop.hbase.client._
import org.apache.hadoop.hbase.util.Bytes
import org.scalatest.FlatSpec
import org.scalatest.mockito.MockitoSugar
import org.mockito.Mockito._

import scala.collection.JavaConverters._
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global

class AdminSpec extends FlatSpec with MockitoSugar {
  import orcus.async._
  import orcus.async.AsyncHandler._

  def voidFuture: CompletableFuture[Void] =
    CompletableFuture.runAsync(new Runnable {
      def run(): Unit = ()
    })
  def jTrueFuture: CompletableFuture[java.lang.Boolean] =
    CompletableFuture.completedFuture(java.lang.Boolean.TRUE)
  def jFalseFuture: CompletableFuture[java.lang.Boolean] =
    CompletableFuture.completedFuture(java.lang.Boolean.FALSE)

  "tableExists" should "call tableExists of AsyncAdmin" in {
    val m  = mock[AsyncAdmin]
    val tn = TableName.valueOf("t")
    when(m.tableExists(tn)).thenReturn(jTrueFuture)
    assert(Await.result(admin.tableExists[Future](m, tn), 3.seconds))
    verify(m).tableExists(tn)
  }

  "listTableDescriptors" should "call listTableDescriptors of AsyncAdmin" in {
    val m = mock[AsyncAdmin]
    val l = java.util.Arrays.asList(TableDescriptorBuilder.NAMESPACE_TABLEDESC)
    when(m.listTableDescriptors(true)).thenReturn(CompletableFuture.completedFuture(l))
    val r = Await.result(admin.listTableDescriptors[Future](m, true), 3.seconds)
    assert(r === l.asScala)
    verify(m).listTableDescriptors(true)
  }

  "listTableDescriptorsByPattern" should "call listTableDescriptors of AsyncAdmin" in {
    val m = mock[AsyncAdmin]
    val p = Pattern.compile(".*")
    val l = java.util.Arrays.asList(TableDescriptorBuilder.NAMESPACE_TABLEDESC)
    when(m.listTableDescriptors(p, true)).thenReturn(CompletableFuture.completedFuture(l))
    val r = Await.result(admin.listTableDescriptorsByPattern[Future](m, p, true), 3.seconds)
    assert(r === l.asScala)
    verify(m).listTableDescriptors(p, true)
  }

  "listTableDescriptorsByNamespace" should "call listTableDescriptorsByNamespace of AsyncAdmin" in {
    val m = mock[AsyncAdmin]
    val l = java.util.Arrays.asList(TableDescriptorBuilder.NAMESPACE_TABLEDESC)
    when(m.listTableDescriptorsByNamespace("ns")).thenReturn(CompletableFuture.completedFuture(l))
    val r = Await.result(admin.listTableDescriptorsByNamespace[Future](m, "ns"), 3.seconds)
    assert(r === l.asScala)
    verify(m).listTableDescriptorsByNamespace("ns")
  }

  "listTableNames" should "call listTableNames of AsyncAdmin" in {
    val m = mock[AsyncAdmin]
    val l = java.util.Arrays.asList(TableName.valueOf("t"))
    when(m.listTableNames(true)).thenReturn(CompletableFuture.completedFuture(l))
    val r = Await.result(admin.listTableNames[Future](m, true), 3.seconds)
    assert(r === l.asScala)
    verify(m).listTableNames(true)
  }

  "listTableNamesByPattern" should "call listTableNames of AsyncAdmin" in {
    val m = mock[AsyncAdmin]
    val p = Pattern.compile(".*")
    val l = java.util.Arrays.asList(TableName.valueOf("t"))
    when(m.listTableNames(p, true)).thenReturn(CompletableFuture.completedFuture(l))
    val r = Await.result(admin.listTableNamesByPattern[Future](m, p, true), 3.seconds)
    assert(r === l.asScala)
    verify(m).listTableNames(p, true)
  }

  "getDescriptor" should "call getDescriptor of AsyncAdmin" in {
    val m = mock[AsyncAdmin]
    val t = TableName.valueOf("t")
    val l = TableDescriptorBuilder.NAMESPACE_TABLEDESC
    when(m.getDescriptor(t)).thenReturn(CompletableFuture.completedFuture(l))
    val r = Await.result(admin.getDescriptor[Future](m, t), 3.seconds)
    assert(r === l)
    verify(m).getDescriptor(t)
  }

  "createTable" should "call createTable of AsyncAdmin" in {
    val m = mock[AsyncAdmin]
    val t = TableDescriptorBuilder.NAMESPACE_TABLEDESC
    when(m.createTable(t)).thenReturn(voidFuture)
    Await.result(admin.createTable[Future](m, t), 3.seconds)
    verify(m).createTable(t)
  }

  "createTableWithKeyRange" should "call createTable of AsyncAdmin" in {
    val m        = mock[AsyncAdmin]
    val t        = TableDescriptorBuilder.NAMESPACE_TABLEDESC
    val startKey = Bytes.toBytes("s")
    val endKey   = Bytes.toBytes("e")
    when(m.createTable(t, startKey, endKey, 10)).thenReturn(voidFuture)
    Await.result(admin.createTableWithKeyRange[Future](m, t, startKey, endKey, 10), 3.seconds)
    verify(m).createTable(t, startKey, endKey, 10)
  }

  "createTableWithSplitKeys" should "call createTable of AsyncAdmin" in {
    val m         = mock[AsyncAdmin]
    val t         = TableDescriptorBuilder.NAMESPACE_TABLEDESC
    val splitKeys = Array(Bytes.toBytes("e"))
    when(m.createTable(t, splitKeys)).thenReturn(voidFuture)
    Await.result(admin.createTableWithSplitKeys[Future](m, t, splitKeys), 3.seconds)
    verify(m).createTable(t, splitKeys)
  }

  "modifyTable" should "call modifyTable of AsyncAdmin" in {
    val m         = mock[AsyncAdmin]
    val t         = TableDescriptorBuilder.NAMESPACE_TABLEDESC
    val splitKeys = Array(Bytes.toBytes("e"))
    when(m.createTable(t, splitKeys)).thenReturn(voidFuture)
    Await.result(admin.createTableWithSplitKeys[Future](m, t, splitKeys), 3.seconds)
    verify(m).createTable(t, splitKeys)
  }

  "deleteTable" should "call deleteTable of AsyncAdmin" in {
    val m = mock[AsyncAdmin]
    val t = TableName.valueOf("t")
    when(m.deleteTable(t)).thenReturn(voidFuture)
    Await.result(admin.deleteTable[Future](m, t), 3.seconds)
    verify(m).deleteTable(t)
  }

  "truncateTable" should "call truncateTable of AsyncAdmin" in {
    val m = mock[AsyncAdmin]
    val t = TableName.valueOf("t")
    when(m.truncateTable(t, true)).thenReturn(voidFuture)
    Await.result(admin.truncateTable[Future](m, t, true), 3.seconds)
    verify(m).truncateTable(t, true)
  }

  "enableTable" should "call enableTable of AsyncAdmin" in {
    val m = mock[AsyncAdmin]
    val t = TableName.valueOf("t")
    when(m.enableTable(t)).thenReturn(voidFuture)
    Await.result(admin.enableTable[Future](m, t), 3.seconds)
    verify(m).enableTable(t)
  }

  "disableTable" should "call disableTable of AsyncAdmin" in {
    val m = mock[AsyncAdmin]
    val t = TableName.valueOf("t")
    when(m.disableTable(t)).thenReturn(voidFuture)
    Await.result(admin.disableTable[Future](m, t), 3.seconds)
    verify(m).disableTable(t)
  }

  "isTableEnabled" should "call isTableEnabled of AsyncAdmin" in {
    val m = mock[AsyncAdmin]
    val t = TableName.valueOf("t")
    when(m.isTableEnabled(t)).thenReturn(jTrueFuture)
    assert(Await.result(admin.isTableEnabled[Future](m, t), 3.seconds))
    verify(m).isTableEnabled(t)
  }

  "isTableAvailable" should "call isTableAvailable of AsyncAdmin" in {
    val m = mock[AsyncAdmin]
    val t = TableName.valueOf("t")
    when(m.isTableAvailable(t)).thenReturn(jTrueFuture)
    assert(Await.result(admin.isTableAvailable[Future](m, t), 3.seconds))
    verify(m).isTableAvailable(t)
  }

  "isTableAvailableWithSplitKeys" should "call isTableAvailable of AsyncAdmin" in {
    val m         = mock[AsyncAdmin]
    val splitKeys = Array(Bytes.toBytes("t"))
    val t         = TableName.valueOf("t")
    when(m.isTableAvailable(t, splitKeys)).thenReturn(jTrueFuture)
    assert(Await.result(admin.isTableAvailableWithSplitKeys[Future](m, t, splitKeys), 3.seconds))
    verify(m).isTableAvailable(t, splitKeys)
  }

  "addColumnFamily" should "call addColumnFamily of AsyncAdmin" in {
    val m = mock[AsyncAdmin]
    val t = TableName.valueOf("t")
    val c = ColumnFamilyDescriptorBuilder.of("c")
    when(m.addColumnFamily(t, c)).thenReturn(voidFuture)
    Await.result(admin.addColumnFamily[Future](m, t, c), 3.seconds)
    verify(m).addColumnFamily(t, c)
  }

  "deleteColumnFamily" should "call deleteColumnFamily of AsyncAdmin" in {
    val m = mock[AsyncAdmin]
    val t = TableName.valueOf("t")
    val c = Bytes.toBytes("c")
    when(m.deleteColumnFamily(t, c)).thenReturn(voidFuture)
    Await.result(admin.deleteColumnFamily[Future](m, t, c), 3.seconds)
    verify(m).deleteColumnFamily(t, c)
  }

  "modifyColumnFamily" should "call modifyColumnFamily of AsyncAdmin" in {
    val m = mock[AsyncAdmin]
    val t = TableName.valueOf("t")
    val c = ColumnFamilyDescriptorBuilder.of("c")
    when(m.modifyColumnFamily(t, c)).thenReturn(voidFuture)
    Await.result(admin.modifyColumnFamily[Future](m, t, c), 3.seconds)
    verify(m).modifyColumnFamily(t, c)
  }

  "createNamespace" should "call createNamespace of AsyncAdmin" in {
    val m = mock[AsyncAdmin]
    val d = NamespaceDescriptor.DEFAULT_NAMESPACE
    when(m.createNamespace(d)).thenReturn(voidFuture)
    Await.result(admin.createNamespace[Future](m, d), 3.seconds)
    verify(m).createNamespace(d)
  }

  "modifyNamespace" should "call modifyNamespace of AsyncAdmin" in {
    val m = mock[AsyncAdmin]
    val d = NamespaceDescriptor.DEFAULT_NAMESPACE
    when(m.modifyNamespace(d)).thenReturn(voidFuture)
    Await.result(admin.modifyNamespace[Future](m, d), 3.seconds)
    verify(m).modifyNamespace(d)
  }

  "deleteNamespace" should "call deleteNamespace of AsyncAdmin" in {
    val m = mock[AsyncAdmin]
    when(m.deleteNamespace("n")).thenReturn(voidFuture)
    Await.result(admin.deleteNamespace[Future](m, "n"), 3.seconds)
    verify(m).deleteNamespace("n")
  }

  "getNamespaceDescriptor" should "call getNamespaceDescriptor of AsyncAdmin" in {
    val m = mock[AsyncAdmin]
    val d = NamespaceDescriptor.DEFAULT_NAMESPACE
    when(m.getNamespaceDescriptor("n")).thenReturn(CompletableFuture.completedFuture(d))
    val r = Await.result(admin.getNamespaceDescriptor[Future](m, "n"), 3.seconds)
    assert(r === d)
    verify(m).getNamespaceDescriptor("n")
  }

  "listNamespaceDescriptors" should "call listNamespaceDescriptors of AsyncAdmin" in {
    val m  = mock[AsyncAdmin]
    val ds = Seq(NamespaceDescriptor.DEFAULT_NAMESPACE)
    when(m.listNamespaceDescriptors()).thenReturn(CompletableFuture.completedFuture(ds.asJava))
    val r = Await.result(admin.listNamespaceDescriptors[Future](m), 3.seconds)
    assert(r === ds)
    verify(m).listNamespaceDescriptors()
  }

  "getRegions" should "call getRegions of AsyncAdmin" in {
    val m  = mock[AsyncAdmin]
    val sn = ServerName.valueOf("sn,80")
    val ds = Seq(mock[RegionInfo])
    when(m.getRegions(sn)).thenReturn(CompletableFuture.completedFuture(ds.asJava))
    val r = Await.result(admin.getRegions[Future](m, sn), 3.seconds)
    assert(r === ds)
    verify(m).getRegions(sn)
  }

  "getRegionsByTableName" should "call getRegions of AsyncAdmin" in {
    val m  = mock[AsyncAdmin]
    val tn = TableName.valueOf("tn")
    val ds = Seq(mock[RegionInfo])
    when(m.getRegions(tn)).thenReturn(CompletableFuture.completedFuture(ds.asJava))
    val r = Await.result(admin.getRegionsByTableName[Future](m, tn), 3.seconds)
    assert(r === ds)
    verify(m).getRegions(tn)
  }

  "flush" should "call flush of AsyncAdmin" in {
    val m  = mock[AsyncAdmin]
    val tn = TableName.valueOf("tn")
    when(m.flush(tn)).thenReturn(voidFuture)
    Await.result(admin.flush[Future](m, tn), 3.seconds)
    verify(m).flush(tn)
  }

  "flushRegion" should "call flushRegion of AsyncAdmin" in {
    val m  = mock[AsyncAdmin]
    val rn = Bytes.toBytes("n")
    when(m.flushRegion(rn)).thenReturn(voidFuture)
    Await.result(admin.flushRegion[Future](m, rn), 3.seconds)
    verify(m).flushRegion(rn)
  }

  "flushRegionServer" should "call flushRegionServer of AsyncAdmin" in {
    val m  = mock[AsyncAdmin]
    val sn = ServerName.valueOf("sn,80")
    when(m.flushRegionServer(sn)).thenReturn(voidFuture)
    Await.result(admin.flushRegionServer[Future](m, sn), 3.seconds)
    verify(m).flushRegionServer(sn)
  }

  "compact" should "call compact of AsyncAdmin" in {
    val m  = mock[AsyncAdmin]
    val tn = TableName.valueOf("tn")
    when(m.compact(tn)).thenReturn(voidFuture)
    Await.result(admin.compact[Future](m, tn), 3.seconds)
    verify(m).compact(tn)
  }

  "compactColumn" should "call compact of AsyncAdmin" in {
    val m  = mock[AsyncAdmin]
    val tn = TableName.valueOf("tn")
    val cn = Bytes.toBytes("cn")
    when(m.compact(tn, cn)).thenReturn(voidFuture)
    Await.result(admin.compactColumn[Future](m, tn, cn), 3.seconds)
    verify(m).compact(tn, cn)
  }

  "compactColumnWithType" should "call compact of AsyncAdmin" in {
    val m  = mock[AsyncAdmin]
    val tn = TableName.valueOf("tn")
    val cn = Bytes.toBytes("cn")
    val ct = CompactType.MOB
    when(m.compact(tn, cn, ct)).thenReturn(voidFuture)
    Await.result(admin.compactColumnWithType[Future](m, tn, cn, ct), 3.seconds)
    verify(m).compact(tn, cn, ct)
  }

  "compactRegion" should "call compactRegion of AsyncAdmin" in {
    val m  = mock[AsyncAdmin]
    val rn = Bytes.toBytes("rn")
    when(m.compactRegion(rn)).thenReturn(voidFuture)
    Await.result(admin.compactRegion[Future](m, rn), 3.seconds)
    verify(m).compactRegion(rn)
  }

  "compactFamilyRegion" should "call compactRegion of AsyncAdmin" in {
    val m   = mock[AsyncAdmin]
    val rn  = Bytes.toBytes("rn")
    val cfn = Bytes.toBytes("fn")
    when(m.compactRegion(rn, cfn)).thenReturn(voidFuture)
    Await.result(admin.compactFamilyRegion[Future](m, rn, cfn), 3.seconds)
    verify(m).compactRegion(rn, cfn)
  }

  "majorCompact" should "call majorCompact of AsyncAdmin" in {
    val m  = mock[AsyncAdmin]
    val tn = TableName.valueOf("tn")
    when(m.majorCompact(tn, CompactType.NORMAL)).thenReturn(voidFuture)
    Await.result(admin.majorCompact[Future](m, tn), 3.seconds)
    verify(m).majorCompact(tn, CompactType.NORMAL)
  }

  "majorCompactFamily" should "call majorCompact of AsyncAdmin" in {
    val m   = mock[AsyncAdmin]
    val tn  = TableName.valueOf("tn")
    val cfn = Bytes.toBytes("cfn")
    when(m.majorCompact(tn, cfn, CompactType.NORMAL)).thenReturn(voidFuture)
    Await.result(admin.majorCompactFamily[Future](m, tn, cfn), 3.seconds)
    verify(m).majorCompact(tn, cfn, CompactType.NORMAL)
  }

  "majorCompactRegion" should "call majorCompactRegion of AsyncAdmin" in {
    val m  = mock[AsyncAdmin]
    val rn = Bytes.toBytes("rn")
    when(m.majorCompactRegion(rn)).thenReturn(voidFuture)
    Await.result(admin.majorCompactRegion[Future](m, rn), 3.seconds)
    verify(m).majorCompactRegion(rn)
  }

  "majorCompactFamilyRegion" should "call majorCompactRegion of AsyncAdmin" in {
    val m   = mock[AsyncAdmin]
    val rn  = Bytes.toBytes("rn")
    val cfn = Bytes.toBytes("cfn")
    when(m.majorCompactRegion(rn, cfn)).thenReturn(voidFuture)
    Await.result(admin.majorCompactFamilyRegion[Future](m, rn, cfn), 3.seconds)
    verify(m).majorCompactRegion(rn, cfn)
  }

  "compactRegionServer" should "call compactRegionServer of AsyncAdmin" in {
    val m  = mock[AsyncAdmin]
    val sn = ServerName.valueOf("aaa,100")
    when(m.compactRegionServer(sn)).thenReturn(voidFuture)
    Await.result(admin.compactRegionServer[Future](m, sn), 3.seconds)
    verify(m).compactRegionServer(sn)
  }

  "majorCompactRegionServer" should "call majorCompactRegionServer of AsyncAdmin" in {
    val m  = mock[AsyncAdmin]
    val sn = ServerName.valueOf("aaa,100")
    when(m.majorCompactRegionServer(sn)).thenReturn(voidFuture)
    Await.result(admin.majorCompactRegionServer[Future](m, sn), 3.seconds)
    verify(m).majorCompactRegionServer(sn)
  }
}
