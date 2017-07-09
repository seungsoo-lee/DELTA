/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.deltaproject.odlagent.core;

import java.util.Map;
import java.util.Map.Entry;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class WakeupOnNode implements DataChangeListener {

    private static final Logger LOG = LoggerFactory
            .getLogger(WakeupOnNode.class);

    private AppAgentHandler appAgentHandler;

    @Override
    public void onDataChanged(AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> change) {
        Short requiredTableId = 0;

        // TODO add flow
        Map<InstanceIdentifier<?>, DataObject> updated = change.getUpdatedData();

        for (Entry<InstanceIdentifier<?>, DataObject> updateItem : updated.entrySet()) {
            DataObject object = updateItem.getValue();
            if (object instanceof Table) {
                Table tableSure = (Table) object;
                LOG.debug("[DELTA] table: {}", object);

                if (requiredTableId.equals(tableSure.getId())) {
                    @SuppressWarnings("unchecked")
                    InstanceIdentifier<Table> tablePath = (InstanceIdentifier<Table>) updateItem.getKey();
                    appAgentHandler.onSwitchAppeared(tablePath);
                }
            }
        }
    }

    /**
     * @param learningSwitchHandler the learningSwitchHandler to set
     */
    public void setLearningSwitchHandler(
            AppAgentHandler appAgentHandler) {
        this.appAgentHandler = appAgentHandler;
    }
}