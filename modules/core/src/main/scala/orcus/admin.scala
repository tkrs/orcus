package orcus

//import java.{util => ju}
import java.util.concurrent.CompletableFuture
import java.util.regex.Pattern

import cats.{Applicative, Functor}
import cats.syntax.apply._
import orcus.async.Par
//import com.google.protobuf.RpcChannel
import org.apache.hadoop.hbase.{
//  CacheEvictionStats,
//  ClusterMetrics,
  NamespaceDescriptor,
//  RegionMetrics,
  ServerName,
  TableName
}
import org.apache.hadoop.hbase.client._
//import org.apache.hadoop.hbase.ClusterMetrics.{Option => CMOption}
//import org.apache.hadoop.hbase.client.replication.TableCFs
//import org.apache.hadoop.hbase.client.security.SecurityCapability
//import org.apache.hadoop.hbase.quotas.{QuotaFilter, QuotaSettings}
//import org.apache.hadoop.hbase.replication.{ReplicationPeerConfig, ReplicationPeerDescription}

import scala.collection.JavaConverters._
// import scala.compat.java8.FunctionConverters._

object admin {
  def tableExists[F[_]](t: AsyncAdmin, tableName: TableName)(
    implicit
    M: Functor[F],
    F: Par.Aux[CompletableFuture, F]
  ): F[Boolean] =
    M.map(F.parallel(t.tableExists(tableName)))(_.booleanValue)

  def listTableDescriptors[F[_]](t: AsyncAdmin, includeSysTables: Boolean = false)(
    implicit
    M: Functor[F],
    F: Par.Aux[CompletableFuture, F]
  ): F[Seq[TableDescriptor]] =
    M.map(F.parallel(t.listTableDescriptors(includeSysTables)))(_.asScala.toSeq)

  def listTableDescriptorsByPattern[F[_]](t: AsyncAdmin, pattern: Pattern, includeSysTables: Boolean = false)(
    implicit
    M: Functor[F],
    F: Par.Aux[CompletableFuture, F]
  ): F[Seq[TableDescriptor]] =
    M.map(F.parallel(t.listTableDescriptors(pattern, includeSysTables)))(_.asScala.toSeq)

  def listTableDescriptorsByNamespace[F[_]](t: AsyncAdmin, namespace: String)(
    implicit
    M: Functor[F],
    F: Par.Aux[CompletableFuture, F]
  ): F[Seq[TableDescriptor]] =
    M.map(F.parallel(t.listTableDescriptorsByNamespace(namespace)))(_.asScala.toSeq)

  def listTableNames[F[_]](t: AsyncAdmin, includeSysTables: Boolean)(
    implicit
    M: Functor[F],
    F: Par.Aux[CompletableFuture, F]
  ): F[Seq[TableName]] =
    M.map(F.parallel(t.listTableNames(includeSysTables)))(_.asScala.toSeq)

  def listTableNamesByPattern[F[_]](t: AsyncAdmin, pattern: Pattern, includeSysTables: Boolean)(
    implicit
    M: Functor[F],
    F: Par.Aux[CompletableFuture, F]
  ): F[Seq[TableName]] =
    M.map(F.parallel(t.listTableNames(pattern, includeSysTables)))(_.asScala.toSeq)

  def getDescriptor[F[_]](t: AsyncAdmin, tableName: TableName)(
    implicit
    F: Par.Aux[CompletableFuture, F]
  ): F[TableDescriptor] =
    F.parallel(t.getDescriptor(tableName))

  def createTable[F[_]](t: AsyncAdmin, tableDescriptor: TableDescriptor)(
    implicit
    M: Applicative[F],
    F: Par.Aux[CompletableFuture, F]
  ): F[Unit] =
    F.parallel(t.createTable(tableDescriptor)) *> M.unit

  def createTableWithKeyRange[F[_]](
    t: AsyncAdmin,
    tableDescriptor: TableDescriptor,
    startKey: Array[Byte],
    endKey: Array[Byte],
    numRegions: Int
  )(
    implicit
    M: Applicative[F],
    F: Par.Aux[CompletableFuture, F]
  ): F[Unit] =
    F.parallel(t.createTable(tableDescriptor, startKey, endKey, numRegions)) *> M.unit

  def createTableWithSplitKeys[F[_]](t: AsyncAdmin, tableDescriptor: TableDescriptor, splitKeys: Array[Array[Byte]])(
    implicit
    M: Applicative[F],
    F: Par.Aux[CompletableFuture, F]
  ): F[Unit] =
    F.parallel(t.createTable(tableDescriptor, splitKeys)) *> M.unit

  def modifyTable[F[_]](t: AsyncAdmin, tableDescriptor: TableDescriptor)(
    implicit
    M: Applicative[F],
    F: Par.Aux[CompletableFuture, F]
  ): F[Unit] =
    F.parallel(t.modifyTable(tableDescriptor)) *> M.unit

  def deleteTable[F[_]](t: AsyncAdmin, tableName: TableName)(
    implicit
    M: Applicative[F],
    F: Par.Aux[CompletableFuture, F]
  ): F[Unit] =
    F.parallel(t.deleteTable(tableName)) *> M.unit

  def truncateTable[F[_]](t: AsyncAdmin, tableName: TableName, preserveSplits: Boolean)(
    implicit
    M: Applicative[F],
    F: Par.Aux[CompletableFuture, F]
  ): F[Unit] =
    F.parallel(t.truncateTable(tableName, preserveSplits)) *> M.unit

