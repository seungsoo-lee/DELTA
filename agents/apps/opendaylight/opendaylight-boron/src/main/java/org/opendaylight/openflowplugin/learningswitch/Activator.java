/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.learningswitch;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.AbstractBindingAwareConsumer;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ConsumerContext;
import org.opendaylight.controller.sal.binding.api.NotificationService;
import org.opendaylight.openflowplugin.learningswitch.multi.LearningSwitchManagerMultiImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketProcessingService;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * learning switch activator
 * <p>
 * Activator is derived from AbstractBindingAwareConsumer, which takes care
 * of looking up MD-SAL in Service Registry and registering consumer
 * when MD-SAL is present.
 */
public class Activator extends AbstractBindingAwareConsumer implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(Activator.class);

    private LearningSwitchManager learningSwitch;


    @Override
    protected void startImpl(BundleContext context) {
        LOG.info("startImpl() passing");
    }

    /**
     * Invoked when consumer is registered to the MD-SAL.
     */
    @Override
    public void onSessionInitialized(ConsumerContext session) {
        LOG.info("inSessionInitialized() passing");
        /**
         * We create instance of our LearningSwitchManager
         * and set all required dependencies,
         *
         * which are 
         *   Data Broker (data storage service) - for configuring flows and reading stored switch state
         *   PacketProcessingService - for sending out packets
         *   NotificationService - for receiving notifications such as packet in.
         *
         */
        learningSwitch = new LearningSwitchManagerMultiImpl();
        learningSwitch.setDataBroker(session.getSALService(DataBroker.class));
        learningSwitch.setPacketProcessingService(session.getRpcService(PacketProcessingService.class));
        learningSwitch.setNotificationService(session.getSALService(NotificationService.class));
        learningSwitch.start();
    }

    @Override
    public void close() {
        LOG.info("close() passing");
        if (learningSwitch != null) {
            learningSwitch.stop();
        }
    }

    @Override
    protected void stopImpl(BundleContext context) {
        close();
        super.stopImpl(context);
    }
}
