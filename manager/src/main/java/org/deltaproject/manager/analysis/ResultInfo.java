package org.deltaproject.manager.analysis;

import java.util.ArrayList;

public class ResultInfo {
    public static final int LATENCY_TIME        = 0;
    public static final int COMMUNICATON        = 1;
    public static final int CONTROLLER_STATE    = 2;
    public static final int SWITCH_STATE        = 3;
    public static final int APPAGENT_REPLY      = 4;
    public static final int CHANNELAGENT_REPLY  = 5;
    public static final int HOSTAGENT_REPLY     = 6;

    private ArrayList<Integer> checkType;

    private String before;
    private String after;

    private String result;

    public ResultInfo() {
        checkType = new ArrayList<Integer>();
    }

    public ResultInfo addType(int in) {
        checkType.add(in);

        return this;
    }

    public void setLatency(String beforeIn, String afterIn) {
        this.before = beforeIn;
        this.after = afterIn;
    }

    public void setResult(String resultIn) {
        result = resultIn;
    }

    public ArrayList<Integer> types() {
        return this.checkType;
    }

    public String getBeforeL() {
        return before;
    }

    public String getAfterL() {
        return after;
    }

    public String getResult() {
        return result;
    }
}