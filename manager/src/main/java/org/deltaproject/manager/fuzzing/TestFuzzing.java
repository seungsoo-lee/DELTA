package org.deltaproject.manager.fuzzing;

import org.deltaproject.manager.analysis.ResultAnalyzer;
import org.deltaproject.manager.analysis.ResultInfo;
import org.deltaproject.manager.core.*;
import org.deltaproject.webui.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestFuzzing {
    private static final Logger log = LoggerFactory.getLogger(TestFuzzing.class.getName());

    private Configuration cfg = Configuration.getInstance();

    private AppAgentManager appm;
    private HostAgentManager hostm;
    private ChannelAgentManager channelm;
    private ControllerManager controllerm;

    private ResultAnalyzer analyzer;

    private int DEFAULT_COUNT = 1;

    public TestFuzzing(AppAgentManager am, HostAgentManager hm, ChannelAgentManager cm, ControllerManager ctm) {
        this.appm = am;
        this.hostm = hm;
        this.channelm = cm;
        this.controllerm = ctm;

        this.analyzer = new ResultAnalyzer(controllerm, appm);
    }

    public void runRemoteAgents(boolean channel, boolean host) {
        log.info("Run channel/host agent..");

        if (channel)
            channelm.runAgent();

        if (host)
            hostm.runAgent("test-fuzzing-topo.py");

        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void stopRemoteAgents(boolean channel, boolean host) {
        log.info("Stop channel/host agent..");

        if (channel)
            channelm.stopAgent();

        if (host)
            hostm.stopAgent();

        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void initController() {
        if (!controllerm.isRunning()) {
            log.info("Target controller [ " + controllerm.getType() + " " + controllerm.getVersion() + " ] is starting..");

            if (controllerm.createController()) {
                log.info("Target controller setup is completed");
            } else {
                log.info("Target controller setup is failed");
                log.info("Terminated the requested test case");
                return;
            }

			/* waiting for switches */
            log.info("Listening to switches..");
            controllerm.isConnectedSwitch(true);
            log.info("All switches are connected");

            if (controllerm.getType().contains("ONOSHandler")) {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } else {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

    public String generateFlow(String proto) {
        hostm.write(proto);
        String result = hostm.read();

        return result;
    }

    public void testFuzzing(TestCase test) {

        switch (test.getcasenum()) {
            case "0.0.010":
                log.info(test.getcasenum() + " - Control Plane Seed-based Fuzzing Test - Finding unknown attack case for control plane");
                break;
            case "0.0.011":
                log.info(test.getcasenum() + " - Control Plane Live Fuzzing Test - Finding unknown attack case for control plane");
                break;
            case "0.0.020":
                log.info(test.getcasenum() + " - Data Plane Seed-based Fuzzing Test - Finding unknown attack case for data plane");
                break;
            case "0.0.021":
                log.info(test.getcasenum() + " - Data Plane Live Fuzzing Test - Finding unknown attack case for data plane");
                break;
        }

        for (int i = 0; i < DEFAULT_COUNT; i++) {
            runRemoteAgents(true, true);

            /* STEP 1: get seed packets */
            log.info("Channel-Agent starts to get seed packets");
            channelm.write(test.getcasenum());
            channelm.read();

            initController();

            log.info("Host-Agent sends packets to others");
            String before = generateFlow("ping");

            try {
                Thread.sleep(5000);                 // 5 seconds
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            /* STEP 2: stop getting seed packets, replay seed packets */
            log.info("Channel-Agent starts to replay seed packets with fuzzing");
            channelm.write("seedstop");

            /* STEP 3: pick handler OF msg */
            stopRemoteAgents(false, true);
            runRemoteAgents(false, true);

            try {
                Thread.sleep(2000);                 // 5 seconds
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            //log.info("Host-Agent sends packets to others again");
            String after = generateFlow("compare");

            log.info("Agent-Manager retrieves the msg from Channel-Agent");
            channelm.write("getmsg");
            String resultChannel = channelm.read();
            log.info(resultChannel);

            log.info("Agent-Manager checks the result of the tese case");
            ResultInfo result = new ResultInfo();

            result.addType(ResultInfo.LATENCY_TIME);
            result.setLatency(before, after);

            result.addType(ResultInfo.COMMUNICATON);
            result.setResult(after);

            result.addType(ResultInfo.CONTROLLER_STATE);
            result.addType(ResultInfo.SWITCH_STATE);

            if (!analyzer.checkResult(test, result)) {
                //log.error(resultChannel);
            }

            stopRemoteAgents(true, true);
            controllerm.flushARPcache();
            controllerm.killController();
        }
    }
}