  def enableTable[F[_]](t: AsyncAdmin, tableName: TableName)(
    implicit
    M: Applicative[F],
    F: Par.Aux[CompletableFuture, F]
  ): F[Unit] =
    F.parallel(t.enableTable(tableName)) *> M.unit

  def disableTable[F[_]](t: AsyncAdmin, tableName: TableName)(
    implicit
    M: Applicative[F],
    F: Par.Aux[CompletableFuture, F]
  ): F[Unit] =
    F.parallel(t.disableTable(tableName)) *> M.unit

  def isTableEnabled[F[_]](t: AsyncAdmin, tableName: TableName)(
    implicit
    M: Functor[F],
    F: Par.Aux[CompletableFuture, F]
  ): F[Boolean] =
    M.map(F.parallel(t.isTableEnabled(tableName)))(_.booleanValue)

  def isTableAvailable[F[_]](t: AsyncAdmin, tableName: TableName)(
    implicit
    M: Functor[F],
    F: Par.Aux[CompletableFuture, F]
  ): F[Boolean] =
    M.map(F.parallel(t.isTableAvailable(tableName)))(_.booleanValue)

  def isTableAvailableWithSplitKeys[F[_]](t: AsyncAdmin, tableName: TableName, splitKeys: Array[Array[Byte]])(
    implicit
    M: Functor[F],
    F: Par.Aux[CompletableFuture, F]
  ): F[Boolean] =
    M.map(F.parallel(t.isTableAvailable(tableName, splitKeys)))(_.booleanValue)

  def addColumnFamily[F[_]](t: AsyncAdmin, tableName: TableName, columnFamilyDescriptor: ColumnFamilyDescriptor)(
    implicit
    M: Applicative[F],
    F: Par.Aux[CompletableFuture, F]
  ): F[Unit] =
    F.parallel(t.addColumnFamily(tableName, columnFamilyDescriptor)) *> M.unit

  def deleteColumnFamily[F[_]](t: AsyncAdmin, tableName: TableName, columnFamily: Array[Byte])(
    implicit
    M: Applicative[F],
    F: Par.Aux[CompletableFuture, F]
  ): F[Unit] =
    F.parallel(t.deleteColumnFamily(tableName, columnFamily)) *> M.unit

  def modifyColumnFamily[F[_]](t: AsyncAdmin, tableName: TableName, columnFamilyDescriptor: ColumnFamilyDescriptor)(
    implicit
    M: Applicative[F],
    F: Par.Aux[CompletableFuture, F]
  ): F[Unit] =
    F.parallel(t.modifyColumnFamily(tableName, columnFamilyDescriptor)) *> M.unit

  def createNamespace[F[_]](t: AsyncAdmin, namespaceDescriptor: NamespaceDescriptor)(
    implicit
    M: Applicative[F],
    F: Par.Aux[CompletableFuture, F]
  ): F[Unit] =
    F.parallel(t.createNamespace(namespaceDescriptor)) *> M.unit

  def modifyNamespace[F[_]](t: AsyncAdmin, namespaceDescriptor: NamespaceDescriptor)(
    implicit
    M: Applicative[F],
    F: Par.Aux[CompletableFuture, F]
  ): F[Unit] =
    F.parallel(t.modifyNamespace(namespaceDescriptor)) *> M.unit

  def deleteNamespace[F[_]](t: AsyncAdmin, namespace: String)(
    implicit
    M: Applicative[F],
    F: Par.Aux[CompletableFuture, F]
  ): F[Unit] =
    F.parallel(t.deleteNamespace(namespace)) *> M.unit

  def getNamespaceDescriptor[F[_]](t: AsyncAdmin, namespace: String)(
    implicit
    F: Par.Aux[CompletableFuture, F]
  ): F[NamespaceDescriptor] =
    F.parallel(t.getNamespaceDescriptor(namespace))

  def listNamespaceDescriptors[F[_]](t: AsyncAdmin)(
    implicit
    M: Applicative[F],
    F: Par.Aux[CompletableFuture, F]
  ): F[Seq[NamespaceDescriptor]] =
    M.map(F.parallel(t.listNamespaceDescriptors))(_.asScala.toSeq)

  def getRegions[F[_]](t: AsyncAdmin, serverName: ServerName)(
    implicit
    M: Applicative[F],
    F: Par.Aux[CompletableFuture, F]
  ): F[Seq[RegionInfo]] =
    M.map(F.parallel(t.getRegions(serverName)))(_.asScala.toSeq)

  def getRegionsByTableName[F[_]](t: AsyncAdmin, tableName: TableName)(
    implicit
    M: Applicative[F],
    F: Par.Aux[CompletableFuture, F]
  ): F[Seq[RegionInfo]] =
    M.map(F.parallel(t.getRegions(tableName)))(_.asScala.toSeq)

  def flush[F[_]](t: AsyncAdmin, tableName: TableName)(
    implicit
    M: Applicative[F],
    F: Par.Aux[CompletableFuture, F]
  ): F[Unit] =
    F.parallel(t.flush(tableName)) *> M.unit

  def flushRegion[F[_]](t: AsyncAdmin, regionName: Array[Byte])(
    implicit
    M: Applicative[F],
    F: Par.Aux[CompletableFuture, F]
  ): F[Unit] =
    F.parallel(t.flushRegion(regionName)) *> M.unit

  def flushRegionServer[F[_]](t: AsyncAdmin, serverName: ServerName)(
    implicit
    M: Applicative[F],
    F: Par.Aux[CompletableFuture, F]
  ): F[Unit] =
    F.parallel(t.flushRegionServer(serverName)) *> M.unit

