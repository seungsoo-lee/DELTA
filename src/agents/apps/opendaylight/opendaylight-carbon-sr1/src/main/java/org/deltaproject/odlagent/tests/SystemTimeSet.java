package org.deltaproject.odlagent.tests;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Random;

/**
 * Created by seungsoo on 09/07/2017.
 */
public class SystemTimeSet extends Thread {
    protected Runtime rt;
    protected String date_information[];
    protected int day;
    protected final String month[] = {"Jan", "Feb", "Mar", "Apr", "May",
            "Jun", "Jul", "Aug", "Dep", " Oct", "Nov", "Dec"};
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
                Process proc = rt.exec(new String[]{
                        "date",
                        "-s",
                        date_information[0] + " " + date_information[1] + " "
                                + date_information[2]});
                BufferedReader br = new BufferedReader(new InputStreamReader(
                        proc.getInputStream()));
                Thread.sleep(500);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
