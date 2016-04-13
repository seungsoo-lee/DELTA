package nss.delta.agentmanager.targetcon;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.logging.Logger;

public class OpenDaylight {

	public static final String HYDROGEN = "";
	
	private Process process = null;
	private boolean isRunning = false;
	
	public static String version = "";
	public String controllerPath = "";
	public String appPath = "";
	
	private int currentPID;
	private int bundleID;
	
	private BufferedWriter stdIn;
	private BufferedReader stdOut;
	
	
	public OpenDaylight(String controllerPath, String appPath) {
		this.controllerPath = controllerPath;
		this.appPath = appPath;
	}
	
	public ArrayList<String> getBaseAttacks() {
		ArrayList<String> attacks = new ArrayList<String>();
		
		attacks.add("A-3-M");
		attacks.add("A-5-M-1");
		
		return attacks;
	}
	
	public ArrayList<String> getBoundaryInputs(String code) {
		ArrayList<String> inputs = new ArrayList<String>();
		
		if(code.contains("A-3-M")) {
		}
		
		return inputs;
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

			this.currentPID = (int) value;
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
	}
	
	public Process getProc() {
		return this.process;
	}
	
}