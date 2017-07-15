/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 * <p>
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.deltaproject.odlagent.core;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.sal.binding.api.NotificationService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketProcessingService;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * Listens to packetIn notification and
 * <ul>
 * <li>in HUB mode simply floods all switch ports (except ingress port)</li>
 * <li>in LSWITCH mode collects source MAC address of packetIn and bind it with ingress port.
 * If handler MAC address is already bound then a flow is created (for direct communication between
 * corresponding MACs)</li>
 * </ul>
 */
public class AppAgentImpl implements DataChangeListenerRegistrationHolder,
        AppAgent {

    private static final Logger LOG = LoggerFactory.getLogger(AppAgentImpl.class);

    private NotificationService notificationService;
    private PacketProcessingService packetProcessingService;
    private DataBroker data;
    private Registration packetInRegistration;
    private SalFlowService salFlowService;
    private ListenerRegistration<DataChangeListener> dataChangeListenerRegistration;

    /**
     * @param notificationService the notificationService to set
     */
    @Override
    public void setNotificationService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * @param packetProcessingService the packetProcessingService to set
     */
    @Override
    public void setPacketProcessingService(
            PacketProcessingService packetProcessingService) {
        this.packetProcessingService = packetProcessingService;
    }

    /**
     * @param data the data to set
     */
    @Override
    public void setDataBroker(DataBroker data) {
        this.data = data;
    }

    public void setSalFlowService(SalFlowService sal) {
        this.salFlowService = sal;
    }

    /**
     * start
     */
    @Override
    public void start() {
        LOG.info("[DELTA] OpenDaylight app agent start()");
        FlowCommitWrapper dataStoreAccessor = new FlowCommitWrapperImpl(data);

        PacketInDispatcherImpl packetInDispatcher = new PacketInDispatcherImpl();

        AppAgentFacadeImpl appAgentHandler = new AppAgentFacadeImpl();
        appAgentHandler.setRegistrationPublisher(this);
        appAgentHandler.setDataStoreAccessor(dataStoreAccessor);
        appAgentHandler.setPacketProcessingService(packetProcessingService);
        appAgentHandler.setPacketInDispatcher(packetInDispatcher);
        appAgentHandler.setDataBroker(data);
        appAgentHandler.setSalFlowService(this.salFlowService);

        packetInRegistration = notificationService.registerNotificationListener(packetInDispatcher);

        WakeupOnNode wakeupListener = new WakeupOnNode();
        wakeupListener.setLearningSwitchHandler(appAgentHandler);

        dataChangeListenerRegistration = data.registerDataChangeListener(LogicalDatastoreType.OPERATIONAL,
                InstanceIdentifier.builder(Nodes.class)
                        .child(Node.class)
                        .augmentation(FlowCapableNode.class)
                        .child(Table.class).build(),
                wakeupListener,
                DataBroker.DataChangeScope.SUBTREE);

        appAgentHandler.connectManager();
    }

    /**
     * stop
     */
    @Override
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


    @Override
    public ListenerRegistration<DataChangeListener> getDataChangeListenerRegistration() {
        return dataChangeListenerRegistration;
    }
}
