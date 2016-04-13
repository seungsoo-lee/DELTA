package nss.delta.channelagent.testcase;

import java.io.IOException;

public class LinkFabricator {
	private ProcessBuilder pb;
	private Process p;
	private final String filePath = "./modules/LinkFabricator.py";
	
	public LinkFabricator () {
		pb = new ProcessBuilder("python",this.filePath);			
	}
	
	public void startLinkFabrication () {
		try {
			p = pb.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void stopLinkFabrication () {
		p.destroy();
	}	
	
}
