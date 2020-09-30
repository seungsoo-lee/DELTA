package org.deltaproject.appagent;
import java.io.*;
import java.util.Random;

public class SystemTime extends Thread{

	protected Runtime rt;
	protected String date_information[];
	protected int day;
	protected final String month[]={"Jan","Feb","Mar", "Apr", "May", "Jun","Jul","Aug", "Dep"," Oct", "Nov", "Dec"};;
	protected int year;
	protected Random rand;

	public SystemTime() {
		rt = Runtime.getRuntime();
		date_information = new String[3];
		rand = new Random();
	}

	@Override
	public void run() {
		while(true)
		{
			day=rand.nextInt(29)+1;
			year=rand.nextInt(100)+1970;
			date_information[0]=String.valueOf(day);
			date_information[1]=month[rand.nextInt(11)];
			date_information[2]=String.valueOf(year);
			try {
				//Process proc = rt.exec(new String[] { "date", "-s", "1 Jan 1960"} );
				Process proc = rt.exec(new String[] { "date", "-s", date_information[0]+" "+date_information[1]+" "+date_information[2] } );
				BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
				System.out.println("System time setting info : "+br.readLine());
				Thread.sleep(1);
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
