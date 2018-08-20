package orcus

//import java.{util => ju}
import java.util.concurrent.CompletableFuture
import java.util.regex.Pattern

import cats.{Applicative, Functor, ~>}
import cats.syntax.apply._
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
      F: CompletableFuture ~> F
  ): F[Boolean] =
    M.map(F(t.tableExists(tableName)))(_.booleanValue)

  def listTableDescriptors[F[_]](t: AsyncAdmin, includeSysTables: Boolean = false)(
      implicit
      M: Functor[F],
      F: CompletableFuture ~> F
  ): F[Seq[TableDescriptor]] =
    M.map(F(t.listTableDescriptors(includeSysTables)))(_.asScala)

  def listTableDescriptorsByPattern[F[_]](t: AsyncAdmin, pattern: Pattern, includeSysTables: Boolean = false)(
      implicit
      M: Functor[F],
      F: CompletableFuture ~> F
  ): F[Seq[TableDescriptor]] =
    M.map(F(t.listTableDescriptors(pattern, includeSysTables)))(_.asScala)

  def listTableDescriptorsByNamespace[F[_]](t: AsyncAdmin, namespace: String)(
      implicit
      M: Functor[F],
      F: CompletableFuture ~> F
  ): F[Seq[TableDescriptor]] =
    M.map(F(t.listTableDescriptorsByNamespace(namespace)))(_.asScala)

  def listTableNames[F[_]](t: AsyncAdmin, includeSysTables: Boolean)(implicit
                                                                     M: Functor[F],
                                                                     F: CompletableFuture ~> F): F[Seq[TableName]] =
    M.map(F(t.listTableNames(includeSysTables)))(_.asScala)

  def listTableNamesByPattern[F[_]](t: AsyncAdmin, pattern: Pattern, includeSysTables: Boolean)(
      implicit
      M: Functor[F],
      F: CompletableFuture ~> F): F[Seq[TableName]] =
    M.map(F(t.listTableNames(pattern, includeSysTables)))(_.asScala)

  def getDescriptor[F[_]](t: AsyncAdmin, tableName: TableName)(implicit
                                                               F: CompletableFuture ~> F): F[TableDescriptor] =
    F(t.getDescriptor(tableName))

  def createTable[F[_]](t: AsyncAdmin, tableDescriptor: TableDescriptor)(implicit
                                                                         M: Applicative[F],
                                                                         F: CompletableFuture ~> F): F[Unit] =
    F(t.createTable(tableDescriptor)) *> M.unit

  def createTableWithKeyRange[F[_]](t: AsyncAdmin,
                                    tableDescriptor: TableDescriptor,
                                    startKey: Array[Byte],
                                    endKey: Array[Byte],
                                    numRegions: Int)(implicit
                                                     M: Applicative[F],
                                                     F: CompletableFuture ~> F): F[Unit] =
    F(t.createTable(tableDescriptor, startKey, endKey, numRegions)) *> M.unit

  def createTableWithSplitKeys[F[_]](t: AsyncAdmin, tableDescriptor: TableDescriptor, splitKeys: Array[Array[Byte]])(
      implicit
      M: Applicative[F],
      F: CompletableFuture ~> F): F[Unit] =
    F(t.createTable(tableDescriptor, splitKeys)) *> M.unit

  def modifyTable[F[_]](t: AsyncAdmin, tableDescriptor: TableDescriptor)(implicit
                                                                         M: Applicative[F],
                                                                         F: CompletableFuture ~> F): F[Unit] =
    F(t.modifyTable(tableDescriptor)) *> M.unit

  def deleteTable[F[_]](t: AsyncAdmin, tableName: TableName)(implicit
                                                             M: Applicative[F],
                                                             F: CompletableFuture ~> F): F[Unit] =
    F(t.deleteTable(tableName)) *> M.unit

  def truncateTable[F[_]](t: AsyncAdmin, tableName: TableName, preserveSplits: Boolean)(
      implicit
      M: Applicative[F],
      F: CompletableFuture ~> F): F[Unit] =
    F(t.truncateTable(tableName, preserveSplits)) *> M.unit

  def enableTable[F[_]](t: AsyncAdmin, tableName: TableName)(implicit
                                                             M: Applicative[F],
                                                             F: CompletableFuture ~> F): F[Unit] =
    F(t.enableTable(tableName)) *> M.unit

  def disableTable[F[_]](t: AsyncAdmin, tableName: TableName)(implicit
                                                              M: Applicative[F],
                                                              F: CompletableFuture ~> F): F[Unit] =
    F(t.disableTable(tableName)) *> M.unit

  def isTableEnabled[F[_]](t: AsyncAdmin, tableName: TableName)(implicit
                                                                M: Functor[F],
                                                                F: CompletableFuture ~> F): F[Boolean] =
    M.map(F(t.isTableEnabled(tableName)))(_.booleanValue)

  def isTableAvailable[F[_]](t: AsyncAdmin, tableName: TableName)(implicit
                                                                  M: Functor[F],
                                                                  F: CompletableFuture ~> F): F[Boolean] =
    M.map(F(t.isTableAvailable(tableName)))(_.booleanValue)

  def isTableAvailableWithSplitKeys[F[_]](t: AsyncAdmin, tableName: TableName, splitKeys: Array[Array[Byte]])(
      implicit
      M: Functor[F],
      F: CompletableFuture ~> F): F[Boolean] =
    M.map(F(t.isTableAvailable(tableName, splitKeys)))(_.booleanValue)

  def addColumnFamily[F[_]](t: AsyncAdmin, tableName: TableName, columnFamilyDescriptor: ColumnFamilyDescriptor)(
      implicit
      M: Applicative[F],
      F: CompletableFuture ~> F): F[Unit] =
    F(t.addColumnFamily(tableName, columnFamilyDescriptor)) *> M.unit

  def deleteColumnFamily[F[_]](t: AsyncAdmin, tableName: TableName, columnFamily: Array[Byte])(
      implicit
      M: Applicative[F],
      F: CompletableFuture ~> F): F[Unit] =
    F(t.deleteColumnFamily(tableName, columnFamily)) *> M.unit

  def modifyColumnFamily[F[_]](t: AsyncAdmin, tableName: TableName, columnFamilyDescriptor: ColumnFamilyDescriptor)(
      implicit
      M: Applicative[F],
      F: CompletableFuture ~> F): F[Unit] =
    F(t.modifyColumnFamily(tableName, columnFamilyDescriptor)) *> M.unit

  def createNamespace[F[_]](t: AsyncAdmin, namespaceDescriptor: NamespaceDescriptor)(
      implicit
      M: Applicative[F],
      F: CompletableFuture ~> F): F[Unit] =
    F(t.createNamespace(namespaceDescriptor)) *> M.unit

  def modifyNamespace[F[_]](t: AsyncAdmin, namespaceDescriptor: NamespaceDescriptor)(
      implicit
      M: Applicative[F],
      F: CompletableFuture ~> F): F[Unit] =
    F(t.modifyNamespace(namespaceDescriptor)) *> M.unit

  def deleteNamespace[F[_]](t: AsyncAdmin, namespace: String)(implicit
                                                              M: Applicative[F],
                                                              F: CompletableFuture ~> F): F[Unit] =
    F(t.deleteNamespace(namespace)) *> M.unit

  def getNamespaceDescriptor[F[_]](t: AsyncAdmin, namespace: String)(
      implicit
      F: CompletableFuture ~> F): F[NamespaceDescriptor] =
    F(t.getNamespaceDescriptor(namespace))

  def listNamespaceDescriptors[F[_]](t: AsyncAdmin)(implicit
                                                    M: Applicative[F],
                                                    F: CompletableFuture ~> F): F[Seq[NamespaceDescriptor]] =
    M.map(F(t.listNamespaceDescriptors))(_.asScala)

  def getRegions[F[_]](t: AsyncAdmin, serverName: ServerName)(implicit
                                                              M: Applicative[F],
                                                              F: CompletableFuture ~> F): F[Seq[RegionInfo]] =
    M.map(F(t.getRegions(serverName)))(_.asScala)

  def getRegionsByTableName[F[_]](t: AsyncAdmin, tableName: TableName)(implicit
                                                                       M: Applicative[F],
                                                                       F: CompletableFuture ~> F): F[Seq[RegionInfo]] =
    M.map(F(t.getRegions(tableName)))(_.asScala)

  def flush[F[_]](t: AsyncAdmin, tableName: TableName)(implicit
                                                       M: Applicative[F],
                                                       F: CompletableFuture ~> F): F[Unit] =
    F(t.flush(tableName)) *> M.unit

  def flushRegion[F[_]](t: AsyncAdmin, regionName: Array[Byte])(implicit
                                                                M: Applicative[F],
                                                                F: CompletableFuture ~> F): F[Unit] =
    F(t.flushRegion(regionName)) *> M.unit

  def flushRegionServer[F[_]](t: AsyncAdmin, serverName: ServerName)(implicit
                                                                     M: Applicative[F],
                                                                     F: CompletableFuture ~> F): F[Unit] =
    F(t.flushRegionServer(serverName)) *> M.unit

  def compact[F[_]](t: AsyncAdmin, tableName: TableName)(implicit
                                                         M: Applicative[F],
                                                         F: CompletableFuture ~> F): F[Unit] =
    F(t.compact(tableName)) *> M.unit

  def compactColumn[F[_]](t: AsyncAdmin, tableName: TableName, columnName: Array[Byte])(
      implicit
      M: Applicative[F],
      F: CompletableFuture ~> F): F[Unit] =
    F(t.compact(tableName, columnName)) *> M.unit

  def compactWithType[F[_]](t: AsyncAdmin, tableName: TableName, compactType: CompactType)(
      implicit
      M: Applicative[F],
      F: CompletableFuture ~> F): F[Unit] =
    F(t.compact(tableName, compactType)) *> M.unit

  def compactColumnWithType[F[_]](t: AsyncAdmin,
                                  tableName: TableName,
                                  columnName: Array[Byte],
                                  compactType: CompactType)(implicit
                                                            M: Applicative[F],
                                                            F: CompletableFuture ~> F): F[Unit] =
    F(t.compact(tableName, columnName, compactType)) *> M.unit

  def compactRegion[F[_]](t: AsyncAdmin, regionName: Array[Byte])(implicit
                                                                  M: Applicative[F],
                                                                  F: CompletableFuture ~> F): F[Unit] =
    F(t.compactRegion(regionName)) *> M.unit

  def compactFamilyRegion[F[_]](t: AsyncAdmin, regionName: Array[Byte], columnFamily: Array[Byte])(
      implicit
      M: Applicative[F],
      F: CompletableFuture ~> F): F[Unit] =
    F(t.compactRegion(regionName, columnFamily)) *> M.unit

  def majorCompact[F[_]](t: AsyncAdmin, tableName: TableName, compactType: CompactType = CompactType.NORMAL)(
      implicit
      M: Applicative[F],
      F: CompletableFuture ~> F): F[Unit] =
    F(t.majorCompact(tableName, compactType)) *> M.unit

  def majorCompactFamily[F[_]](t: AsyncAdmin,
                               tableName: TableName,
                               columnFamily: Array[Byte],
                               compactType: CompactType = CompactType.NORMAL)(implicit
                                                                              M: Applicative[F],
                                                                              F: CompletableFuture ~> F): F[Unit] =
    F(t.majorCompact(tableName, columnFamily, compactType)) *> M.unit

  def majorCompactRegion[F[_]](t: AsyncAdmin, regionName: Array[Byte])(implicit
                                                                       M: Applicative[F],
                                                                       F: CompletableFuture ~> F): F[Unit] =
    F(t.majorCompactRegion(regionName)) *> M.unit

  def majorCompactFamilyRegion[F[_]](t: AsyncAdmin, regionName: Array[Byte], columnFamily: Array[Byte])(
      implicit
      M: Applicative[F],
      F: CompletableFuture ~> F): F[Unit] =
    F(t.majorCompactRegion(regionName, columnFamily)) *> M.unit

  def compactRegionServer[F[_]](t: AsyncAdmin, serverName: ServerName)(implicit
                                                                       M: Applicative[F],
                                                                       F: CompletableFuture ~> F): F[Unit] =
    F(t.compactRegionServer(serverName)) *> M.unit

  def majorCompactRegionServer[F[_]](t: AsyncAdmin, serverName: ServerName)(implicit
                                                                            M: Applicative[F],
                                                                            F: CompletableFuture ~> F): F[Unit] =
    F(t.majorCompactRegionServer(serverName)) *> M.unit

