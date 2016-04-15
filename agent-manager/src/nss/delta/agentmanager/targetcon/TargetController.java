package nss.delta.agentmanager.targetcon;

public interface TargetController {
	public int createController();

	public Process getProc();

	public void killController();

	public boolean installAppAgent();
	
	public String getType();
	
	public String getVersion();
	
	public String getPath();
	
	public int getPID();
}
