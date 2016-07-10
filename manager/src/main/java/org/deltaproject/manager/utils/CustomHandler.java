package org.deltaproject.manager.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CustomHandler {
	private static final DateFormat df = new SimpleDateFormat("hh:mm:ss.SSS");

	private static List strHolder = new ArrayList();
	private String code = "";
	
	public CustomHandler(String code) {
		this.code = code;
	}
	
	public void publish(String record) {
		StringBuilder builder = new StringBuilder(1000);
		builder.append(df.format(new Date(System.currentTimeMillis()))).append(" - ");
		builder.append("[").append(code).append("] - ");
		builder.append(record);
		builder.append("\n");
		
		strHolder.add(builder.toString());
	}

	public void flush() {
		System.out.println("\n");
		if(strHolder.size() != 0) {
			for(int i=0; i<strHolder.size(); i++) {
				System.out.print(strHolder.get(i));
			}
		}
	}

	public void clear() {
		strHolder.clear();
	}
	
	public void close() {
	}
}