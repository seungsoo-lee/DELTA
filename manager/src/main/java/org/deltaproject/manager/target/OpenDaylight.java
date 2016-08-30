package org.deltaproject.manager.target;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.lang.reflect.Field;

public class OpenDaylight implements TargetController {

	public static final String HYDROGEN = "";
	
	private Process process = null;
	private boolean isRunning = false;
	
	public String version = "";
	public String controllerPath = "";
	public String appPath = "";
	
	private int currentPID = -1;
	private int bundleID;
	
	private BufferedWriter stdIn;
	private BufferedReader stdOut;
	
	
	public OpenDaylight(String controllerPath, String v) {
		this.controllerPath = controllerPath;
		this.version = v;
	}
	
	public OpenDaylight setAppAgentPath(String path) {
		this.appPath = path;
		
		return this;
	}
	
	public int createController() {
		isRunning = false;

		String str;

		try {
			if (version.equals("helium-sr4")) {
				process = Runtime.getRuntime().exec("ssh vagrant@10.100.100.11 /home/vagrant/distribution-karaf-0.2.4-Helium-SR4/bin/karaf");
			} else if (version.equals("berylium")) {
				process = Runtime.getRuntime().exec("ssh vagrant@10.100.100.11 /home/vagrant/onos-1.6.0/bin/onos-service start");
			}

			Field pidField = Class.forName("java.lang.UNIXProcess").getDeclaredField("pid");
			pidField.setAccessible(true);
			Object value = pidField.get(process);

			this.currentPID = (Integer) value;

			stdOut = new BufferedReader(new InputStreamReader(process.getInputStream()));
			stdIn = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));

			while ((str = stdOut.readLine()) != null) {
				// System.out.println(str);
				if (str.contains("read timeout is 0")) {
					isRunning = true;
					break;
				}
			}

			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			Process temp = Runtime.getRuntime().exec("ssh vagrant@10.100.100.11 sudo ps -ef | grep karaf");
			String tempS;

			BufferedReader stdOut2 = new BufferedReader(new InputStreamReader(temp.getInputStream()));

			while ((tempS = stdOut2.readLine()) != null && !tempS.isEmpty()) {
				if (tempS.contains("Helium-SR4")) {
					String[] list = StringUtils.split(tempS);

					currentPID = Integer.parseInt(list[1]);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return currentPID;
	}
	
	public boolean installAppAgent() {
		boolean isInstalled = false;

		String str = "";

		try {
			stdIn.write("install file:" + appPath + "\n");
			stdIn.flush();

			while (!isInstalled) {
				str = stdOut.readLine();
				if (str.contains("Installed")) {
					isInstalled = true;

					int idx = str.indexOf("Installed");
					this.bundleID = Integer.parseInt(str.substring(idx - 4,
							idx - 1));

					stdIn.write("start " + bundleID + "\n");
					stdIn.flush();

//					 System.out.println("AppAgent bundle ID [" + bundleID
//					 + "] Installed");
				}
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return true;
	}
	
	public void killController() {
		try {
			if (stdIn != null) {
				stdIn.write("system:shutdown -f\n");
				stdIn.flush();
				stdIn.close();
			}

			if (stdOut != null) {
				stdOut.close();
			}

			if (this.currentPID != -1) {
				Process pc = null;
				try {
					pc = Runtime.getRuntime().exec("ssh vagrant@10.100.100.11 sudo kill -9 " + this.currentPID);
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
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Process getProc() {
		return this.process;
	}

	@Override
	public String getType() {
		// TODO Auto-generated method stub
		return "OpenDaylight";
	}
	
	@Override
	public String getVersion() {
		// TODO Auto-generated method stub
		return this.version;
	}

	@Override
	public String getPath() {
		// TODO Auto-generated method stub
		return this.controllerPath + "\n" + this.appPath;
	}

	@Override
	public int getPID() {
		// TODO Auto-generated method stub
		return this.currentPID;
	}

	@Override
	public BufferedReader getStdOut() {
		// TODO Auto-generated method stub
		return this.stdOut;
	}
	
}