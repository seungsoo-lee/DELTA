package org.deltaproject.onosagent.fuzzer;

import com.google.common.collect.Lists;
import org.onosproject.cfg.ComponentConfigService;
import org.onosproject.cfg.ConfigProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

class ComponentConfig {
	private String component;
	private HashMap<String, String> cfgMap;
	private ArrayList<String> pname;

	public ComponentConfig(String in) {
		cfgMap = new HashMap<String, String>();
		pname = new ArrayList<String>();
		component = in;
	}

	public void put(String name, String type) {
		cfgMap.put(name, type);
	}

	public void add(String in) {
		pname.add(in);
	}

	public String getComponent() {
		return this.component;
	}

	public String getType(String pname) {
		return cfgMap.get(pname);
	}
	
	public ArrayList<String> getPname() {
		return this.pname;
	}
}

public class Fuzzing {
	private ComponentConfigService cfgService;
	private ArrayList<ComponentConfig> cfgList;

	public Fuzzing() {

	}

	public void setCfgService(ComponentConfigService in) {
		cfgService = in;
		cfgList = new ArrayList<ComponentConfig>();

		ArrayList<String> cpName = Lists.newArrayList(cfgService
				.getComponentNames());

		for (String s : cpName) {
			ArrayList<ConfigProperty> plist = Lists.newArrayList(cfgService
					.getProperties(s));

			ComponentConfig temp = new ComponentConfig(s);

			for (ConfigProperty p : plist) {
				temp.add(p.name());
				temp.put(p.name(), p.type().toString());
			}

			cfgList.add(temp);
		}
	}

	public void startCfgServiceFuzzing() {
		Random ran = new Random();
		
		ComponentConfig com = cfgList.get(ran.nextInt(cfgList.size()));
		this.cfgService.setProperty(
				"org.onosproject.provider.host.impl.HostLocationProvider",
				"hostRemovalEnabled", "false");
	}
}