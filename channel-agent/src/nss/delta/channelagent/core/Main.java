package nss.delta.channelagent.core;


public class Main {
	public static void main(String args[]) {
		AMInterface am_if = new AMInterface("192.168.101.195", 3366);
		am_if.connectServer();		
		try {
			Thread.sleep(500);
			am_if.start();
			am_if.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("main exit");
	}
}