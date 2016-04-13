package nss.delta.agentmanager.targetcon;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;

public class ONOS {
	private Process process = null;
	private boolean isRunning = false;

	public static String version = "";
	public String controllerPath = "";
	public String appPath = "";

	private int currentPID;

	private BufferedWriter stdIn;
	private BufferedReader stdOut;

	public ONOS(String controllerPath, String appPath) {
		this.controllerPath = controllerPath;
		this.appPath = appPath;
	}

	public ArrayList<String> getBaseInputs() {
		ArrayList<String> attacks = new ArrayList<String>();

		attacks.add("HostService");
		attacks.add("DeviceService");
		attacks.add("ApplicationService");
		attacks.add("ComponentConfigService");
		attacks.add("LinkService");
		attacks.add("ClusterService");
		
		return attacks;
	}

	public ArrayList<String> getBoundaryInputs(String code) {
		ArrayList<String> inputs = new ArrayList<String>();

		if (code.contains("A-3-M")) {
			inputs.add("switch");
			inputs.add("host");
			inputs.add("flow");
			inputs.add("controller info");
		}

		return inputs;
	}

	public int createController() {
		isRunning = false;

		String str = "";
		try {
			process = Runtime
					.getRuntime()
					.exec("/home/seungsoo/Application/apache-karaf-3.0.4/bin/karaf clean");
			
			Field pidField = Class.forName("java.lang.UNIXProcess")
					.getDeclaredField("pid");
			pidField.setAccessible(true);
			Object value = pidField.get(process);

			this.currentPID = (int) value;

			try {
				Thread.sleep(7000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			stdOut = new BufferedReader(new InputStreamReader(
					process.getInputStream()));
			stdIn = new BufferedWriter(new OutputStreamWriter(
					process.getOutputStream()));
			
			while ((str = stdOut.readLine()) != null) {
				if (str.contains("ONOS.")) {
					isRunning = true;
					break;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (SecurityException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IllegalArgumentException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IllegalAccessException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		return currentPID;
	}

	public Process getProc() {
		return this.process;
	}

	public void killController() {
		try {
			if(stdIn != null) {
				stdIn.write("system:shutdown -f\n");
				stdIn.flush();
				
				stdIn.close();
				stdOut.close();
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}

	public boolean installAppAgent() {
		/* ONOS, AppAgent is automatically installed when the controller starts */
		return true;
	}
}
