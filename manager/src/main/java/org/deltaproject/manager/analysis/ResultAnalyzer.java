package org.deltaproject.manager.analysis;

import org.deltaproject.manager.core.ControllerManager;
import org.deltaproject.manager.testcase.TestAdvancedCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class ResultAnalyzer {
	private static final Logger log = LoggerFactory.getLogger(TestAdvancedCase.class);
	public static final int LATENCY_TIME = 0;
	public static final int COMMUNICATON = 1;
	public static final int CONTROLLER_STATE = 2;
	public static final int SWITCH_STATE = 3;
	public static final int APPAGENT_REPLY = 4;

	private ControllerManager controllerm;
	private String fuzzingResult = "";

	public ResultAnalyzer(ControllerManager in) {
		controllerm = in;
	}

	public boolean checkAll(String code, String in) {
		return true;
	}

	public String getFuzzingLog() {
		return this.fuzzingResult;
	}

	public boolean checkUnknown(ArrayList<ResultInfo> results) {
		boolean isSuccess = false;

		if (this.checkControllerState()) {
			isSuccess = true;
			fuzzingResult += "Controller Shutdown\n";
		}

		if (this.checkSwitchState()) {
			isSuccess = true;
			fuzzingResult += "Switch Shutdown\n";
		}

		if (this.checkSwitchState()) {
			isSuccess = true;
			fuzzingResult += "Switch Shutdown\n";
		}

		if (isSuccess) {
			log.info("Success");

		}

		return true;
	}	

	public boolean checkResult(String code, ResultInfo result) {
		ArrayList<Integer> types = result.types();
		boolean isSuccess = false;

		for (int i = 0; i < types.size(); i++) {
			switch (types.get(i)) {
			case ResultInfo.CONTROLLER_STATE:
				isSuccess = checkControllerState();
				break;

			case ResultInfo.SWITCH_STATE:
				isSuccess = checkSwitchState();
				break;

			case ResultInfo.LATENCY_TIME:
				isSuccess = checkLatency(result.getBeforeL(), result.getAfterL());
				break;

			case ResultInfo.COMMUNICATON:
				isSuccess = checkCommunication(result.getResult());
				break;

			case ResultInfo.APPAGENT_REPLY:
				isSuccess = checkAppAgentResponse(result.getResult());
				break;

			case ResultInfo.CHANNELAGENT_REPLY:
				isSuccess = checkTopoInfo(result.getResult());
				break;
			}

			if (isSuccess) {
				log.info(code + ", FAIL");
				break;
			}
		}

		if (!isSuccess) {
			log.info(code + ", PASS");
			return true;
		} else
			return false;
	}

	public boolean checkCommunication(String in) {
		if (in.contains("Unreachable")) {
			log.info("Ping response host unreachable");
		} else if (in.contains("100% packet loss")) {
			log.info("100% Packet loss");
		} else {
			return false;
		}

		return true;
	}

	public boolean checkLatency(String before, String after) {
		// System.out.println(before+"\n"+after);

		int idx = before.indexOf("min");
		before = before.substring(idx);

		StringTokenizer str = new StringTokenizer(before, "/");
		for (int i = 0; i < 4; i++)
			str.nextToken();
		before = str.nextToken();

		idx = after.indexOf("min");
		after = after.substring(idx);

		str = new StringTokenizer(after, "/");
		for (int i = 0; i < 4; i++)
			str.nextToken();
		after = str.nextToken();

		int beforeInt = (int) (Double.parseDouble(before) * 1000);
		int afterInt = (int) (Double.parseDouble(after) * 1000);

		if (beforeInt < afterInt) {
			log.info("Latency before :" + beforeInt + " < After :" + afterInt);
		} else {
			log.info("Latency before :" + beforeInt + " < After :" + afterInt);
			return false;
		}

		return true;
	}

	public boolean checkAppAgentResponse(String in) {
		// System.out.println("from: " + in);
		if (in.contains("success")) {
			in = in.substring(in.indexOf("|")+1);
			log.info(in);
		} else {
			return false;
		}

		return true;
	}

	public boolean checkControllerState() {
		if (!controllerm.isRunning()) {
			log.info("Controller shutdown");
		} else {
			log.info("Controller NOT shutdown");
			return false;
		}

		return true;
	}

	public boolean checkTopoInfo(String result) {
		if (result.contains("Success")) {
			log.info(result);
		} else {
			log.info(result);
			return false;
		}

		return true;
	}

	public boolean checkSwitchState() {
		Process temp = null;
		String tempS = "";
		int switchCnt = controllerm.getSwitchCounter();

		try {
			int cnt = 0;
			temp = Runtime.getRuntime().exec(new String[] { "bash", "-c", "netstat -ap | grep 6633" });

			BufferedReader stdOut = new BufferedReader(new InputStreamReader(temp.getInputStream()));

			while ((tempS = stdOut.readLine()) != null) {
				if (tempS.contains("ESTABLISHED")) {
					cnt++;
				}
			}

			if (switchCnt != cnt)
				log.info("Switch diconnected");
			else {
				log.info("Switches NOT disconnected");
				return false;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return true;
	}
}