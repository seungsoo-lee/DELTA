/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 * <p>
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.deltaproject.odlagent.core;

import org.apache.felix.dm.Component;
import org.apache.felix.dm.Dependency;
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

import java.util.Dictionary;
import java.util.List;

/**
 * Activator is derived from AbstractBindingAwareConsumer, which takes care
 * of looking up MD-SAL in Service Registry and registering consumer
 * when MD-SAL is present.
 */
public class Activator extends DependencyActivatorBase implements AutoCloseable, BindingAwareConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(Activator.class);
    private AppAgent appAgent;
    private AMinterface cm;
    private BundleContext bundleContext;
    private DependencyManager dependencyManager;

    /**
     * Invoked first
     */
    @Override
    public void init(BundleContext bundleContext, DependencyManager dependencyManager) throws Exception {
        LOG.info("[DELTA] init() passing");

        ServiceReference<BindingAwareBroker> brokerRef = bundleContext.getServiceReference(BindingAwareBroker.class);
        BindingAwareBroker broker = bundleContext.getService(brokerRef);
        broker.registerConsumer(this);

        this.bundleContext = bundleContext;
        this.dependencyManager = dependencyManager;

//        cm = new AMinterface();
//        cm.setActivator(this);
//        cm.setServerAddr();
//        cm.connectServer("ActAgent");
//        cm.start();
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

        testEventListenerUnsubscription("l2switch");
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

    /* 3.1.100: Application Eviction */
    public String testApplicationEviction(String target) {
        boolean removed = false;

        Bundle[] list = bundleContext.getBundles();

        for (Bundle b : list) {
            if (b.getSymbolicName().contains("l2switch")) {
                LOG.info("[DELTA] uninstall! - " + b.getSymbolicName());
                try {
                    b.uninstall();
                    removed = true;
                } catch (BundleException e) {
                    e.printStackTrace();
                }
            }
        }

        if (removed)
            return "l2switch";
        else
            return "null";
    }

    /* 3.1.090: Event Listener Unsubscription */
    public String testEventListenerUnsubscription(String input) {
        String removed = "";

        DependencyManager manager = this.dependencyManager;
        List<DependencyManager> dlist = manager.getDependencyManagers();

        BundleContext ctx = this.bundleContext;
        Bundle[] blist = ctx.getBundles();

        for (int i = 0; i < dlist.size(); i++) {
            DependencyManager dm = dlist.get(i);
            List<Component> temp = dm.getComponents();

            for (int j = 0; j < temp.size(); j++) {
                Component ct = temp.get(j);

                @SuppressWarnings("unchecked")
                Dictionary<String, Object> props = ct.getServiceProperties();
                if (props != null) {
                    Object res = props.get("salListenerName"); // salListenerName
                    if (res != null) {
                        if (!((String) res).contains(input)) {
                            continue;
                        }

                        ServiceRegistration sr = ct.getServiceRegistration();
                        Bundle bd = sr.getReference().getBundle();
                        LOG.info("[TEST] " + bd.getSymbolicName());

						/* service unregister */
                        sr.unregister();
                        removed = sr.toString();

                        String[] lists = sr.getReference().getPropertyKeys();
                        for (String s : lists) {
                            LOG.info(s + " ");
                        }

//                        List<Dependency> dpl = ct.getDependencies();
//                        for (int k = 0; k < dpl.size(); k++) {
//                            Dependency dp = dpl.get(k);
//                            ct.remove(dp);
//                        }

                        return removed;
                    }
                }
            }
        }
        return removed;
    }
}