package nss.delta.channelagent.unknown;

public class Fuzzing {
	
	/* for Asymmetric  Control Message */	
	public static final int ERROR = 1;	
	public static final int PacketIn = 10;
	public static final int FlowRemoved = 11;
	public static final int PortStatus = 12;
	public static final int PacketOut = 13;
	public static final int FlowMod = 14;
	public static final int PortMod = 15;
	
	/* for Symmetric Control Message */	
	public static final int Vendor = 4;
	public static final int HELLO = 0;
	public static final int EchoReq = 2;
	public static final int EchoRes = 3;
	public static final int FeatureReq = 5;
	public static final int FeatureRes = 6;
	public static final int GetConfigReq = 7;
	public static final int GetConfigRes = 8;
	public static final int SetConfig = 9;
	public static final int StatsReq = 16;
	public static final int StatsRes = 17;
	public static final int BarrierReq = 18;
	public static final int BarrierRes = 19;
	public static final int QueueGetConfigReq = 20;
	public static final int QueueGetConfigRes = 21;

	
}