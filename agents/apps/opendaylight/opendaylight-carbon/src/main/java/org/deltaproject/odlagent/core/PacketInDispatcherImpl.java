/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.deltaproject.odlagent.core;

import java.util.HashMap;
import java.util.Map;

import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketProcessingListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketReceived;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * 
 */
public class PacketInDispatcherImpl implements PacketProcessingListener {
    
    private Map<InstanceIdentifier<Node>, PacketProcessingListener> handlerMapping;
    
    /**
     * default constructor
     */
    public PacketInDispatcherImpl() {
        handlerMapping = new HashMap<>();
    }

    @Override
    public void onPacketReceived(PacketReceived notification) {
        // find corresponding handler
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

        /**
         * We lookup up the the packet-in listener for this node.
         * 
         */
        PacketProcessingListener nodeHandler = handlerMapping.get(nodeOfPacket);
        
        /**
         * 
         * If we have packet-processing listener, we delegate notification.
         * 
         */
        if (nodeHandler != null) {
            nodeHandler.onPacketReceived(notification);
        }
    }
    
    /**
     * @return the handlerMapping
     */
    public Map<InstanceIdentifier<Node>, PacketProcessingListener> getHandlerMapping() {
        return handlerMapping;
    }
}
