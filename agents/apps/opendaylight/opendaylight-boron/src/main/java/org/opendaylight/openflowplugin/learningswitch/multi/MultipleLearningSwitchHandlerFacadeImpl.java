/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.learningswitch.multi;

import org.opendaylight.openflowplugin.learningswitch.DataChangeListenerRegistrationHolder;
import org.opendaylight.openflowplugin.learningswitch.InstanceIdentifierUtils;
import org.opendaylight.openflowplugin.learningswitch.FlowCommitWrapper;
import org.opendaylight.openflowplugin.learningswitch.LearningSwitchHandler;
import org.opendaylight.openflowplugin.learningswitch.LearningSwitchHandlerSimpleImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketProcessingService;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public class MultipleLearningSwitchHandlerFacadeImpl implements LearningSwitchHandler {
    
    private static final Logger LOG = LoggerFactory
            .getLogger(MultipleLearningSwitchHandlerFacadeImpl.class);
    
    private FlowCommitWrapper dataStoreAccessor;
    private PacketProcessingService packetProcessingService;
    private PacketInDispatcherImpl packetInDispatcher;

    @Override
    public synchronized void onSwitchAppeared(InstanceIdentifier<Table> appearedTablePath) {
        LOG.debug("expected table acquired, learning ..");
       
        /**
         * appearedTablePath is in form of /nodes/node/node-id/table/table-id
         * so we shorten it to /nodes/node/node-id to get identifier of switch.
         * 
         */
        InstanceIdentifier<Node> nodePath = InstanceIdentifierUtils.getNodePath(appearedTablePath);
        
        /**
         * We check if we already initialized dispatcher for that node,
         * if not we create new handler for switch.
         * 
         */
        if (!packetInDispatcher.getHandlerMapping().containsKey(nodePath)) {
            // delegate this node (owning appearedTable) to SimpleLearningSwitchHandler  
            LearningSwitchHandlerSimpleImpl simpleLearningSwitch = new LearningSwitchHandlerSimpleImpl();
            /**
             * We set runtime dependencies
             */
            simpleLearningSwitch.setDataStoreAccessor(dataStoreAccessor);
            simpleLearningSwitch.setPacketProcessingService(packetProcessingService);
            
            /**
             * We propagate table event to newly instantiated instance of learning switch
             */
            simpleLearningSwitch.onSwitchAppeared(appearedTablePath);
            /**
             * We update mapping of already instantiated LearningSwitchHanlders
             */
            packetInDispatcher.getHandlerMapping().put(nodePath, simpleLearningSwitch);
        }
    }

    @Override
    public void setRegistrationPublisher(
            DataChangeListenerRegistrationHolder registrationPublisher) {
        //NOOP
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
}
