/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 * <p>
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.deltaproject.odlagent.core;

import org.apache.felix.dm.DependencyActivatorBase;
import org.apache.felix.dm.DependencyManager;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ConsumerContext;
import org.opendaylight.controller.sal.binding.api.BindingAwareConsumer;
import org.opendaylight.controller.sal.binding.api.NotificationService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketProcessingService;
import org.osgi.framework.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * learning switch activator
 * <p>
 * Activator is derived from AbstractBindingAwareConsumer, which takes care
 * of looking up MD-SAL in Service Registry and registering consumer
 * when MD-SAL is present.
 */
public class Activator extends DependencyActivatorBase implements AutoCloseable, BindingAwareConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(Activator.class);
    private AppAgent appAgent;

    /**
     * Invoked first
     */
    @Override
    public void init(BundleContext bundleContext, DependencyManager dependencyManager) throws Exception {
        LOG.info("[DELTA] init() passing");

        ServiceReference<BindingAwareBroker> brokerRef = bundleContext.getServiceReference(BindingAwareBroker.class);
        BindingAwareBroker broker = bundleContext.getService(brokerRef);
        broker.registerConsumer(this);

        Bundle[] list = bundleContext.getBundles();

        for (Bundle b : list) {
            if (b.getSymbolicName().contains("l2switch")) {
                // LOG.info("[DELTA] uninstall! - " + b.getSymbolicName());
                // b.uninstall();
            }
        }
    }

    /**
     * Invoked when consumer is registered to the MD-SAL.
     */
    @Override
    public void onSessionInitialized(ConsumerContext session) {
        LOG.info("[DELTA] inSessionInitialized() passing");
        /**
         * We create instance of our AppAgent
         * and set all required dependencies,
         *
         * which are
         *   Data Broker (data storage service) - for configuring flows and reading stored switch state
         *   PacketProcessingService - for sending out packets
         *   NotificationService - for receiving notifications such as packet in.
         *
         */
        appAgent = new AppAgentImpl();
        appAgent.setDataBroker(session.getSALService(DataBroker.class));
        appAgent.setPacketProcessingService(session.getRpcService(PacketProcessingService.class));
        appAgent.setNotificationService(session.getSALService(NotificationService.class));
        appAgent.start();
    }

    @Override
    public void close() {
        LOG.info("[DELTA] close() passing");
        if (appAgent != null) {
            appAgent.stop();
        }
    }

    @Override
    public void destroy(BundleContext bundleContext, DependencyManager dependencyManager) throws Exception {
        LOG.info("[DELTA] destroy() passing");
    }
}