  def compact[F[_]](t: AsyncAdmin, tableName: TableName)(
    implicit
    M: Applicative[F],
    F: Par.Aux[CompletableFuture, F]
  ): F[Unit] =
    F.parallel(t.compact(tableName)) *> M.unit

  def compactColumn[F[_]](t: AsyncAdmin, tableName: TableName, columnName: Array[Byte])(
    implicit
    M: Applicative[F],
    F: Par.Aux[CompletableFuture, F]
  ): F[Unit] =
    F.parallel(t.compact(tableName, columnName)) *> M.unit

  def compactWithType[F[_]](t: AsyncAdmin, tableName: TableName, compactType: CompactType)(
    implicit
    M: Applicative[F],
    F: Par.Aux[CompletableFuture, F]
  ): F[Unit] =
    F.parallel(t.compact(tableName, compactType)) *> M.unit

  def compactColumnWithType[F[_]](
    t: AsyncAdmin,
    tableName: TableName,
    columnName: Array[Byte],
    compactType: CompactType
  )(
    implicit
    M: Applicative[F],
    F: Par.Aux[CompletableFuture, F]
  ): F[Unit] =
    F.parallel(t.compact(tableName, columnName, compactType)) *> M.unit

  def compactRegion[F[_]](t: AsyncAdmin, regionName: Array[Byte])(
    implicit
    M: Applicative[F],
    F: Par.Aux[CompletableFuture, F]
  ): F[Unit] =
    F.parallel(t.compactRegion(regionName)) *> M.unit

  def compactFamilyRegion[F[_]](t: AsyncAdmin, regionName: Array[Byte], columnFamily: Array[Byte])(
    implicit
    M: Applicative[F],
    F: Par.Aux[CompletableFuture, F]
  ): F[Unit] =
    F.parallel(t.compactRegion(regionName, columnFamily)) *> M.unit

  def majorCompact[F[_]](t: AsyncAdmin, tableName: TableName, compactType: CompactType = CompactType.NORMAL)(
    implicit
    M: Applicative[F],
    F: Par.Aux[CompletableFuture, F]
  ): F[Unit] =
    F.parallel(t.majorCompact(tableName, compactType)) *> M.unit

  def majorCompactFamily[F[_]](
    t: AsyncAdmin,
    tableName: TableName,
    columnFamily: Array[Byte],
    compactType: CompactType = CompactType.NORMAL
  )(
    implicit
    M: Applicative[F],
    F: Par.Aux[CompletableFuture, F]
  ): F[Unit] =
    F.parallel(t.majorCompact(tableName, columnFamily, compactType)) *> M.unit

  def majorCompactRegion[F[_]](t: AsyncAdmin, regionName: Array[Byte])(
    implicit
    M: Applicative[F],
    F: Par.Aux[CompletableFuture, F]
  ): F[Unit] =
    F.parallel(t.majorCompactRegion(regionName)) *> M.unit

  def majorCompactFamilyRegion[F[_]](t: AsyncAdmin, regionName: Array[Byte], columnFamily: Array[Byte])(
    implicit
    M: Applicative[F],
    F: Par.Aux[CompletableFuture, F]
  ): F[Unit] =
    F.parallel(t.majorCompactRegion(regionName, columnFamily)) *> M.unit

  def compactRegionServer[F[_]](t: AsyncAdmin, serverName: ServerName)(
    implicit
    M: Applicative[F],
    F: Par.Aux[CompletableFuture, F]
  ): F[Unit] =
    F.parallel(t.compactRegionServer(serverName)) *> M.unit

