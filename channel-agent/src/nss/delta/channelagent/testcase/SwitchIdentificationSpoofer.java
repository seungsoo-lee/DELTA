package nss.delta.channelagent.testcase;

import java.io.IOException;

public class SwitchIdentificationSpoofer extends Thread {
	private boolean flag = true;
	private final String filePath = "./modules/SwitchIdentificationSpoofer.py";

	public void setFlag(boolean value) {
		flag = value;
	}

	public void run() {
		try {
			while (flag) {
				ProcessBuilder pb = new ProcessBuilder("python", this.filePath);

				Process p = pb.start();
				p.destroy();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
