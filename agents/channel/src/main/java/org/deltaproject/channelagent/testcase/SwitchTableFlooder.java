package org.deltaproject.channelagent.testcase;

import java.io.IOException;

public class SwitchTableFlooder {
	private ProcessBuilder pb;
	private Process p;
	private final String filePath = "./modules/SwitchTableFlooder.py";
	
	public SwitchTableFlooder () {
		pb = new ProcessBuilder("python",this.filePath);			
	}
	
	public void startSwitchTableFlooding () {
		try {
			p = pb.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void stopSwitchTableFlooding () {
		p.destroy();
	}	
}
