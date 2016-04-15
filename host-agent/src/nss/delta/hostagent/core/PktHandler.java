package nss.delta.hostagent.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import nss.delta.hostagent.core.AMInterface;

public class PktHandler {
	private AMInterface cm;
	private Process process;

	private BufferedWriter stdIn;
	private BufferedReader stdOut;

	private String amIP;
	private int amPort;

	public PktHandler() {
		
	}

	public String executePing(String destIP) {
		try {
			System.out.println("ping " + destIP + " -c 3");
			process = Runtime.getRuntime().exec("ping " + destIP + " -c 3");

			stdOut = new BufferedReader(new InputStreamReader(process.getInputStream()));

			// stdIn = new BufferedWriter(new OutputStreamWriter(
			// process.getOutputStream()));

			process.waitFor();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String lineRead = "";
		String result = "";

		try {
			while ((lineRead = stdOut.readLine()) != null) {
				// swallow the line, or print it out -
				result += lineRead + "\n";
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(result);
		return result;
	}

	public String comparePing(String destIP) {
		String result = "";

		try {
			System.out.println("compare");
			process = Runtime.getRuntime().exec("ping " + destIP + " -c 2");
			stdOut = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String lines = "";

			try {
				while ((lines = stdOut.readLine()) != null) {
					// swallow the line, or print it out -
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			process.getErrorStream().close();
			process.getInputStream().close();
			process.getOutputStream().close();
			process.waitFor();

			process = Runtime.getRuntime().exec("ping " + destIP + " -c 10");
			stdOut = new BufferedReader(new InputStreamReader(process.getInputStream()));

			String lineRead = "";

			try {
				while ((lineRead = stdOut.readLine()) != null) {
					// swallow the line, or print it out -
					result += lineRead + "\n";
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println(result);

			// stdIn = new BufferedWriter(new OutputStreamWriter(
			// process.getOutputStream()));
			process.getErrorStream().close();
			process.getInputStream().close();
			process.getOutputStream().close();
			process.waitFor();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return result;
	}

	public void killProc() {
		process.destroy();
	}

}
