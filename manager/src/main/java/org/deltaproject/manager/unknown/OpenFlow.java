package org.deltaproject.manager.unknown;

import java.util.ArrayList;
import java.util.Random;

public class OpenFlow {
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

    private ArrayList<Integer> symmetric;
    private ArrayList<Integer> asymmetric;

    public OpenFlow() {
        symmetric = new ArrayList<Integer>();
        asymmetric = new ArrayList<Integer>();

        asymmetric.add(OpenFlow.ERROR);
        asymmetric.add(OpenFlow.PacketIn);
        asymmetric.add(OpenFlow.FlowRemoved);
        asymmetric.add(OpenFlow.PortStatus);
        asymmetric.add(OpenFlow.PacketOut);
        asymmetric.add(OpenFlow.FlowMod);
        asymmetric.add(OpenFlow.PortMod);

        symmetric.add(OpenFlow.Vendor);
        symmetric.add(OpenFlow.HELLO);
        symmetric.add(OpenFlow.EchoReq);
        symmetric.add(OpenFlow.EchoRes);
        symmetric.add(OpenFlow.FeatureReq);
        symmetric.add(OpenFlow.FeatureRes);
        symmetric.add(OpenFlow.GetConfigReq);
        symmetric.add(OpenFlow.GetConfigRes);
        symmetric.add(OpenFlow.SetConfig);
        symmetric.add(OpenFlow.StatsReq);
        symmetric.add(OpenFlow.StatsRes);
        symmetric.add(OpenFlow.BarrierReq);
        symmetric.add(OpenFlow.BarrierRes);
        symmetric.add(OpenFlow.QueueGetConfigReq);
        symmetric.add(OpenFlow.QueueGetConfigRes);
    }

    public int getRandomAsy() {
        Random ran = new Random();
        return asymmetric.get(ran.nextInt(asymmetric.size()));
    }

    public int getRandomSym() {
        Random ran = new Random();
        return symmetric.get(ran.nextInt(symmetric.size()));
    }
}