  def majorCompactRegionServer[F[_]](t: AsyncAdmin, serverName: ServerName)(
    implicit
    M: Applicative[F],
    F: Par.Aux[CompletableFuture, F]
  ): F[Unit] =
    F.parallel(t.majorCompactRegionServer(serverName)) *> M.unit

//  def mergeSwitch[F[_]](t: AsyncAdmin, on: Boolean)(implicit
//                                                    M: Applicative[F],
//                                                    F: Par.Aux[CompletableFuture,F]): F[Boolean] =
//    M.map(F.parallel(t.mergeSwitch(on)))(_.booleanValue)
//
//  def isMergeEnabled[F[_]](t: AsyncAdmin)(implicit
//                                          M: Applicative[F],
//                                          F: Par.Aux[CompletableFuture,F]): F[Boolean] =
//    M.map(F.parallel(t.isMergeEnabled))(_.booleanValue)
//
//  def splitSwitch[F[_]](t: AsyncAdmin, on: Boolean)(implicit
//                                                    M: Apply[F],
//                                                    F: Par.Aux[CompletableFuture,F]): F[Boolean] =
//    M.map(F.parallel(t.splitSwitch(on)))(_.booleanValue)
//
//  def isSplitEnabled[F[_]](t: AsyncAdmin)(implicit
//                                          M: Apply[F],
//                                          F: Par.Aux[CompletableFuture,F]): F[Boolean] =
//    M.map(F.parallel(t.isSplitEnabled))(_.booleanValue)
//
//  def mergeRegions[F[_]](t: AsyncAdmin,
//                         nameOfRegionA: Array[Byte],
//                         nameOfRegionB: Array[Byte],
//                         forcible: Boolean)(implicit
//                                            M: Applicative[F],
//                                            F: Par.Aux[CompletableFuture,F]): F[Unit] =
//    F.parallel(t.mergeRegions(nameOfRegionA, nameOfRegionB, forcible)) *> M.unit
//
//  def split[F[_]](t: AsyncAdmin, tableName: TableName)(implicit
//                                                       M: Applicative[F],
//                                                       F: Par.Aux[CompletableFuture,F]): F[Unit] =
//    F.parallel(t.split(tableName)) *> M.unit
//
//  def splitRegion[F[_]](t: AsyncAdmin, regionName: Array[Byte])(
//      implicit
//      M: Applicative[F],
//      F: Par.Aux[CompletableFuture,F]): F[Unit] =
//    F.parallel(t.splitRegion(regionName)) *> M.unit
//
//  def splitByPoint[F[_]](t: AsyncAdmin, tableName: TableName, splitPoint: Array[Byte])(
//      implicit
//      M: Applicative[F],
//      F: Par.Aux[CompletableFuture,F]): F[Unit] =
//    F.parallel(t.split(tableName, splitPoint)) *> M.unit
//
//  def splitRegion[F[_]](t: AsyncAdmin, regionName: Array[Byte], splitPoint: Array[Byte])(
//      implicit
//      M: Applicative[F],
//      F: Par.Aux[CompletableFuture,F]): F[Unit] =
//    F.parallel(t.splitRegion(regionName, splitPoint)) *> M.unit
//
//  def assign[F[_]](t: AsyncAdmin, regioName: Array[Byte])(implicit
//                                                          M: Applicative[F],
//                                                          F: Par.Aux[CompletableFuture,F]): F[Unit] =
//    F.parallel(t.assign(regioName)) *> M.unit
//
//  def unassign[F[_]](t: AsyncAdmin, regionName: Array[Byte], forcible: Boolean)(
//      implicit
//      M: Applicative[F],
//      F: Par.Aux[CompletableFuture,F]): F[Unit] =
//    F.parallel(t.unassign(regionName, forcible)) *> M.unit
//
//  def offline[F[_]](t: AsyncAdmin, regioName: Array[Byte])(implicit
//                                                           M: Applicative[F],
//                                                           F: Par.Aux[CompletableFuture,F]): F[Unit] =
//    F.parallel(t.offline(regioName)) *> M.unit
//
//  def move[F[_]](t: AsyncAdmin, regioName: Array[Byte])(implicit
//                                                        M: Applicative[F],
//                                                        F: Par.Aux[CompletableFuture,F]): F[Unit] =
//    F.parallel(t.move(regioName)) *> M.unit
//
//  def moveStrict[F[_]](t: AsyncAdmin, regionName: Array[Byte], dest: ServerName)(
//      implicit
//      M: Applicative[F],
//      F: Par.Aux[CompletableFuture,F]): F[Unit] =
//    F.parallel(t.move(regionName, dest)) *> M.unit
//
//  def setQuota[F[_]](t: AsyncAdmin, settings: QuotaSettings)(implicit
//                                                             M: Applicative[F],
//                                                             F: Par.Aux[CompletableFuture,F]): F[Unit] =
//    F.parallel(t.setQuota(settings)) *> M.unit
//
//  def getQuota[F[_]](t: AsyncAdmin, filter: QuotaFilter)(
//      implicit
//      M: Apply[F],
//      F: Par.Aux[CompletableFuture,F]): F[Seq[QuotaSettings]] =
//    M.map(F.parallel(t.getQuota(filter)))(_.asScala.toSeq)
//
//  def addReplicationPeer[F[_]](t: AsyncAdmin,
//                               peerId: String,
//                               config: ReplicationPeerConfig,
//                               enabled: Boolean = true)(implicit
//                                                        M: Applicative[F],
//                                                        F: Par.Aux[CompletableFuture,F]): F[Unit] =
//    F.parallel(t.addReplicationPeer(peerId, config, enabled)) *> M.unit
//
//  def updateReplicationPeer[F[_]](t: AsyncAdmin, peerId: String, config: ReplicationPeerConfig)(
//      implicit
//      M: Applicative[F],
//      F: Par.Aux[CompletableFuture,F]): F[Unit] =
//    F.parallel(t.updateReplicationPeerConfig(peerId, config)) *> M.unit
//
//  def removeReplicationPeer[F[_]](t: AsyncAdmin, peerId: String)(
//      implicit
//      M: Applicative[F],
//      F: Par.Aux[CompletableFuture,F]): F[Unit] =
//    F.parallel(t.removeReplicationPeer(peerId)) *> M.unit
//
//  def enableReplicationPeer[F[_]](t: AsyncAdmin, peerId: String)(
//      implicit
//      M: Applicative[F],
//      F: Par.Aux[CompletableFuture,F]): F[Unit] =
//    F.parallel(t.enableReplicationPeer(peerId)) *> M.unit
//
//  def disableReplicationPeer[F[_]](t: AsyncAdmin, peerId: String)(
//      implicit
//      M: Applicative[F],
//      F: Par.Aux[CompletableFuture,F]): F[Unit] =
//    F.parallel(t.disableReplicationPeer(peerId)) *> M.unit
//
//  def getReplicationPeer[F[_]](t: AsyncAdmin, peerId: String)(
//      implicit
//      F: Par.Aux[CompletableFuture,F]): F[ReplicationPeerConfig] =
//    F.parallel(t.getReplicationPeerConfig(peerId))
//
//  def appendReplicationPeerTableCFs[F[_]](t: AsyncAdmin,
//                                          peerId: String,
//                                          tableCfs: Map[TableName, List[String]])(
//      implicit
//      M: Applicative[F],
//      F: Par.Aux[CompletableFuture,F]): F[Unit] =
//    F.parallel(t.appendReplicationPeerTableCFs(peerId, tableCfs.mapValues(_.asJava).asJava)) *> M.unit
//
//  def removeReplicationPeerTableCFs[F[_]](t: AsyncAdmin,
//                                          peerId: String,
//                                          tableCfs: Map[TableName, List[String]])(
//      implicit
//      M: Applicative[F],
//      F: Par.Aux[CompletableFuture,F]): F[Unit] =
//    F.parallel(t.removeReplicationPeerTableCFs(peerId, tableCfs.mapValues(_.asJava).asJava)) *> M.unit
//
//  def listReplicationPeers[F[_]](t: AsyncAdmin)(
//      implicit
//      M: Apply[F],
//      F: CompletableFuture ~> F): F[Seq[ReplicationPeerDescription]] =
//    M.map(F.parallel(t.listReplicationPeers))(_.asScala.toSeq)
//
//  def listReplicationPeersByPattern[F[_]](t: AsyncAdmin, pattern: Pattern)(
//      implicit
//      M: Apply[F],
//      F: Par.Aux[CompletableFuture,F]): F[Seq[ReplicationPeerDescription]] =
//    M.map(F.parallel(t.listReplicationPeers(pattern)))(_.asScala.toSeq)
//
//  def listReplicatedTableCFs[F[_]](t: AsyncAdmin)(implicit
//                                                  M: Apply[F],
//                                                  F: Par.Aux[CompletableFuture,F]): F[Seq[TableCFs]] =
//    M.map(F.parallel(t.listReplicatedTableCFs))(_.asScala.toSeq)
//
//  def enableTableReplication[F[_]](t: AsyncAdmin, tableName: TableName)(
//      implicit
//      M: Applicative[F],
//      F: Par.Aux[CompletableFuture,F]): F[Unit] =
//    F.parallel(t.enableTableReplication(tableName)) *> M.unit
//
//  def disableTableReplication[F[_]](t: AsyncAdmin, tableName: TableName)(
//      implicit
//      M: Applicative[F],
//      F: CompletableFuture ~> F): F[Unit] =
//    F.parallel(t.disableTableReplication(tableName)) *> M.unit
//
//  def snapshot2[F[_]](t: AsyncAdmin,
//                      snapshotName: String,
//                      tableName: TableName,
//                      snapshotType: SnapshotType = SnapshotType.FLUSH)(
//      implicit
//      M: Applicative[F],
//      F: Par.Aux[CompletableFuture,F]): F[Unit] =
//    F.parallel(t.snapshot(snapshotName, tableName, snapshotType)) *> M.unit
//
//  def snapshot[F[_]](t: AsyncAdmin, description: SnapshotDescription)(
//      implicit
//      M: Applicative[F],
//      F: Par.Aux[CompletableFuture,F]): F[Unit] =
//    F.parallel(t.snapshot(description)) *> M.unit
//
//  def isSnapshotFinished[F[_]](t: AsyncAdmin, description: SnapshotDescription)(
//      implicit
//      M: Apply[F],
//      F: Par.Aux[CompletableFuture,F]): F[Boolean] =
//    M.map(F.parallel(t.isSnapshotFinished(description)))(_.booleanValue)
//
//  def restoreSnapshot[F[_]](t: AsyncAdmin, snapshotName: String)(
//      implicit
//      M: Applicative[F],
//      F: Par.Aux[CompletableFuture,F]): F[Unit] =
//    F.parallel(t.restoreSnapshot(snapshotName)) *> M.unit
//
//  def restoreSnapshotWithFailSafe[F[_]](t: AsyncAdmin,
//                                        snapshotName: String,
//                                        takeFailSafeSnapshot: Boolean)(
//      implicit
//      M: Applicative[F],
//      F: Par.Aux[CompletableFuture,F]): F[Unit] =
//    F.parallel(t.restoreSnapshot(snapshotName, takeFailSafeSnapshot)) *> M.unit
//
//  def cloneSnapshot[F[_]](t: AsyncAdmin, snapshotName: String, tableName: TableName)(
//      implicit
//      M: Applicative[F],
//      F: Par.Aux[CompletableFuture,F]): F[Unit] =
//    F.parallel(t.cloneSnapshot(snapshotName, tableName)) *> M.unit
//
//  def listSnapshots[F[_]](t: AsyncAdmin)(implicit
//                                         M: Apply[F],
//                                         F: Par.Aux[CompletableFuture,F]): F[Seq[SnapshotDescription]] =
//    M.map(F.parallel(t.listSnapshots))(_.asScala.toSeq)
//
//  def listSnapshotsByPattern[F[_]](t: AsyncAdmin, pattern: Pattern)(
//      implicit
//      M: Apply[F],
//      F: CompletableFuture ~> F): F[Seq[SnapshotDescription]] =
//    M.map(F.parallel(t.listSnapshots(pattern)))(_.asScala.toSeq)
//
//  def listTableSnapshots[F[_]](t: AsyncAdmin, pattern: Pattern)(
//      implicit
//      M: Apply[F],
//      F: Par.Aux[CompletableFuture,F]): F[Seq[SnapshotDescription]] =
//    M.map(F.parallel(t.listTableSnapshots(pattern)))(_.asScala.toSeq)
//
//  def listTableSnapshotsByPattern[F[_]](t: AsyncAdmin, pattern: Pattern, snapshotPattern: Pattern)(
//      implicit
//      M: Apply[F],
//      F: Par.Aux[CompletableFuture,F]): F[Seq[SnapshotDescription]] =
//    M.map(F.parallel(t.listTableSnapshots(pattern, snapshotPattern)))(_.asScala.toSeq)
//
//  def deleteSnapshots[F[_]](t: AsyncAdmin)(implicit
//                                           M: Applicative[F],
//                                           F: Par.Aux[CompletableFuture,F]): F[Unit] =
//    F.parallel(t.deleteSnapshots()) *> M.unit
//
//  def deleteSnapshotsByPattern[F[_]](t: AsyncAdmin, pattern: Pattern)(
//      implicit
//      M: Applicative[F],
//      F: Par.Aux[CompletableFuture,F]): F[Unit] =
//    F.parallel(t.deleteSnapshots(pattern)) *> M.unit
//
//  def deleteSnapshot[F[_]](t: AsyncAdmin, snapshotName: String)(
//      implicit
//      M: Applicative[F],
//      F: Par.Aux[CompletableFuture,FPar.Aux[CompletableFuture,FPar.Aux[CompletableFuture,F]
//    F.parallel(t.deleteSnapshot(snapshotName)) *> M.unit
//
//  def deleteTableSnapshots[F[_]](t: AsyncAdmin, pattern: Pattern)(
//      implicit
//      M: Applicative[F],
//      F: CompletableFuture ~> F): F[Unit] =
//    F.parallel(t.deleteTableSnapshots(pattern)) *> M.unit
//
//  def deleteTableSnapshotsWithSnapshotPattern[F[_]](t: AsyncAdmin,
//                                                    pattern: Pattern,
//                                                    snapshotPattern: Pattern)(
//      implicit
//      M: Applicative[F],
//      F: Par.Aux[CompletableFuture,F]): F[Unit] =
//    F.parallel(t.deleteTableSnapshots(pattern, snapshotPattern)) *> M.unit
//
//  def execProcedure[F[_]](t: AsyncAdmin,
//                          signature: String,
//                          instance: String,
//                          props: Map[String, String])(implicit
//                                                      M: Applicative[F],
//                                                      F: Par.Aux[CompletableFuture,FPar.Aux[CompletableFuture,F]
//    F.parallel(t.execProcedure(signature, instance, props.asJava)) *> M.unit
//
//  def execProcedureWithReturn[F[_]](t: AsyncAdmin,
//                                    signature: String,
//                                    instance: String,
//                                    props: Map[String, String])(
//      implicit
//      F: CompletableFuture ~> F): F[Array[Byte]] =
//    F.parallel(t.execProcedureWithReturn(signature, instance, props.asJava))
//
//  def isProcedureFinished[F[_]](t: AsyncAdmin,
//                                signature: String,
//                                instance: String,
//                                props: Map[String, String])(implicit
//                                                            M: Apply[F],
//                                                            F: Par.Aux[CompletableFuture,F]): F[Boolean] =
//    M.map(F.parallel(t.isProcedureFinished(signature, instance, props.asJava)))(_.booleanValue)
//
//  def abortProcedure[F[_]](t: AsyncAdmin, procId: Long, mayInterruptIfRunning: Boolean)(
//      implicit
//      M: Apply[F],
//      F: CompletableFuture ~> F): F[Boolean] =
//    M.map(F.parallel(t.abortProcedure(procId, mayInterruptIfRunning)))(_.booleanValue)
//
//  def getProcedures[F[_]](t: AsyncAdmin)(implicit
//                                         F: Par.Aux[CompletableFuture,F]): F[String] =
//    F.parallel(t.getProcedures)
//
//  def getLocks[F[_]](t: AsyncAdmin)(implicit
//                                    F: Par.Aux[CompletableFuture,F]): F[String] =
//    F.parallel(t.getLocks)
//
//  def decommissionRegionServers[F[_]](t: AsyncAdmin, servers: Seq[ServerName], offload: Boolean)(
//      implicit
//      M: Applicative[F],
//      F: Par.Aux[CompletableFuture,F]): F[Unit] =
//    F.parallel(t.decommissionRegionServers(servers.asJava, offload)) *> M.unit
//
//  def listDecommissionedRegionServers[F[_]](t: AsyncAdmin)(
//      implicit
//      M: Apply[F],
//      F: CompletableFuture ~> F): F[Seq[ServerName]] =
//    M.map(F.parallel(t.listDecommissionedRegionServers))(_.asScala.toSeq)
//
//  def recommissionRegionServer[F[_]](t: AsyncAdmin,
//                                     server: ServerName,
//                                     encodedRegionNames: Seq[Array[Byte]])(
//      implicit
//      M: Applicative[F],
//      F: Par.Aux[CompletableFuture,F]): F[Unit] =
//    F.parallel(t.recommissionRegionServer(server, encodedRegionNames.asJava)) *> M.unit
//
//  def getClusterMetrics[F[_]](t: AsyncAdmin)(implicit
//                                             F: Par.Aux[CompletableFuture,F]): F[ClusterMetrics] =
//    F.parallel(t.getClusterMetrics)
//
//  def getClusterMetricsWithOptions[F[_]](t: AsyncAdmin, options: ju.EnumSet[CMOption])(
//      implicit
//      F: CompletableFuture ~> F): F[ClusterMetrics] =
//    F.parallel(t.getClusterMetrics(options))
//
//  def getMaster[F[_]](t: AsyncAdmin)(implicit
//                                     F: CompletableFuture ~> F): F[ServerName] =
//    F.parallel(t.getMaster)
//
//  def getBackupMasters[F[_]](t: AsyncAdmin)(implicit
//                                            M: Apply[F],
//                                            F: Par.Aux[CompletableFuture,FPar.Aux[CompletableFuture,F]ame]] =
//    M.map(F.parallel(t.getBackupMasters))(_.asScala.toSeq)
//
//  def getRegionServers[F[_]](t: AsyncAdmin)(implicit
//                                            M: Apply[F],
//                                            F: Par.Aux[CompletableFuture,F]): F[Iterable[ServerName]] =
//    M.map(F.parallel(t.getRegionServers))(_.asScala.toSeq)
//
//  def getMasterCoprocessorNames[F[_]](t: AsyncAdmin)(implicit
//                                                     M: Apply[F],
//                                                     F: Par.Aux[CompletableFuture,F]): F[Seq[String]] =
//    M.map(F.parallel(t.getMasterCoprocessorNames))(_.asScala.toSeq)
//
//  def shutdown[F[_]](t: AsyncAdmin)(implicit
//                                    M: Applicative[F],
//                                    F: Par.Aux[CompletableFuture,F]): F[Unit] =
//    F.parallel(t.shutdown) *> M.unit
//
//  def stopMaster[F[_]](t: AsyncAdmin)(implicit
//                                      M: Applicative[F],
//                                      F: Par.Aux[CompletableFuture,F]): F[Unit] =
//    F.parallel(t.stopMaster) *> M.unit
//
//  def stopRegionServer[F[_]](t: AsyncAdmin, serverName: ServerName)(
//      implicit
//      M: Applicative[F],
//      F: Par.Aux[CompletableFuture,F]): F[Unit] =
//    F.parallel(t.stopRegionServer(serverName)) *> M.unit
//
//  def updateConfiguration[F[_]](t: AsyncAdmin)(implicit
//                                               M: Applicative[F],
//                                               F: CompletableFuture ~> F): F[Unit] =
//    F.parallel(t.updateConfiguration()) *> M.unit
//
//  def updateConfigurationWithName[F[_]](t: AsyncAdmin, serverName: ServerName)(
//      implicit
//      M: Applicative[F],
//      F: CompletableFuture ~> F): F[Unit] =
//    F.parallel(t.updateConfiguration(serverName)) *> M.unit
//
//  def clearCompactionQueues[F[_]](t: AsyncAdmin, serverName: ServerName, queues: Set[String])(
//      implicit
//      M: Applicative[F],
//      F: Par.Aux[CompletableFuture,F]): F[Unit] =
//    F.parallel(t.clearCompactionQueues(serverName, queues.asJava)) *> M.unit
//
//  def getRegionMetrics[F[_]](t: AsyncAdmin, serverName: ServerName)(
//      implicit
//      M: Apply[F],
//      F: CompletableFuture ~> F): F[Seq[RegionMetrics]] =
//    M.map(F.parallel(t.getRegionMetrics(serverName)))(_.asScala.toSeq)
//
//  def getRegionMetricsWithTable[F[_]](t: AsyncAdmin, serverName: ServerName, tableName: TableName)(
//      implicit
//      M: Apply[F],
//      F: Par.Aux[CompletableFuture,F]): F[Seq[RegionMetrics]] =
//    M.map(F.parallel(t.getRegionMetrics(serverName, tableName)))(_.asScala.toSeq)
//
//  def isMasterInMaintenanceMode[F[_]](t: AsyncAdmin)(implicit
//                                                     M: Apply[F],
//                                                     F: CompletableFuture ~> F): F[Boolean] =
//    M.map(F.parallel(t.isMasterInMaintenanceMode))(_.booleanValue())
//
//  def getCompactionState[F[_]](t: AsyncAdmin, tableName: TableName)(
//      implicit
//      F: Par.Aux[CompletableFuture,F]): F[CompactionState] =
//    F.parallel(t.getCompactionState(tableName))
//
//  def getCompactionStateWithType[F[_]](t: AsyncAdmin,
//                                       tableName: TableName,
//                                       compactType: CompactType)(
//      implicit
//      F: Par.Aux[CompletableFuture,FPar.Aux[CompletableFuture,FPar.Aux[CompletableFuture,F]
//    F.parallel(t.getCompactionState(tableName, compactType))
//
//  def getCompactionStateForRegion[F[_]](t: AsyncAdmin, regionName: Array[Byte])(
//      implicit
//      F: Par.Aux[CompletableFuture,F]): F[CompactionState] =
//    F.parallel(t.getCompactionStateForRegion(regionName))
//
//  def getLastMajorCompactionTimestamp[F[_]](t: AsyncAdmin, tableName: TableName)(
//      implicit
//      M: Apply[F],
//      F: Par.Aux[CompletableFuture,F]): F[Option[Long]] =
//    M.map(F.parallel(t.getLastMajorCompactionTimestamp(tableName)))(r =>
//      if (r.isPresent) Option(r.get.longValue) else None)
//
//  def getLastMajorCompactionTimestampForRegion[F[_]](t: AsyncAdmin, regionName: Array[Byte])(
//      implicit
//      M: Apply[F],
//      F: CompletableFuture ~> F): F[Option[Long]] =
//    M.map(F.parallel(t.getLastMajorCompactionTimestampForRegion(regionName)))(r =>
//      if (r.isPresent) Option(r.get.longValue) else None)
//
//  def getSecurityCapabilities[F[_]](t: AsyncAdmin)(
//      implicit
//      M: Apply[F],
//      F: Par.Aux[CompletableFuture,F]): F[Seq[SecurityCapability]] =
//    M.map(F.parallel(t.getSecurityCapabilities))(_.asScala.toSeq)
//
//  def balancerSwitch[F[_]](t: AsyncAdmin, on: Boolean)(implicit
//                                                       M: Apply[F],
//                                                       F: CompletableFuture ~> F): F[Boolean] =
//    M.map(F.parallel(t.balancerSwitch(on)))(_.booleanValue)
//
//  def balance[F[_]](t: AsyncAdmin, forcible: Boolean = false)(
//      implicit
//      M: Apply[F],
//      F: Par.Aux[CompletableFuture,F]): F[Boolean] =
//    M.map(F.parallel(t.balance(forcible)))(_.booleanValue)
//
//  def isBalancerEnabled[F[_]](t: AsyncAdmin)(implicit
//                                             M: Apply[F],
//                                             F: CompletableFuture ~> F): F[Boolean] =
//    M.map(F.parallel(t.isBalancerEnabled))(_.booleanValue)
//
//  def normalizerSwitch[F[_]](t: AsyncAdmin, on: Boolean)(implicit
//                                                         M: Apply[F],
//                                                         F: Par.Aux[CompletableFuture,F]): F[Boolean] =
//    M.map(F.parallel(t.normalizerSwitch(on)))(_.booleanValue)
//
//  def isNormalizerEnabled[F[_]](t: AsyncAdmin)(implicit
//                                               M: Apply[F],
//                                               F: Par.Aux[CompletableFuture,F]): F[Boolean] =
//    M.map(F.parallel(t.isNormalizerEnabled))(_.booleanValue)
//
//  def normalize[F[_]](t: AsyncAdmin)(implicit
//                                     M: Apply[F],
//                                     F: Par.Aux[CompletableFuture,F]): F[Boolean] =
//    M.map(F.parallel(t.normalize))(_.booleanValue)
//
//  def cleanerChoreSwitch[F[_]](t: AsyncAdmin, on: Boolean)(implicit
//                                                           M: Apply[F],
//                                                           F: Par.Aux[CompletableFuture,F]): F[Boolean] =
//    M.map(F.parallel(t.cleanerChoreSwitch(on)))(_.booleanValue)
//
//  def isCleanerChoreEnabled[F[_]](t: AsyncAdmin)(implicit
//                                                 M: Apply[F],
//                                                 F: CompletableFuture ~> F): F[Boolean] =
//    M.map(F.parallel(t.isCleanerChoreEnabled))(_.booleanValue)
//
//  def runCleanerChore[F[_]](t: AsyncAdmin)(implicit
//                                           M: Apply[F],
//                                           F: CompletableFuture ~> F): F[Boolean] =
//    M.map(F.parallel(t.runCleanerChore))(_.booleanValue)
//
//  def catalogJanitorSwitch[F[_]](t: AsyncAdmin, on: Boolean)(
//      implicit
//      M: Apply[F],
//      F: Par.Aux[CompletableFuture,F]): F[Boolean] =
//    M.map(F.parallel(t.catalogJanitorSwitch(on)))(_.booleanValue)
//
//  def isCatalogJanitorEnabled[F[_]](t: AsyncAdmin)(implicit
//                                                   M: Apply[F],
//                                                   F: Par.Aux[CompletableFuture,FPar.Aux[CompletableFuture,F]
//    M.map(F.parallel(t.isCatalogJanitorEnabled))(_.booleanValue)
//
//  def runCatalogJanitor[F[_]](t: AsyncAdmin)(implicit
//                                             M: Apply[F],
//                                             F: CompletableFuture ~> F): F[Int] =
//    M.map(F.parallel(t.runCatalogJanitor))(_.intValue)
//
//  def coprocessorService[F[_], S, R](t: AsyncAdmin,
//                                     stubMaker: RpcChannel => S,
//                                     callable: ServiceCaller[S, R])(
//      implicit
//      F: Par.Aux[CompletableFuture,F]): F[R] =
//    F.parallel(t.coprocessorService[S, R](stubMaker.asJava, callable))
//
//  def coprocessorServiceFor[F[_], S, R](t: AsyncAdmin,
//                                        stubMaker: RpcChannel => S,
//                                        callable: ServiceCaller[S, R],
//                                        serverName: ServerName)(implicit
//                                                                F: Par.Aux[CompletableFuture,F]): F[R] =
//    F.parallel(t.coprocessorService[S, R](stubMaker.asJava, callable, serverName))
//
//  def listDeadServers[F[_]](t: AsyncAdmin)(
//      implicit
//      M: Apply[F],
//      F: Par.Aux[CompletableFuture,F]
//  ): F[Seq[ServerName]] =
//    M.map(F.parallel(t.listDeadServers))(_.asScala.toSeq)
//
//  def clearDeadServers[F[_]](t: AsyncAdmin, servers: Seq[ServerName])(
//      implicit
//      M: Apply[F],
//      F: Par.Aux[CompletableFuture,F]
//  ): F[Seq[ServerName]] =
//    M.map(F.parallel(t.clearDeadServers(servers.asJava)))(_.asScala.toSeq)
//
//  def clearBlockCache[F[_]](t: AsyncAdmin, tableName: TableName)(
//      implicit
//      F: Par.Aux[CompletableFuture,F]
//  ): F[CacheEvictionStats] =
//    F.parallel(t.clearBlockCache(tableName))
}
