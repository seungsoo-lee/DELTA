package org.deltaproject.manager.target;

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
		
		String str = "";
		try {
			process = new ProcessBuilder(controllerPath).start();
			Field pidField = Class.forName("java.lang.UNIXProcess")
					.getDeclaredField("pid");
			pidField.setAccessible(true);
			Object value = pidField.get(process);

			this.currentPID = (Integer) value;
//			System.err.println("pid = " + currentPID);
			
			stdOut = new BufferedReader(new InputStreamReader(
					process.getInputStream()));
			stdIn = new BufferedWriter(new OutputStreamWriter(
					process.getOutputStream()));

			while (!isRunning) {
				str = stdOut.readLine();				
				if (str.endsWith("initialized successfully")) {
					isRunning = true;
//					System.out.println(str);
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

		installAppAgent();
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
			if(stdIn != null) {
				String str = "";
//				while ((str = stdOut.readLine()) != null) {
//					System.out.println("{"+str+"}");
//				}
				
//				System.out.println("Kill Controller");
				stdIn.write("exit\n");
				stdIn.flush();
				
//				while ((str = stdOut.readLine()) != null) {
//					System.out.println("{"+str+"}");
//				}
				
				stdIn.write("y\n");
				stdIn.flush();
				stdIn.close();
				stdOut.close();
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		
		this.currentPID = -1;
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