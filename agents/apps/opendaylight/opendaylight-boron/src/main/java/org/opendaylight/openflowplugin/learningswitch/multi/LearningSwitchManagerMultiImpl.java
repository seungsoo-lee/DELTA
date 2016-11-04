/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.learningswitch.multi;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.sal.binding.api.NotificationService;
import org.opendaylight.openflowplugin.learningswitch.DataChangeListenerRegistrationHolder;
import org.opendaylight.openflowplugin.learningswitch.FlowCommitWrapper;
import org.opendaylight.openflowplugin.learningswitch.FlowCommitWrapperImpl;
import org.opendaylight.openflowplugin.learningswitch.LearningSwitchManager;
import org.opendaylight.openflowplugin.learningswitch.WakeupOnNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketProcessingService;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Listens to packetIn notification and
 * <ul>
 * <li>in HUB mode simply floods all switch ports (except ingress port)</li>
 * <li>in LSWITCH mode collects source MAC address of packetIn and bind it with ingress port.
 * If target MAC address is already bound then a flow is created (for direct communication between
 * corresponding MACs)</li>
 * </ul>
 */
public class LearningSwitchManagerMultiImpl implements DataChangeListenerRegistrationHolder,
        LearningSwitchManager {

    private static final Logger LOG = LoggerFactory
            .getLogger(LearningSwitchManagerMultiImpl.class);

    private NotificationService notificationService;
    private PacketProcessingService packetProcessingService;
    private DataBroker data;

    private Registration packetInRegistration;

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

    /**
     * starting learning switch
     */
    @Override
    public void start() {
        LOG.debug("start() -->");
        FlowCommitWrapper dataStoreAccessor = new FlowCommitWrapperImpl(data);

        PacketInDispatcherImpl packetInDispatcher = new PacketInDispatcherImpl();
        MultipleLearningSwitchHandlerFacadeImpl learningSwitchHandler = new MultipleLearningSwitchHandlerFacadeImpl();
        learningSwitchHandler.setRegistrationPublisher(this);
        learningSwitchHandler.setDataStoreAccessor(dataStoreAccessor);
        learningSwitchHandler.setPacketProcessingService(packetProcessingService);
        learningSwitchHandler.setPacketInDispatcher(packetInDispatcher);
        packetInRegistration = notificationService.registerNotificationListener(packetInDispatcher);

        WakeupOnNode wakeupListener = new WakeupOnNode();
        wakeupListener.setLearningSwitchHandler(learningSwitchHandler);
        dataChangeListenerRegistration = data.registerDataChangeListener(LogicalDatastoreType.OPERATIONAL,
                InstanceIdentifier.builder(Nodes.class)
                        .child(Node.class)
                        .augmentation(FlowCapableNode.class)
                        .child(Table.class).build(),
                wakeupListener,
                DataBroker.DataChangeScope.SUBTREE);
        LOG.debug("start() <--");
    }

    /**
     * stopping learning switch
     */
    @Override
    public void stop() {
        LOG.debug("stop() -->");
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
        LOG.debug("stop() <--");
    }


    @Override
    public ListenerRegistration<DataChangeListener> getDataChangeListenerRegistration() {
        return dataChangeListenerRegistration;
    }
}
