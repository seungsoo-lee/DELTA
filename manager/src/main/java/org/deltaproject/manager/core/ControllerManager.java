package org.deltaproject.manager.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.deltaproject.manager.target.Floodlight;
import org.deltaproject.manager.target.ONOS;
import org.deltaproject.manager.target.OpenDaylight;
import org.deltaproject.manager.target.TargetController;

public class ControllerManager {
	private String cbechPath = "";
	private String targetController = "";
	private String ofPort = "";

	private int cbenchPID = -1;

	private ArrayList<TargetController> targetList;
	private ArrayList<String> switchList;

	private Process processCbench;

	private Configuration cfg;

	public ControllerManager(Configuration config) {
		targetList = new ArrayList<TargetController>();
		switchList = new ArrayList<String>();

		cfg = config;

		this.setConfig();
	}

	public void setConfig() {
		TargetController fl = new Floodlight(cfg.getFloodlightRoot(), cfg.getFloodlightVer());
		targetList.add(fl);

		TargetController odl = new OpenDaylight(cfg.getODLRoot(), cfg.getODLVer())
				.setAppAgentPath(cfg.getODLAppAgent());

		targetList.add(odl);

		TargetController onos = new ONOS(cfg.getONOSRoot(), cfg.getONOSVer()).setKarafPath(cfg.getONOSKarafRoot());
		targetList.add(onos);

		cbechPath = cfg.getCbenchRoot();
		targetController = cfg.getTargetController();
		ofPort = cfg.getOFPort();

		String temp = cfg.getSwitchIP();
		StringTokenizer st = new StringTokenizer(temp, ",");
		while (st.hasMoreTokens()) {
			this.addSwitchIP(st.nextToken());
		}
	}

	public boolean createController() {
		for (TargetController tc : targetList) {
			if (tc.getType().equals(this.targetController)) {
				tc.createController();

				return true;
			}
		}
		return false;
	}

	public boolean killController() {
		for (TargetController tc : targetList) {
			if (tc.getType().equals(this.targetController)) {
				tc.killController();
				return true;
			}
		}
		return false;
	}

	public String getType() {
		return targetController;
	}

	public String showConfig() {
		String result = "";

		result += "Target Controller: ";

		for (TargetController tc : targetList) {
			if (tc.getType().equals(this.targetController)) {
				result += tc.getType() + " - " + tc.getVersion();
			}
		}

		result += "\nTarget Path: ";

		for (TargetController tc : targetList) {
			if (tc.getType().equals(this.targetController)) {
				result += tc.getPath();
			}
		}

		result += "\nSwitchs: ";

		for (String s : this.switchList) {
			result += s + ", ";
		}

		result += "\b\b  ";

		result += "\nCbench Path: " + this.cbechPath;

		return result;
	}

	public void addSwitchIP(String ip) {
		switchList.add(ip);
	}

	public boolean isRunning() {
		int controllerPID = -1;

		for (TargetController tc : targetList) {
			if (tc.getType().equals(this.targetController)) {
				controllerPID = tc.getPID();
			}
		}

		Process temp = null;
		try {
			temp = Runtime.getRuntime().exec(new String[] { "bash", "-c", "ps -a | grep " + controllerPID });

			BufferedReader stdOut = new BufferedReader(new InputStreamReader(temp.getInputStream()));

			if (stdOut.readLine() == null) {
				controllerPID = -1;
				temp.destroy();
				return false;
			}

			stdOut.close();
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

	public boolean executeCbench() {
		try {
			processCbench = Runtime.getRuntime().exec(cbechPath + "cbench -c " + cfg.getControllerIP() + "  -p "
					+ ofPort + " -m 10000 -l 10 -s 16 -M 1000 -t");

			Field pidField = Class.forName("java.lang.UNIXProcess").getDeclaredField("pid");
			pidField.setAccessible(true);
			Object value = pidField.get(processCbench);

			this.cbenchPID = (Integer) value;

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return true;
	}

	public void flushARPcache() {
		try {
			Process pc = Runtime.getRuntime().exec("ip -s -s neigh flush all");
			pc.getErrorStream().close();
			pc.getInputStream().close();
			pc.getOutputStream().close();
			pc.waitFor();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public boolean isConnectedSwitch() {
		Process temp = null;
		String tempS = "";
		int switchCnt = this.switchList.size();

		while (true) {
			try {
				int cnt = 0;
				temp = Runtime.getRuntime().exec(new String[] { "bash", "-c", "netstat -ap | grep " + ofPort });

				BufferedReader stdOut = new BufferedReader(new InputStreamReader(temp.getInputStream()));

				while ((tempS = stdOut.readLine()) != null) {
					if (tempS.contains("ESTABLISHED")) {
						cnt++;
					}
				}
				stdOut.close();

				if (switchCnt == cnt) {
					return true;
				}

				Thread.sleep(1000);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public int getSwitchCounter() {
		return this.switchList.size();
	}

	public void killCbench() {
		Process pc = null;
		try {
			if (processCbench != null) {
				processCbench.getErrorStream().close();
				processCbench.getInputStream().close();
				processCbench.getOutputStream().close();
				processCbench.destroy();
			}

			pc = Runtime.getRuntime().exec("kill -9 " + this.cbenchPID);
			pc.getErrorStream().close();
			pc.getInputStream().close();
			pc.getOutputStream().close();
			pc.waitFor();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
