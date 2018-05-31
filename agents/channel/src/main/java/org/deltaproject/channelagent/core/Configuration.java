package org.deltaproject.channelagent.core;

public class Configuration {
    private static final Configuration instance = new Configuration();

    public static Configuration getInstance() {
        return instance;
    }

    private String controllerIp;
    private String switchIp;

    private String channelIp;
    private byte[] channelMac;

    private byte ofVersion;
    private String ofPort;


    public void setControllerIp(String val) {
        controllerIp = val;
    }

    public void setSwitchIp(String val) {
        switchIp = val;
    }

    public void setChannelIp(String val) {
        channelIp = val;
    }

    public void setChannelMac(byte[] val) {
        channelMac = val;
    }

    public void setOfVersion(byte val) {
        ofVersion = val;
    }

    public void setOfPort(String val) {
        ofPort = val;
    }

    public String getControllerIp() {
        return controllerIp;
    }

    public String getSwitchIp() {
        return switchIp;
    }

    public String getChannelIp() {
        return channelIp;
    }

    public byte[] getChannelMac() {
        return channelMac;
    }

    public byte getOfVersion() {
        return ofVersion;
    }

    public String getOfPort() {
        return ofPort;
    }
}