/*
 * Copyright (c) 2014, 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.deltaproject.odlagent.core;

import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;


public class InstanceIdentifierUtils {

    private InstanceIdentifierUtils() {
        //hiding constructor for util class
    }

    /**
     * Creates an Instance Identifier (path) for node with specified id
     *
     * @param nodeId
     * @return
     */
    public static final InstanceIdentifier<Node> createNodePath(final NodeId nodeId) {
        return InstanceIdentifier.builder(Nodes.class)
                .child(Node.class, new NodeKey(nodeId))
                .build();
    }

    /**
     * Shorten's node child path to node path.
     *
     * @param nodeChild child of node, from which we want node path.
     * @return
     */
    public static final InstanceIdentifier<Node> getNodePath(final InstanceIdentifier<?> nodeChild) {
        return nodeChild.firstIdentifierOf(Node.class);
    }


    /**
     * Creates a table path by appending table specific location to node path
     *
     * @param nodePath
     * @param tableKey
     * @return
     */
    public static final InstanceIdentifier<Table> createTablePath(final InstanceIdentifier<Node> nodePath,final TableKey tableKey) {
        return nodePath.augmentation(FlowCapableNode.class).child(Table.class, tableKey);
    }

    /**
     * Creates a path for particular flow, by appending flow-specific information
     * to table path.
     *
     * @param tablePath
     * @param flowKey
     * @return path to flow
     */
    public static InstanceIdentifier<Flow> createFlowPath(final InstanceIdentifier<Table> tablePath, final FlowKey flowKey) {
        return tablePath.child(Flow.class, flowKey);
    }

    /**
     * Extract table id from table path.
     *
     * @param tablePath
     * @return
     */
    public static Short getTableId(final InstanceIdentifier<Table> tablePath) {
        return tablePath.firstKeyOf(Table.class, TableKey.class).getId();
    }

    /**
     * Extracts NodeConnectorKey from node connector path.
     *
     */
    public static NodeConnectorKey getNodeConnectorKey(final InstanceIdentifier<?> nodeConnectorPath) {
        return nodeConnectorPath.firstKeyOf(NodeConnector.class, NodeConnectorKey.class);
    }


    //
    public static final InstanceIdentifier<NodeConnector> createNodeConnectorPath(final InstanceIdentifier<Node> nodeKey,final NodeConnectorKey nodeConnectorKey) {
        return nodeKey.child(NodeConnector.class,nodeConnectorKey);
    }
}
