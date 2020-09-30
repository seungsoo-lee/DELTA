package org.deltaproject.odlagent;

import org.apache.felix.dm.Component;
import org.apache.felix.dm.DependencyManager;
import org.opendaylight.controller.sal.core.ComponentActivatorAbstractBase;
import org.opendaylight.controller.sal.core.ContainerServiceDependency;
import org.opendaylight.controller.sal.flowprogrammer.IFlowProgrammerService;
import org.opendaylight.controller.sal.packet.IDataPacketService;
import org.opendaylight.controller.sal.packet.IListenDataPacket;
import org.opendaylight.controller.switchmanager.ISwitchManager;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

public class Activator extends ComponentActivatorAbstractBase {

    private Component c;
    private Object imp;
    private String containerName;

    protected static final Logger logger = LoggerFactory
            .getLogger(Activator.class);

    public void init() {

    }

    public void destroy() {
        System.out.println("Activator.destroy()");
    }

    public Object[] getImplementations() {
        Object[] res = {AppAgent2.class};
        return res;
    }

    public void Service_Chain_Interference() {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        List<ContainerServiceDependency> list =  this.c.getDependencies();

        DependencyManager manager = this.c.getDependencyManager();
        List<DependencyManager> dlist = manager.getDependencyManagers();
        BundleContext ctx = manager.getBundleContext();

        Bundle[] blist = ctx.getBundles();

        for (int i = 0; i < dlist.size(); i++) {
            DependencyManager dm = dlist.get(i);
            List<Component> temp = dm.getComponents();

            for (int j = 0; j < temp.size(); j++) {
                Component ct = temp.get(j);
                Dictionary<String, Object> props = ct.getServiceProperties();

                if (props != null) {
                    Object res = props.get("salListenerName"); // salListenerName
                    if (res != null) {
                        System.out.print(res.toString()+"\n");
                        if (!((String) res).contains("appagent") && !((String) res).contains("simpleforwarding")) {
                            Dictionary<String, Object> props_new = new Hashtable<String, Object>();
                            Enumeration<String> keys = props.keys();
                            while (keys.hasMoreElements()) {
                                String key = keys.nextElement();
                                props_new.put(key, props.get(key));
                            }

                            props_new.put("salListenerDependency", "appagent");
                            ct.stop();
                            ct.setServiceProperties(props_new);
                            ct.start();
                        } else {

                        }
                    }
                }
            }
        }
    }

    public void configureInstance(Component c, Object imp, String containerName) {
        if (imp.equals(AppAgent2.class)) {
            // export the service

            Dictionary<String, String> props = new Hashtable<String, String>();
            props.put("salListenerName", "appagent2");

            c.setInterface(new String[]{IListenDataPacket.class.getName()},
                    props);

            c.add(createContainerServiceDependency(containerName)
                    .setService(ISwitchManager.class)
                    .setCallbacks("setSwitchManager", "unsetSwitchManager")
                    .setRequired(true));

            c.add(createContainerServiceDependency(containerName)
                    .setService(IDataPacketService.class)
                    .setCallbacks("setDataPacketService",
                            "unsetDataPacketService").setRequired(true));

            c.add(createContainerServiceDependency(containerName)
                    .setService(IFlowProgrammerService.class)
                    .setCallbacks("setFlowProgrammerService",
                            "unsetFlowProgrammerService").setRequired(true));

            this.c = c;
            this.imp = imp;
            this.containerName = containerName;

            Service_Chain_Interference();
        }
    }
}
