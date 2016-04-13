package nss.delta.agentmanager.targetcon;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class ControllerManager {

	public final static int OPENDAYLIGHT = 0;
	public final static int FLOODLIGHT = 1;
	public final static int ONOS = 2;

	private String configPath = "";
	private String cbechPath = "";

	private OpenDaylight odl;
	private ONOS onos;
	private Floodlight floodlight;

	private int controllerType;
	private String controllerVersion;

	private int bundleID = -1;
	private int controllerPID = -1;
	private int cbenchPID = -1;

	private ArrayList<TargetController> targetList;
	private ArrayList<String> switchList;
	private Process process;
	private Process processCbench;

	public ControllerManager(String config) {
		targetList = new ArrayList<TargetController>();
		switchList = new ArrayList<String>();

		readConfigFile(config);
	}

	public int getType() {
		return controllerType;
	}

	public String getInfo() {
		String info = "";

		if (controllerType == OPENDAYLIGHT) {
			info = "OpenDaylight";
		} else if (controllerType == FLOODLIGHT) {
			info = "Floodlight";
		} else if (controllerType == ONOS) {
			info = "ONOS";
		}

		info += "-" + this.controllerVersion;
		return info;
	}

	public ArrayList<String> getBaseInputs() {
		ArrayList<String> lists = null;

		if (controllerType == OPENDAYLIGHT && odl != null) {
			lists = odl.getBaseAttacks();
		} else if (controllerType == FLOODLIGHT && floodlight != null) {

		} else if (controllerType == ONOS && onos != null) {
			lists = onos.getBaseInputs();
		}

		return lists;
	}

	public int getRunningControllerType() {
		return controllerType;
	}

	public boolean checkAvailableFuzzing(int type) {
		return true;
	}

	public void showConfig() {
		System.out.print("Target Controller: ");

		if (controllerType == OPENDAYLIGHT) {
			System.out.print("OpenDaylight");
		} else if (controllerType == FLOODLIGHT) {
			System.out.print("Floodlight");
		} else if (controllerType == ONOS) {
			System.out.print("ONOS");
		}

		System.out.println("-" + this.controllerVersion);

		System.out.print("Controller Path: ");

		if (controllerType == OPENDAYLIGHT) {
			System.out.println(odl.controllerPath);
		} else if (controllerType == FLOODLIGHT) {
			System.out.println(floodlight.controllerPath);
		} else if (controllerType == ONOS) {
			System.out.println(onos.controllerPath);
		}

		System.out.print("AppAgent Path: ");

		if (controllerType == OPENDAYLIGHT) {
			System.out.println(odl.appPath);
		} else if (controllerType == FLOODLIGHT) {
			System.out.println(floodlight.appPath);
		} else if (controllerType == ONOS) {
			System.out.println(onos.appPath);
		}

		System.out.print("Switchs: ");

		for (String s : this.switchList) {
			System.out.print(s + ", ");
		}

		System.out.println("\b\b  ");

		System.out.println("Cbench Path: " + this.cbechPath);
	}

	public void readConfigFile(String config) {
		BufferedReader br = null;
		InputStreamReader isr = null;
		FileInputStream fis = null;
		File file = new File(config);
		String temp = "";

		String controller = "";
		String controllerPath = "";
		String appPath = "";

		try {
			fis = new FileInputStream(file);
			isr = new InputStreamReader(fis, "UTF-8");
			br = new BufferedReader(isr);

			while ((temp = br.readLine()) != null) {
				if (temp.contains("CONTROLLER")) {
					controller = temp.substring(temp.indexOf("=") + 1);

					if (controller.contains("opendaylight")) {
						controllerType = OPENDAYLIGHT;
					} else if (controller.contains("onos")) {
						controllerType = ONOS;
					} else if (controller.contains("floodlight")) {
						controllerType = FLOODLIGHT;
					}

					controllerPath = controller.substring(controller.indexOf("|") + 1);

				} else if (temp.contains("VERSION")) {
					this.controllerVersion = temp.substring(temp.indexOf("=") + 1);
				} else if (temp.contains("APPAGENT_PATH")) {
					appPath = temp.substring(temp.indexOf("=") + 1);
				} else if (temp.contains("CBENCH_PATH")) {
					cbechPath = temp.substring(temp.indexOf("=") + 1);
				} else if (temp.contains("SWITCHS")) {
					temp = temp.substring(temp.indexOf("=") + 1);
					StringTokenizer st = new StringTokenizer(temp, ",");
					while (st.hasMoreTokens()) {
						this.addSwitchIP(st.nextToken());
					}
				}
			}

			switch (controllerType) {
			case OPENDAYLIGHT:
				odl = new OpenDaylight(controllerPath, appPath);
				break;
			case ONOS:
				onos = new ONOS(controllerPath, appPath);
				break;
			case FLOODLIGHT:
				floodlight = new Floodlight(controllerPath, appPath);
				break;
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				fis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				isr.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void addSwitchIP(String ip) {
		switchList.add(ip);
	}

	public boolean isRunning() {
		if (controllerPID == -1)
			return false;
		else {
			Process temp = null;
			try {
				temp = Runtime.getRuntime().exec(new String[] { "bash", "-c", "ps -a | grep " + controllerPID });

				BufferedReader stdOut = new BufferedReader(new InputStreamReader(temp.getInputStream()));

				if (stdOut.readLine() == null) {
					controllerPID = -1;
					process.destroy();
					return true;
				}

				stdOut.close();
				return false;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
		}
	}

	public Process getProcess() {
		return this.process;
	}

	public boolean executeCbench() {
		try {
			processCbench = Runtime.getRuntime()
					.exec(cbechPath + "cbench -c localhost -p 6633 -m 10000 -l 10 -s 16 -M 1000 -t");

			Field pidField = Class.forName("java.lang.UNIXProcess").getDeclaredField("pid");
			pidField.setAccessible(true);
			Object value = pidField.get(processCbench);

			this.cbenchPID = (int) value;

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
				temp = Runtime.getRuntime().exec(new String[] { "bash", "-c", "netstat -ap | grep 6633" });

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

	public void killController() {
		if (controllerPID == -1)
			return;

		switch (this.controllerType) {
		case OPENDAYLIGHT:
			odl.killController();
			break;

		case ONOS:
			onos.killController();
			break;

		case FLOODLIGHT:
			Process pc = null;
			try {
				if (process != null) {
					process.getErrorStream().close();
					process.getInputStream().close();
					process.getOutputStream().close();
				}

				pc = Runtime.getRuntime().exec("kill -9 " + this.controllerPID);
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
			break;

		}

		controllerPID = -1;
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

	public boolean createController() {
		switch (this.controllerType) {
		case OPENDAYLIGHT:
			controllerPID = odl.createController();
			process = odl.getProc();
			odl.installAppAgent();
			break;

		case ONOS:
			controllerPID = onos.createController();
			process = onos.getProc();
			break;

		case FLOODLIGHT:
			controllerPID = floodlight.createController();
			process = floodlight.getProc();
			break;
		}
		return true;
	}
}
