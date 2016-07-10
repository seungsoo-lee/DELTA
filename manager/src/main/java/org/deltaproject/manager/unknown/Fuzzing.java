package org.deltaproject.manager.unknown;

public class Fuzzing {
	public static final int APPLICATION = 0x010;
	public static final int CHANNEL_U = 0x001;
	public static final int CHANNEL_D = 0x100;

	public static final int ASM = 4;
	public static final int SYM = 5;
	public static final int INT = 6;

	private OpenFlow openflow;

	private String targetFlow;
	private int targetType;
	private int targetAgent;

	private boolean isAAFuzzing;
	private boolean isCAFuzzing;

	private int fuzzOrdering;

	public Fuzzing() {
		openflow = new OpenFlow();
		isCAFuzzing = true;
	}

	public void writeLog(String in) {
		
	}

	public int getTargetType(String target) {
		this.targetFlow = target;

		if (target.equalsIgnoreCase("asymmetric")) {
			targetType = openflow.getRandomAsy();
		} else if (target.equalsIgnoreCase("symmetric")) {
			targetType = openflow.getRandomSym();
		} else {
			targetType = -1; /* for intra controller */
			setCAfuzzing(false);
		}

		return this.targetType;
	}

	public void setAAfuzzing(boolean input) {
		this.isAAFuzzing = input;
	}

	public void setCAfuzzing(boolean input) {
		this.isCAFuzzing = input;
	}

	public int getFuzzingOrdering() {
		int order = 0;

		if (targetType == -1) {
			return APPLICATION;
		} else {
			switch (targetType) {
			case OpenFlow.HELLO:
				order = CHANNEL_U | CHANNEL_D;
				break;

			case OpenFlow.PacketIn:
				order = CHANNEL_U | APPLICATION;
				break;

			case OpenFlow.EchoRes:
				order = CHANNEL_U;
				break;
			}
		}
		return order;
	}
}