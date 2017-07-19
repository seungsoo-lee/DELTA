package org.deltaproject.manager.core;

import java.io.*;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class Configuration {

    private String FLOODLIGHT_ROOT = "";
    private String FLOODLIGHT_VER = "";

    private String ODL_ROOT = "";
    private String ODL_VER = "";
    private String ODL_APPAGENT = "";

    private String ONOS_ROOT = "";
    private String ONOS_VER = "";
    private String ONOS_KARAF_ROOT = "";

    private String CBENCH_ROOT = "";

    private String TARGET_CONTROLLER = "";
    private String TARGET_VERSION = "";

    private String CONTROLLER_SSH = "";
    private String CHANNEL_SSH = "";
    private String HOST_SSH = "";

    private String TARGET_HOST = "";

    private String OF_PORT = "";
    private String OF_VERSION = "";

    private String MITM_NIC = "";
    private String CONTROLLER_IP = "";

    private String original = "";

    private ArrayList<String> switchList;

    private String DUMMY_CONT_IP = "";
    private String DUMMY_CONT_PORT = "";

    private String AM_IP = "";
    private String AM_PORT = "";

    private String WEB = "";

    private String TOPOLOGY_TYPE = "";

    private static final Configuration instance = new Configuration();


    protected Configuration() {

    }

    public static Configuration getInstance() {
        return instance;
    }

    public void initialize(String path) {
        switchList = new ArrayList<String>();
        readConfigFile(path);
    }

    public String getTopologyType() { return TOPOLOGY_TYPE; }

    public ArrayList<String> getSwitchList() {
        return switchList;
    }

    public String getFloodlightRoot() {
        return this.FLOODLIGHT_ROOT;
    }

    public String getTargetVer() {
        return this.TARGET_VERSION;
    }

    public String getODLRoot() {
        return this.ODL_ROOT;
    }

    public String getONOSRoot() {
        return this.ONOS_ROOT;
    }

    public String getCbenchRoot() {
        return this.CBENCH_ROOT;
    }

    public String getTargetController() {
        return this.TARGET_CONTROLLER;
    }

    public String getOFPort() {
        return this.OF_PORT;
    }

    public String getOFVer() {
        return this.OF_VERSION;
    }

    public String getMitmNIC() {
        return this.MITM_NIC;
    }

    public String getTargetHost() {
        return this.TARGET_HOST;
    }

    public String getControllerIP() {
        return this.CONTROLLER_IP;
    }

    public String getSwitchIP(int idx) {
        return this.switchList.get(idx);
    }

    public String getAppSSH() {
        return this.CONTROLLER_SSH;
    }

    public String getChannelSSH() {
        return this.CHANNEL_SSH;
    }

    public String getHostSSH() {
        return this.HOST_SSH;
    }

    public String getDMCIP() {
        return this.DUMMY_CONT_IP;
    }

    public String getDMCPort() {
        return this.DUMMY_CONT_PORT;
    }

    public String getAMIP() {
        return this.AM_IP;
    }

    public String getAMPort() {
        return this.AM_PORT;
    }

    public void setTARGET_CONTROLLER(String TARGET_CONTROLLER) {
        this.TARGET_CONTROLLER = TARGET_CONTROLLER;
    }

    public void setTARGET_VERSION(String TARGET_VERSION) {
        this.TARGET_VERSION = TARGET_VERSION;
    }

    public void setOF_PORT(String OF_PORT) {
        this.OF_PORT = OF_PORT;
    }

    public void setOF_VERSION(String OF_VERSION) {
        this.OF_VERSION = OF_VERSION;
    }

    public void setCONTROLLER_IP(String CONTROLLER_IP) {
        this.CONTROLLER_IP = CONTROLLER_IP;
    }

    public void setSwitchList(ArrayList<String> switchList) {
        this.switchList = switchList;
    }

    public void setTOPOLOGY_TYPE(String TOPOLOGY_TYPE) {
        this.TOPOLOGY_TYPE = TOPOLOGY_TYPE;
    }

    public String show() {
        return original;
    }

    public void readConfigFile(String config) {
        BufferedReader br = null;
        InputStreamReader isr = null;
        FileInputStream fis = null;
        File file = new File(config);
        String temp = "";

        try {
            fis = new FileInputStream(file);
            isr = new InputStreamReader(fis, "UTF-8");
            br = new BufferedReader(isr);

            while ((temp = br.readLine()) != null) {

                original += temp + "\n";

                if (temp.contains("FLOODLIGHT_ROOT"))
                    this.FLOODLIGHT_ROOT = temp.substring(temp.indexOf("=") + 1);

                if (temp.contains("FLOODLIGHT_VER"))
                    this.FLOODLIGHT_VER = temp.substring(temp.indexOf("=") + 1);

                if (temp.contains("ODL_ROOT"))
                    this.ODL_ROOT = temp.substring(temp.indexOf("=") + 1);

                if (temp.contains("ODL_VER"))
                    this.ODL_VER = temp.substring(temp.indexOf("=") + 1);

                if (temp.contains("ODL_APPAGENT"))
                    this.ODL_APPAGENT = temp.substring(temp.indexOf("=") + 1);

                if (temp.contains("ONOS_ROOT"))
                    this.ONOS_ROOT = temp.substring(temp.indexOf("=") + 1);

                if (temp.contains("ONOS_VER"))
                    this.ONOS_VER = temp.substring(temp.indexOf("=") + 1);

                if (temp.contains("ONOS_KARAF_ROOT"))
                    this.ONOS_KARAF_ROOT = temp
                            .substring(temp.indexOf("=") + 1);

                if (temp.contains("CBENCH_ROOT"))
                    this.CBENCH_ROOT = temp.substring(temp.indexOf("=") + 1);

                if (temp.contains("CONTROLLER_IP")) {
                    this.CONTROLLER_IP = temp.substring(temp.indexOf("=") + 1);

                    WEB += temp + "\n";
                }

//                if (temp.contains("TARGET_CONTROLLER")) {
//                    this.TARGET_CONTROLLER = temp
//                            .substring(temp.indexOf("=") + 1);
//
//                    WEB += temp + "\n";
//                }

//                if (temp.contains("TARGET_VERSION")) {
//                    this.TARGET_VERSION = temp.substring(temp.indexOf("=") + 1);
//
//                    WEB += temp + "\n";
//                }

//                if (temp.contains("OF_PORT")) {
//                    this.OF_PORT = temp.substring(temp.indexOf("=") + 1);
//
//                    WEB += temp + "\n";
//                }

//                if (temp.contains("OF_VER")) {
//                    this.OF_VERSION = temp.substring(temp.indexOf("=") + 1);
//
//                    WEB += temp + "\n";
//                }

//                if (temp.contains("SWITCH_IP")) {
//                    String switchlist = temp.substring(temp.indexOf("=") + 1);
//                    StringTokenizer st = new StringTokenizer(switchlist, ",");
//
//                    while (st.hasMoreTokens()) {
//                        this.switchList.add(st.nextToken());
//                    }
//
//                    WEB += temp + "\n";
//                }

                if (temp.contains("MITM_NIC"))
                    this.MITM_NIC = temp.substring(temp.indexOf("=") + 1);

                if (temp.contains("TARGET_HOST"))
                    this.TARGET_HOST = temp.substring(temp.indexOf("=") + 1);

                if (temp.contains("CONTROLLER_SSH"))
                    this.CONTROLLER_SSH = temp.substring(temp.indexOf("=") + 1);

                if (temp.contains("CHANNEL_SSH"))
                    this.CHANNEL_SSH = temp.substring(temp.indexOf("=") + 1);

                if (temp.contains("HOST_SSH"))
                    this.HOST_SSH = temp.substring(temp.indexOf("=") + 1);

                if (temp.contains("DUMMY_CONT_IP"))
                    this.DUMMY_CONT_IP = temp.substring(temp.indexOf("=") + 1);

                if (temp.contains("DUMMY_CONT_PORT"))
                    this.DUMMY_CONT_PORT = temp.substring(temp.indexOf("=") + 1);

                if (temp.contains("AM_IP"))
                    this.AM_IP = temp.substring(temp.indexOf("=") + 1);

                if (temp.contains("AM_PORT"))
                    this.AM_PORT = temp.substring(temp.indexOf("=") + 1);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fis.close();
                isr.close();
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String toString() {
        return "Configuration{" +
                "FLOODLIGHT_ROOT='" + FLOODLIGHT_ROOT + '\'' +
                ", FLOODLIGHT_VER='" + FLOODLIGHT_VER + '\'' +
                ", ODL_ROOT='" + ODL_ROOT + '\'' +
                ", ODL_VER='" + ODL_VER + '\'' +
                ", ODL_APPAGENT='" + ODL_APPAGENT + '\'' +
                ", ONOS_ROOT='" + ONOS_ROOT + '\'' +
                ", ONOS_VER='" + ONOS_VER + '\'' +
                ", ONOS_KARAF_ROOT='" + ONOS_KARAF_ROOT + '\'' +
                ", CBENCH_ROOT='" + CBENCH_ROOT + '\'' +
                ", TARGET_CONTROLLER='" + TARGET_CONTROLLER + '\'' +
                ", TARGET_VERSION='" + TARGET_VERSION + '\'' +
                ", CONTROLLER_SSH='" + CONTROLLER_SSH + '\'' +
                ", CHANNEL_SSH='" + CHANNEL_SSH + '\'' +
                ", HOST_SSH='" + HOST_SSH + '\'' +
                ", TARGET_HOST='" + TARGET_HOST + '\'' +
                ", OF_PORT='" + OF_PORT + '\'' +
                ", OF_VERSION='" + OF_VERSION + '\'' +
                ", MITM_NIC='" + MITM_NIC + '\'' +
                ", CONTROLLER_IP='" + CONTROLLER_IP + '\'' +
                ", original='" + original + '\'' +
                ", switchList=" + switchList +
                ", DUMMY_CONT_IP='" + DUMMY_CONT_IP + '\'' +
                ", DUMMY_CONT_PORT='" + DUMMY_CONT_PORT + '\'' +
                ", AM_IP='" + AM_IP + '\'' +
                ", AM_PORT='" + AM_PORT + '\'' +
                ", WEB='" + WEB + '\'' +
                ", TOPOLOGY_TYPE='" + TOPOLOGY_TYPE + '\'' +
                '}';
    }
}
