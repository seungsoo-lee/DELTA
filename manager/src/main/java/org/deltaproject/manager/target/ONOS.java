package org.deltaproject.manager.target;

import java.io.*;
import java.lang.reflect.Field;

public class ONOS implements TargetController {
	private Process process = null;
	private boolean isRunning = false;

	public String version = "";
	public String controllerPath = "";
	public String karafPath = "";

	private int currentPID = -1;

	private BufferedWriter stdIn;
	private BufferedReader stdOut;

	public ONOS(String controllerPath, String v) {
		this.controllerPath = controllerPath;
		this.version = v;
	}

	public ONOS setKarafPath(String p) {
		this.karafPath = p;
		
		return this;
	}
	
	public int createController() {
		isRunning = false;

		String str = "";
		try {
			process = Runtime.getRuntime().exec(karafPath+" clean");

			Field pidField = Class.forName("java.lang.UNIXProcess").getDeclaredField("pid");
			pidField.setAccessible(true);
			Object value = pidField.get(process);

			this.currentPID = (Integer) value;

			try {
				Thread.sleep(7000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			stdOut = new BufferedReader(new InputStreamReader(process.getInputStream()));
			stdIn = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));

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
			if (stdIn != null) {
				stdIn.write("system:shutdown -f\n");
				stdIn.flush();
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
		this.currentPID = -1;
	}

	/* ONOS, AppAgent is automatically installed when the controller starts */
	public boolean installAppAgent() {

		return true;
	}

	@Override
	public String getType() {
		// TODO Auto-generated method stub
		return "ONOS";
	}
	
	@Override
	public String getVersion() {
		// TODO Auto-generated method stub
		return this.version;
	}
	
	@Override
	public String getPath() {
		// TODO Auto-generated method stub
		return this.controllerPath;
	}

	@Override
	public int getPID() {
		// TODO Auto-generated method stub
		return this.currentPID;
	}
}
