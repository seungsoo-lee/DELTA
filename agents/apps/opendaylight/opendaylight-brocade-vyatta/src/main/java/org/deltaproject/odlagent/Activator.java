package org.deltaproject.odlagent;

import org.apache.felix.dm.DependencyActivatorBase;
import org.apache.felix.dm.DependencyManager;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Activator extends DependencyActivatorBase{
	private Object imp;
	private String containerName;

	protected static final Logger logger = LoggerFactory
			.getLogger(Activator.class);

	public void init() {
		 connectManager();
	}

	public void connectManager() {

	}

	public void destroy() {
		System.out.println("Activator.destroy()");
	}

	public Object[] getImplementations() {
		Object[] res = { AppAgent.class };
		return res;
	}


	public String Application_Eviction(String target) {
		String removed = "";
//		DependencyManager manager = this.c.getDependencyManager();
//		BundleContext ctx = manager.getBundleContext();
//		Bundle[] blist = ctx.getBundles();
//
//		for (int i = 0; i < blist.length; i++) {
//			Bundle bd = blist[i];
//			String bdName = bd.getSymbolicName();
//			if (bdName.contains(handler)) {
//				System.out.println(bd.getBundleId() + ":" + bdName);
//				
//				removed = bdName;
//				try {
//					bd.uninstall();
//				} catch (BundleException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//					return removed;
//				}
//				return removed;
//			}
//		}
		return removed;
	}



	public void close() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void destroy(BundleContext arg0, DependencyManager arg1) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void init(BundleContext arg0, DependencyManager arg1) throws Exception {
		// TODO Auto-generated method stub
		
		System.out.println("[Hi I'm App Agent");
		String removed = "";
		DependencyManager manager = arg1;
		BundleContext ctx = manager.getBundleContext();
		Bundle[] blist = ctx.getBundles();

		for (int i = 0; i < blist.length; i++) {
			Bundle bd = blist[i];
			String bdName = bd.getSymbolicName();
			System.out.println(bd.getBundleId() + ":" + bdName);
		}
		
	}
}