//  def mergeSwitch[F[_]](t: AsyncAdmin, on: Boolean)(implicit
//                                                    M: Applicative[F],
//                                                    F: CompletableFuture ~> F): F[Boolean] =
//    M.map(F(t.mergeSwitch(on)))(_.booleanValue)
//
//  def isMergeEnabled[F[_]](t: AsyncAdmin)(implicit
//                                          M: Applicative[F],
//                                          F: CompletableFuture ~> F): F[Boolean] =
//    M.map(F(t.isMergeEnabled))(_.booleanValue)
//
//  def splitSwitch[F[_]](t: AsyncAdmin, on: Boolean)(implicit
//                                                    M: Apply[F],
//                                                    F: CompletableFuture ~> F): F[Boolean] =
//    M.map(F(t.splitSwitch(on)))(_.booleanValue)
//
//  def isSplitEnabled[F[_]](t: AsyncAdmin)(implicit
//                                          M: Apply[F],
//                                          F: CompletableFuture ~> F): F[Boolean] =
//    M.map(F(t.isSplitEnabled))(_.booleanValue)
//
//  def mergeRegions[F[_]](t: AsyncAdmin,
//                         nameOfRegionA: Array[Byte],
//                         nameOfRegionB: Array[Byte],
//                         forcible: Boolean)(implicit
//                                            M: Applicative[F],
//                                            F: CompletableFuture ~> F): F[Unit] =
//    F(t.mergeRegions(nameOfRegionA, nameOfRegionB, forcible)) *> M.unit
//
//  def split[F[_]](t: AsyncAdmin, tableName: TableName)(implicit
//                                                       M: Applicative[F],
//                                                       F: CompletableFuture ~> F): F[Unit] =
//    F(t.split(tableName)) *> M.unit
//
//  def splitRegion[F[_]](t: AsyncAdmin, regionName: Array[Byte])(
//      implicit
//      M: Applicative[F],
//      F: CompletableFuture ~> F): F[Unit] =
//    F(t.splitRegion(regionName)) *> M.unit
//
//  def splitByPoint[F[_]](t: AsyncAdmin, tableName: TableName, splitPoint: Array[Byte])(
//      implicit
//      M: Applicative[F],
//      F: CompletableFuture ~> F): F[Unit] =
//    F(t.split(tableName, splitPoint)) *> M.unit
//
//  def splitRegion[F[_]](t: AsyncAdmin, regionName: Array[Byte], splitPoint: Array[Byte])(
//      implicit
//      M: Applicative[F],
//      F: CompletableFuture ~> F): F[Unit] =
//    F(t.splitRegion(regionName, splitPoint)) *> M.unit
//
//  def assign[F[_]](t: AsyncAdmin, regioName: Array[Byte])(implicit
//                                                          M: Applicative[F],
//                                                          F: CompletableFuture ~> F): F[Unit] =
//    F(t.assign(regioName)) *> M.unit
//
//  def unassign[F[_]](t: AsyncAdmin, regionName: Array[Byte], forcible: Boolean)(
//      implicit
//      M: Applicative[F],
//      F: CompletableFuture ~> F): F[Unit] =
//    F(t.unassign(regionName, forcible)) *> M.unit
//
//  def offline[F[_]](t: AsyncAdmin, regioName: Array[Byte])(implicit
//                                                           M: Applicative[F],
//                                                           F: CompletableFuture ~> F): F[Unit] =
//    F(t.offline(regioName)) *> M.unit
//
//  def move[F[_]](t: AsyncAdmin, regioName: Array[Byte])(implicit
//                                                        M: Applicative[F],
//                                                        F: CompletableFuture ~> F): F[Unit] =
//    F(t.move(regioName)) *> M.unit
//
//  def moveStrict[F[_]](t: AsyncAdmin, regionName: Array[Byte], dest: ServerName)(
//      implicit
//      M: Applicative[F],
//      F: CompletableFuture ~> F): F[Unit] =
//    F(t.move(regionName, dest)) *> M.unit
//
//  def setQuota[F[_]](t: AsyncAdmin, settings: QuotaSettings)(implicit
//                                                             M: Applicative[F],
//                                                             F: CompletableFuture ~> F): F[Unit] =
//    F(t.setQuota(settings)) *> M.unit
//
//  def getQuota[F[_]](t: AsyncAdmin, filter: QuotaFilter)(
//      implicit
//      M: Apply[F],
//      F: CompletableFuture ~> F): F[Seq[QuotaSettings]] =
//    M.map(F(t.getQuota(filter)))(_.asScala)
//
//  def addReplicationPeer[F[_]](t: AsyncAdmin,
//                               peerId: String,
//                               config: ReplicationPeerConfig,
//                               enabled: Boolean = true)(implicit
//                                                        M: Applicative[F],
//                                                        F: CompletableFuture ~> F): F[Unit] =
//    F(t.addReplicationPeer(peerId, config, enabled)) *> M.unit
//
//  def updateReplicationPeer[F[_]](t: AsyncAdmin, peerId: String, config: ReplicationPeerConfig)(
//      implicit
//      M: Applicative[F],
//      F: CompletableFuture ~> F): F[Unit] =
//    F(t.updateReplicationPeerConfig(peerId, config)) *> M.unit
//
//  def removeReplicationPeer[F[_]](t: AsyncAdmin, peerId: String)(
//      implicit
//      M: Applicative[F],
//      F: CompletableFuture ~> F): F[Unit] =
//    F(t.removeReplicationPeer(peerId)) *> M.unit
//
//  def enableReplicationPeer[F[_]](t: AsyncAdmin, peerId: String)(
//      implicit
//      M: Applicative[F],
//      F: CompletableFuture ~> F): F[Unit] =
//    F(t.enableReplicationPeer(peerId)) *> M.unit
//
//  def disableReplicationPeer[F[_]](t: AsyncAdmin, peerId: String)(
//      implicit
//      M: Applicative[F],
//      F: CompletableFuture ~> F): F[Unit] =
//    F(t.disableReplicationPeer(peerId)) *> M.unit
//
//  def getReplicationPeer[F[_]](t: AsyncAdmin, peerId: String)(
//      implicit
//      F: CompletableFuture ~> F): F[ReplicationPeerConfig] =
//    F(t.getReplicationPeerConfig(peerId))
//
//  def appendReplicationPeerTableCFs[F[_]](t: AsyncAdmin,
//                                          peerId: String,
//                                          tableCfs: Map[TableName, List[String]])(
//      implicit
//      M: Applicative[F],
//      F: CompletableFuture ~> F): F[Unit] =
//    F(t.appendReplicationPeerTableCFs(peerId, tableCfs.mapValues(_.asJava).asJava)) *> M.unit
//
//  def removeReplicationPeerTableCFs[F[_]](t: AsyncAdmin,
//                                          peerId: String,
//                                          tableCfs: Map[TableName, List[String]])(
//      implicit
//      M: Applicative[F],
//      F: CompletableFuture ~> F): F[Unit] =
//    F(t.removeReplicationPeerTableCFs(peerId, tableCfs.mapValues(_.asJava).asJava)) *> M.unit
//
//  def listReplicationPeers[F[_]](t: AsyncAdmin)(
//      implicit
//      M: Apply[F],
//      F: CompletableFuture ~> F): F[Seq[ReplicationPeerDescription]] =
//    M.map(F(t.listReplicationPeers))(_.asScala)
//
//  def listReplicationPeersByPattern[F[_]](t: AsyncAdmin, pattern: Pattern)(
//      implicit
//      M: Apply[F],
//      F: CompletableFuture ~> F): F[Seq[ReplicationPeerDescription]] =
//    M.map(F(t.listReplicationPeers(pattern)))(_.asScala)
//
//  def listReplicatedTableCFs[F[_]](t: AsyncAdmin)(implicit
//                                                  M: Apply[F],
//                                                  F: CompletableFuture ~> F): F[Seq[TableCFs]] =
//    M.map(F(t.listReplicatedTableCFs))(_.asScala)
//
//  def enableTableReplication[F[_]](t: AsyncAdmin, tableName: TableName)(
//      implicit
//      M: Applicative[F],
//      F: CompletableFuture ~> F): F[Unit] =
//    F(t.enableTableReplication(tableName)) *> M.unit
//
//  def disableTableReplication[F[_]](t: AsyncAdmin, tableName: TableName)(
//      implicit
//      M: Applicative[F],
//      F: CompletableFuture ~> F): F[Unit] =
//    F(t.disableTableReplication(tableName)) *> M.unit
//
//  def snapshot2[F[_]](t: AsyncAdmin,
//                      snapshotName: String,
//                      tableName: TableName,
//                      snapshotType: SnapshotType = SnapshotType.FLUSH)(
//      implicit
//      M: Applicative[F],
//      F: CompletableFuture ~> F): F[Unit] =
//    F(t.snapshot(snapshotName, tableName, snapshotType)) *> M.unit
//
//  def snapshot[F[_]](t: AsyncAdmin, description: SnapshotDescription)(
//      implicit
//      M: Applicative[F],
//      F: CompletableFuture ~> F): F[Unit] =
//    F(t.snapshot(description)) *> M.unit
//
//  def isSnapshotFinished[F[_]](t: AsyncAdmin, description: SnapshotDescription)(
//      implicit
//      M: Apply[F],
//      F: CompletableFuture ~> F): F[Boolean] =
//    M.map(F(t.isSnapshotFinished(description)))(_.booleanValue)
//
//  def restoreSnapshot[F[_]](t: AsyncAdmin, snapshotName: String)(
//      implicit
//      M: Applicative[F],
//      F: CompletableFuture ~> F): F[Unit] =
//    F(t.restoreSnapshot(snapshotName)) *> M.unit
//
//  def restoreSnapshotWithFailSafe[F[_]](t: AsyncAdmin,
//                                        snapshotName: String,
//                                        takeFailSafeSnapshot: Boolean)(
//      implicit
//      M: Applicative[F],
//      F: CompletableFuture ~> F): F[Unit] =
//    F(t.restoreSnapshot(snapshotName, takeFailSafeSnapshot)) *> M.unit
//
//  def cloneSnapshot[F[_]](t: AsyncAdmin, snapshotName: String, tableName: TableName)(
//      implicit
//      M: Applicative[F],
//      F: CompletableFuture ~> F): F[Unit] =
//    F(t.cloneSnapshot(snapshotName, tableName)) *> M.unit
//
//  def listSnapshots[F[_]](t: AsyncAdmin)(implicit
//                                         M: Apply[F],
//                                         F: CompletableFuture ~> F): F[Seq[SnapshotDescription]] =
//    M.map(F(t.listSnapshots))(_.asScala)
//
//  def listSnapshotsByPattern[F[_]](t: AsyncAdmin, pattern: Pattern)(
//      implicit
//      M: Apply[F],
//      F: CompletableFuture ~> F): F[Seq[SnapshotDescription]] =
//    M.map(F(t.listSnapshots(pattern)))(_.asScala)
//
//  def listTableSnapshots[F[_]](t: AsyncAdmin, pattern: Pattern)(
//      implicit
//      M: Apply[F],
//      F: CompletableFuture ~> F): F[Seq[SnapshotDescription]] =
//    M.map(F(t.listTableSnapshots(pattern)))(_.asScala)
//
//  def listTableSnapshotsByPattern[F[_]](t: AsyncAdmin, pattern: Pattern, snapshotPattern: Pattern)(
//      implicit
//      M: Apply[F],
//      F: CompletableFuture ~> F): F[Seq[SnapshotDescription]] =
//    M.map(F(t.listTableSnapshots(pattern, snapshotPattern)))(_.asScala)
//
//  def deleteSnapshots[F[_]](t: AsyncAdmin)(implicit
//                                           M: Applicative[F],
//                                           F: CompletableFuture ~> F): F[Unit] =
//    F(t.deleteSnapshots()) *> M.unit
//
//  def deleteSnapshotsByPattern[F[_]](t: AsyncAdmin, pattern: Pattern)(
//      implicit
//      M: Applicative[F],
//      F: CompletableFuture ~> F): F[Unit] =
//    F(t.deleteSnapshots(pattern)) *> M.unit
//
//  def deleteSnapshot[F[_]](t: AsyncAdmin, snapshotName: String)(
//      implicit
//      M: Applicative[F],
//      F: CompletableFuture ~> F): F[Unit] =
//    F(t.deleteSnapshot(snapshotName)) *> M.unit
//
//  def deleteTableSnapshots[F[_]](t: AsyncAdmin, pattern: Pattern)(
//      implicit
//      M: Applicative[F],
//      F: CompletableFuture ~> F): F[Unit] =
//    F(t.deleteTableSnapshots(pattern)) *> M.unit
//
//  def deleteTableSnapshotsWithSnapshotPattern[F[_]](t: AsyncAdmin,
//                                                    pattern: Pattern,
//                                                    snapshotPattern: Pattern)(
//      implicit
//      M: Applicative[F],
//      F: CompletableFuture ~> F): F[Unit] =
//    F(t.deleteTableSnapshots(pattern, snapshotPattern)) *> M.unit
//
//  def execProcedure[F[_]](t: AsyncAdmin,
//                          signature: String,
//                          instance: String,
//                          props: Map[String, String])(implicit
//                                                      M: Applicative[F],
//                                                      F: CompletableFuture ~> F): F[Unit] =
//    F(t.execProcedure(signature, instance, props.asJava)) *> M.unit
//
//  def execProcedureWithReturn[F[_]](t: AsyncAdmin,
//                                    signature: String,
//                                    instance: String,
//                                    props: Map[String, String])(
//      implicit
//      F: CompletableFuture ~> F): F[Array[Byte]] =
//    F(t.execProcedureWithReturn(signature, instance, props.asJava))
//
//  def isProcedureFinished[F[_]](t: AsyncAdmin,
//                                signature: String,
//                                instance: String,
//                                props: Map[String, String])(implicit
//                                                            M: Apply[F],
//                                                            F: CompletableFuture ~> F): F[Boolean] =
//    M.map(F(t.isProcedureFinished(signature, instance, props.asJava)))(_.booleanValue)
//
//  def abortProcedure[F[_]](t: AsyncAdmin, procId: Long, mayInterruptIfRunning: Boolean)(
//      implicit
//      M: Apply[F],
//      F: CompletableFuture ~> F): F[Boolean] =
//    M.map(F(t.abortProcedure(procId, mayInterruptIfRunning)))(_.booleanValue)
//
//  def getProcedures[F[_]](t: AsyncAdmin)(implicit
//                                         F: CompletableFuture ~> F): F[String] =
//    F(t.getProcedures)
//
//  def getLocks[F[_]](t: AsyncAdmin)(implicit
//                                    F: CompletableFuture ~> F): F[String] =
//    F(t.getLocks)
//
//  def decommissionRegionServers[F[_]](t: AsyncAdmin, servers: Seq[ServerName], offload: Boolean)(
//      implicit
//      M: Applicative[F],
//      F: CompletableFuture ~> F): F[Unit] =
//    F(t.decommissionRegionServers(servers.asJava, offload)) *> M.unit
//
//  def listDecommissionedRegionServers[F[_]](t: AsyncAdmin)(
//      implicit
//      M: Apply[F],
//      F: CompletableFuture ~> F): F[Seq[ServerName]] =
//    M.map(F(t.listDecommissionedRegionServers))(_.asScala)
//
//  def recommissionRegionServer[F[_]](t: AsyncAdmin,
//                                     server: ServerName,
//                                     encodedRegionNames: Seq[Array[Byte]])(
//      implicit
//      M: Applicative[F],
//      F: CompletableFuture ~> F): F[Unit] =
//    F(t.recommissionRegionServer(server, encodedRegionNames.asJava)) *> M.unit
//
//  def getClusterMetrics[F[_]](t: AsyncAdmin)(implicit
//                                             F: CompletableFuture ~> F): F[ClusterMetrics] =
//    F(t.getClusterMetrics)
//
//  def getClusterMetricsWithOptions[F[_]](t: AsyncAdmin, options: ju.EnumSet[CMOption])(
//      implicit
//      F: CompletableFuture ~> F): F[ClusterMetrics] =
//    F(t.getClusterMetrics(options))
//
//  def getMaster[F[_]](t: AsyncAdmin)(implicit
//                                     F: CompletableFuture ~> F): F[ServerName] =
//    F(t.getMaster)
//
//  def getBackupMasters[F[_]](t: AsyncAdmin)(implicit
//                                            M: Apply[F],
//                                            F: CompletableFuture ~> F): F[Iterable[ServerName]] =
//    M.map(F(t.getBackupMasters))(_.asScala)
//
//  def getRegionServers[F[_]](t: AsyncAdmin)(implicit
//                                            M: Apply[F],
//                                            F: CompletableFuture ~> F): F[Iterable[ServerName]] =
//    M.map(F(t.getRegionServers))(_.asScala)
//
//  def getMasterCoprocessorNames[F[_]](t: AsyncAdmin)(implicit
//                                                     M: Apply[F],
//                                                     F: CompletableFuture ~> F): F[Seq[String]] =
//    M.map(F(t.getMasterCoprocessorNames))(_.asScala)
//
//  def shutdown[F[_]](t: AsyncAdmin)(implicit
//                                    M: Applicative[F],
//                                    F: CompletableFuture ~> F): F[Unit] =
//    F(t.shutdown) *> M.unit
//
//  def stopMaster[F[_]](t: AsyncAdmin)(implicit
//                                      M: Applicative[F],
//                                      F: CompletableFuture ~> F): F[Unit] =
//    F(t.stopMaster) *> M.unit
//
//  def stopRegionServer[F[_]](t: AsyncAdmin, serverName: ServerName)(
//      implicit
//      M: Applicative[F],
//      F: CompletableFuture ~> F): F[Unit] =
//    F(t.stopRegionServer(serverName)) *> M.unit
//
//  def updateConfiguration[F[_]](t: AsyncAdmin)(implicit
//                                               M: Applicative[F],
//                                               F: CompletableFuture ~> F): F[Unit] =
//    F(t.updateConfiguration()) *> M.unit
//
//  def updateConfigurationWithName[F[_]](t: AsyncAdmin, serverName: ServerName)(
//      implicit
//      M: Applicative[F],
//      F: CompletableFuture ~> F): F[Unit] =
//    F(t.updateConfiguration(serverName)) *> M.unit
//
//  def clearCompactionQueues[F[_]](t: AsyncAdmin, serverName: ServerName, queues: Set[String])(
//      implicit
//      M: Applicative[F],
//      F: CompletableFuture ~> F): F[Unit] =
//    F(t.clearCompactionQueues(serverName, queues.asJava)) *> M.unit
//
//  def getRegionMetrics[F[_]](t: AsyncAdmin, serverName: ServerName)(
//      implicit
//      M: Apply[F],
//      F: CompletableFuture ~> F): F[Seq[RegionMetrics]] =
//    M.map(F(t.getRegionMetrics(serverName)))(_.asScala)
//
//  def getRegionMetricsWithTable[F[_]](t: AsyncAdmin, serverName: ServerName, tableName: TableName)(
//      implicit
//      M: Apply[F],
//      F: CompletableFuture ~> F): F[Seq[RegionMetrics]] =
//    M.map(F(t.getRegionMetrics(serverName, tableName)))(_.asScala)
//
//  def isMasterInMaintenanceMode[F[_]](t: AsyncAdmin)(implicit
//                                                     M: Apply[F],
//                                                     F: CompletableFuture ~> F): F[Boolean] =
//    M.map(F(t.isMasterInMaintenanceMode))(_.booleanValue())
//
//  def getCompactionState[F[_]](t: AsyncAdmin, tableName: TableName)(
//      implicit
//      F: CompletableFuture ~> F): F[CompactionState] =
//    F(t.getCompactionState(tableName))
//
//  def getCompactionStateWithType[F[_]](t: AsyncAdmin,
//                                       tableName: TableName,
//                                       compactType: CompactType)(
//      implicit
//      F: CompletableFuture ~> F): F[CompactionState] =
//    F(t.getCompactionState(tableName, compactType))
//
//  def getCompactionStateForRegion[F[_]](t: AsyncAdmin, regionName: Array[Byte])(
//      implicit
//      F: CompletableFuture ~> F): F[CompactionState] =
//    F(t.getCompactionStateForRegion(regionName))
//
//  def getLastMajorCompactionTimestamp[F[_]](t: AsyncAdmin, tableName: TableName)(
//      implicit
//      M: Apply[F],
//      F: CompletableFuture ~> F): F[Option[Long]] =
//    M.map(F(t.getLastMajorCompactionTimestamp(tableName)))(r =>
//      if (r.isPresent) Option(r.get.longValue) else None)
//
//  def getLastMajorCompactionTimestampForRegion[F[_]](t: AsyncAdmin, regionName: Array[Byte])(
//      implicit
//      M: Apply[F],
//      F: CompletableFuture ~> F): F[Option[Long]] =
//    M.map(F(t.getLastMajorCompactionTimestampForRegion(regionName)))(r =>
//      if (r.isPresent) Option(r.get.longValue) else None)
//
//  def getSecurityCapabilities[F[_]](t: AsyncAdmin)(
//      implicit
//      M: Apply[F],
//      F: CompletableFuture ~> F): F[Seq[SecurityCapability]] =
//    M.map(F(t.getSecurityCapabilities))(_.asScala)
//
//  def balancerSwitch[F[_]](t: AsyncAdmin, on: Boolean)(implicit
//                                                       M: Apply[F],
//                                                       F: CompletableFuture ~> F): F[Boolean] =
//    M.map(F(t.balancerSwitch(on)))(_.booleanValue)
//
//  def balance[F[_]](t: AsyncAdmin, forcible: Boolean = false)(
//      implicit
//      M: Apply[F],
//      F: CompletableFuture ~> F): F[Boolean] =
//    M.map(F(t.balance(forcible)))(_.booleanValue)
//
//  def isBalancerEnabled[F[_]](t: AsyncAdmin)(implicit
//                                             M: Apply[F],
//                                             F: CompletableFuture ~> F): F[Boolean] =
//    M.map(F(t.isBalancerEnabled))(_.booleanValue)
//
//  def normalizerSwitch[F[_]](t: AsyncAdmin, on: Boolean)(implicit
//                                                         M: Apply[F],
//                                                         F: CompletableFuture ~> F): F[Boolean] =
//    M.map(F(t.normalizerSwitch(on)))(_.booleanValue)
//
//  def isNormalizerEnabled[F[_]](t: AsyncAdmin)(implicit
//                                               M: Apply[F],
//                                               F: CompletableFuture ~> F): F[Boolean] =
//    M.map(F(t.isNormalizerEnabled))(_.booleanValue)
//
//  def normalize[F[_]](t: AsyncAdmin)(implicit
//                                     M: Apply[F],
//                                     F: CompletableFuture ~> F): F[Boolean] =
//    M.map(F(t.normalize))(_.booleanValue)
//
//  def cleanerChoreSwitch[F[_]](t: AsyncAdmin, on: Boolean)(implicit
//                                                           M: Apply[F],
//                                                           F: CompletableFuture ~> F): F[Boolean] =
//    M.map(F(t.cleanerChoreSwitch(on)))(_.booleanValue)
//
//  def isCleanerChoreEnabled[F[_]](t: AsyncAdmin)(implicit
//                                                 M: Apply[F],
//                                                 F: CompletableFuture ~> F): F[Boolean] =
//    M.map(F(t.isCleanerChoreEnabled))(_.booleanValue)
//
//  def runCleanerChore[F[_]](t: AsyncAdmin)(implicit
//                                           M: Apply[F],
//                                           F: CompletableFuture ~> F): F[Boolean] =
//    M.map(F(t.runCleanerChore))(_.booleanValue)
//
//  def catalogJanitorSwitch[F[_]](t: AsyncAdmin, on: Boolean)(
//      implicit
//      M: Apply[F],
//      F: CompletableFuture ~> F): F[Boolean] =
//    M.map(F(t.catalogJanitorSwitch(on)))(_.booleanValue)
//
//  def isCatalogJanitorEnabled[F[_]](t: AsyncAdmin)(implicit
//                                                   M: Apply[F],
//                                                   F: CompletableFuture ~> F): F[Boolean] =
//    M.map(F(t.isCatalogJanitorEnabled))(_.booleanValue)
//
//  def runCatalogJanitor[F[_]](t: AsyncAdmin)(implicit
//                                             M: Apply[F],
//                                             F: CompletableFuture ~> F): F[Int] =
//    M.map(F(t.runCatalogJanitor))(_.intValue)
//
//  def coprocessorService[F[_], S, R](t: AsyncAdmin,
//                                     stubMaker: RpcChannel => S,
//                                     callable: ServiceCaller[S, R])(
//      implicit
//      F: CompletableFuture ~> F): F[R] =
//    F(t.coprocessorService[S, R](stubMaker.asJava, callable))
//
//  def coprocessorServiceFor[F[_], S, R](t: AsyncAdmin,
//                                        stubMaker: RpcChannel => S,
//                                        callable: ServiceCaller[S, R],
//                                        serverName: ServerName)(implicit
//                                                                F: CompletableFuture ~> F): F[R] =
//    F(t.coprocessorService[S, R](stubMaker.asJava, callable, serverName))
//
//  def listDeadServers[F[_]](t: AsyncAdmin)(
//      implicit
//      M: Apply[F],
//      F: CompletableFuture ~> F
//  ): F[Seq[ServerName]] =
//    M.map(F(t.listDeadServers))(_.asScala)
//
//  def clearDeadServers[F[_]](t: AsyncAdmin, servers: Seq[ServerName])(
//      implicit
//      M: Apply[F],
//      F: CompletableFuture ~> F
//  ): F[Seq[ServerName]] =
//    M.map(F(t.clearDeadServers(servers.asJava)))(_.asScala)
//
//  def clearBlockCache[F[_]](t: AsyncAdmin, tableName: TableName)(
//      implicit
//      F: CompletableFuture ~> F
//  ): F[CacheEvictionStats] =
//    F(t.clearBlockCache(tableName))
}
