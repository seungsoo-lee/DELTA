package org.deltaproject.manager.fuzzing;

import de.learnlib.algorithms.lstargeneric.mealy.ExtensibleLStarMealyBuilder;
import de.learnlib.api.EquivalenceOracle;
import de.learnlib.api.LearningAlgorithm;
import de.learnlib.api.SUL;
import de.learnlib.cache.sul.SULCaches;
import de.learnlib.drivers.reflect.AbstractMethodInput;
import de.learnlib.drivers.reflect.AbstractMethodOutput;
import de.learnlib.drivers.reflect.SimplePOJOTestDriver;
import de.learnlib.eqtests.basic.mealy.RandomWalkEQOracle;
import de.learnlib.experiments.Experiment;
import de.learnlib.oracles.ResetCounterSUL;
import de.learnlib.oracles.SULOracle;
import de.learnlib.statistics.SimpleProfiler;
import de.learnlib.statistics.StatisticSUL;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.commons.dotutil.DOT;
import net.automatalib.util.graphs.dot.GraphDOT;
import net.automatalib.words.Word;
import org.deltaproject.manager.core.AppAgentManager;
import org.projectfloodlight.openflow.exceptions.OFParseError;
import org.projectfloodlight.openflow.protocol.*;
import org.projectfloodlight.openflow.types.U16;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Method;
import java.net.*;
import java.util.*;

/**
 * Created by seungsoo on 12/10/16.
 */
public class TestStateDiagram {
    private static final Logger log = LoggerFactory.getLogger(TestStateDiagram.class.getName());

    private static AppAgentManager appm;

    public TestStateDiagram(AppAgentManager appm) {
        this.appm = appm;
    }

    public static class OpenFlowHandshake {
        private OFFactory factory;
        private OFMessageReader<OFMessage> reader;

        private Socket socket = null;
        private InputStream in;
        private OutputStream out;

        // capacity
        public static final int MAX_SIZE = 5;
        private Deque<String> data = new ArrayDeque<>(5);

        private BufferedReader stdOut;
        private Thread logThread = null;

        public OpenFlowHandshake() {
            boolean err;
            factory = OFFactories.getFactory(OFVersion.OF_10);
            reader = factory.getReader();

            while (true) {
                try {
                    if (socket != null)
                        socket.close();

                    socket = new Socket("10.100.100.11", 6633);
                    socket.setSoTimeout(2000);
                    socket.setReuseAddress(true);

                    in = socket.getInputStream();
                    out = socket.getOutputStream();

                    byte[] recv = new byte[8];
                    in.read(recv, 0, recv.length);

                    err = false;
                } catch (ConnectException e) {
                    e.printStackTrace();
                    err = true;
                } catch (UnknownHostException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    err = true;
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    err = true;
                }

                if (err) {
                    try {
                        Runtime.getRuntime().exec("ssh vagrant@10.100.100.11 sudo pkill java");
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            continue;
                        }

                        Process process = Runtime.getRuntime().exec("ssh vagrant@10.100.100.11 java -jar floodlight-0.91.jar");
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            continue;
                        }

                        String str = "";
                        stdOut = new BufferedReader(new InputStreamReader(process.getInputStream()));
                        while ((str = stdOut.readLine()) != null) {
                            if (str.contains("Starting DebugServer on :6655")) {
                                break;
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("[*] New test case");
                    break;
                }
            }


//            while (true) {
//                try {
//                    Runtime.getRuntime().exec("ssh vagrant@10.100.100.11 sudo pkill java");
//
//                    try {
//                        Thread.sleep(500);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                        continue;
//                    }
//
//                    Process process = Runtime.getRuntime().exec("ssh vagrant@10.100.100.11 java -jar floodlight-0.91.jar");
//
//                    try {
//                        Thread.sleep(500);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                        continue;
//                    }
//
//                    String str = "";
//                    stdOut = new BufferedReader(new InputStreamReader(process.getInputStream()));
//                    while ((str = stdOut.readLine()) != null) {
//                        if (str.contains("Starting DebugServer on :6655")) {
//                            break;
//                        }
//                    }
//
//                    try {
//                        Thread.sleep(500);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                        continue;
//                    }
//
//                    //                logThread = new Thread() {
//                    //                    public synchronized void run() {
//                    //                        String str = "";
//                    //                        try {
//                    //                            while ((str = stdOut.readLine()) != null) {
//                    //                                System.out.println("    [+] " + str);
//                    //                            }
//                    //                        } catch (IOException e) {
//                    //                            System.out.println("*** FAIL TO RUN THREAD");
//                    //                            e.printStackTrace();
//                    //                        }
//                    //                    }
//                    //                };
//                    //                logThread.start();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    continue;
//                }
//
//
//
//                factory = OFFactories.getFactory(OFVersion.OF_10);
//                reader = factory.getReader();
//
//                System.out.println("[*] Ready to learn");
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                    continue;
//                }
//
//                break;
//            }
        }

        public void sendRawMsg(byte[] msg) {
            try {
                this.out.write(msg, 0, msg.length);
            } catch (SocketException e) {
                System.out.println("Socket closed before sending");
            } catch (IOException e) {
                // TODO Auto-gaenerated catch block
                e.printStackTrace();
            }
        }

        public byte[] hexStringToByteArray(String s) {
            int len = s.length();
            byte[] data = new byte[len / 2];
            for (int i = 0; i < len; i += 2) {
                data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
            }
            return data;
        }

        public String getInternalUpdate(String type) {

            return "";
        }

        public String recvOFMsg(String type) {
            System.out.println("  [+] Recv msg type = " + type);

            byte[] msg = null;
            byte[] recv = new byte[2048];
            int readlen;

            switch (type) {
                // Handshake
                case "HELLO":
                    msg = hexStringToByteArray(DummyOF10.HELLO);
                    break;
                case "FEATURE_REPLY":
                    msg = hexStringToByteArray(DummyOF10.FEATURE_REPLY);
                    break;
                case "GET_CONFIG_REPLY":
                    msg = hexStringToByteArray(DummyOF10.GET_CONFIG_REPLY);
                    break;
                case "BARRIER_REPLY":
                    msg = hexStringToByteArray(DummyOF10.BARRIER_REPLY);
                    break;

                // Operation
                case "ERROR":
                    msg = hexStringToByteArray(DummyOF10.ERROR);
                    break;
                case "ECHO_REPLY":
                    msg = hexStringToByteArray(DummyOF10.ECHO_REPLY);
                    break;
                case "FLOW_REMOVED":
                    msg = hexStringToByteArray(DummyOF10.FLOW_REMOVED);
                    break;
                case "PACKET_IN":
                    msg = hexStringToByteArray(DummyOF10.PACKET_IN);
                    break;
                case "STATS_REPLY":
                    msg = hexStringToByteArray(DummyOF10.STATS_REPLY);
                    break;
                case "PORT_STATUS":
                    msg = hexStringToByteArray(DummyOF10.PORT_STATUS);
                    break;
                case "QUEUE_GET_CONFIG_REPLY":
                    msg = hexStringToByteArray(DummyOF10.QUEUE_GET_CONFIG_REPLY);
                    break;
                case "VENDOR":
                    msg = hexStringToByteArray(DummyOF10.VENDOR);
                    break;
            }

            sendRawMsg(msg);

            ArrayList<String> msgList = null;
            String output = "send ";

            while (true) {
                try {
                    while ((readlen = in.read(recv, 0, recv.length)) != -1) {
                        msgList = parseOFMsg(recv, readlen);
                    }

                    if (readlen == -1) {
                        //if (msgList == null) {
                        //    //output = "Switch Disconnection";
                        //    break;
                        //} else {
                        //    msgList.add("Switch Disconnection");
                        //    break;
                        //}
                        if (msgList == null) {
                            msgList = new ArrayList<>();
                        }
                        msgList.add("Switch Disconnection");
                        break;
                    } else if (readlen == 0) {
                        //if (msgList == null) {
                        //    output = "Length 0";
                        //    break;
                        //} else {
                        //    msgList.add("Length 0");
                        //    break;
                        //}
                        if (msgList == null) {
                            msgList = new ArrayList<>();
                        }
                        msgList.add("Length 0");
                        break;
                    }
                } catch (SocketTimeoutException e) {
                    //if (msgList == null) {
                    //    output = "SocketTimeoutException";
                    //    break;
                    //} else {
                    //    msgList.add("SocketTimeoutException");
                    //    break;
                    //}
                    if (msgList == null) {
                        msgList = new ArrayList<>();
                    }
                    msgList.add("SocketTimeoutException");
                    break;
                } catch (SocketException e) {
                    //if (msgList == null) {
                    //    output = "SocketException";
                    //    break;
                    //} else {
                    //    msgList.add("SocketException");
                    //    break;
                    //}
                    if (msgList == null) {
                        msgList = new ArrayList<>();
                    }
                    msgList.add("SocketException");
                    break;
                } catch (NegativeArraySizeException e) {
                    //if (msgList == null) {
                    //    output = "NegativeArraySizeException";
                    //    break;
                    //} else {
                    //    msgList.add("NegativeArraySizeException");
                    //    break;
                    //}
                    if (msgList == null) {
                        msgList = new ArrayList<>();
                    }
                    msgList.add("NegativeArraySizeException");
                    break;
                } catch (OFParseError e) {
                    // ignore ack packet
                    continue;
                } catch (Exception e) {
                    //if (msgList == null) {
                    //    output = "Exception";
                    //    e.printStackTrace();
                    //    break;
                    //} else {
                    //    msgList.add("Exception");
                    //    break;
                    //}
                    if (msgList == null) {
                        msgList = new ArrayList<>();
                    }
                    msgList.add("Exception");
                    break;
                }
            }

            if (msgList != null) {
                for (String s : msgList) {
                    output += (s + "/");
                }
            } else {
                output = "empty";
            }

            System.out.println("    [-] Result: " + output);
            return output;
        }

        public ArrayList<String> parseOFMsg(byte[] recv, int len) throws OFParseError {
            // for OpenFlow Message
            byte[] rawMsg = new byte[len];
            System.arraycopy(recv, 0, rawMsg, 0, len);
            ByteBuf bb = Unpooled.copiedBuffer(rawMsg);

            int totalLen = bb.readableBytes();
            int offset = bb.readerIndex();

            ArrayList<String> msgList = new ArrayList<>();

            while (offset < totalLen) {
                bb.readerIndex(offset);

                byte version = bb.readByte();
                byte type = bb.readByte();
                int length = U16.f(bb.readShort());
                bb.readerIndex(offset);

                if (length < 8)
                    throw new OFParseError("Wrong length: Expected to be >= " + 8 + ", was: " + length);

                try {
                    OFMessage message = reader.readFrom(bb);

                    long xid = message.getXid();

                    // handshake
                    if (message.getType() == OFType.HELLO) {
                        msgList.add("HELLO");
                    } else if (message.getType() == OFType.FEATURES_REQUEST) {
                        msgList.add("FEATURES_REQUEST");
                    } else if (message.getType() == OFType.GET_CONFIG_REQUEST) {
                        msgList.add("GET_CONFIG_REQUEST");
                    } else if (message.getType() == OFType.SET_CONFIG) {
                        msgList.add("SET_CONFIG");

                        // operation
                    } else if (message.getType() == OFType.ECHO_REQUEST) {
                        msgList.add("ECHO_REQUEST");
                    } else if (message.getType() == OFType.EXPERIMENTER) {
                        msgList.add("EXPERIMENTER");
                    } else if (message.getType() == OFType.PACKET_OUT) {
                        msgList.add("PACKET_OUT");
                    } else if (message.getType() == OFType.FLOW_MOD) {
                        msgList.add("FLOW_MOD");
                    } else if (message.getType() == OFType.PORT_MOD) {
                        msgList.add("PORT_MOD");
                    } else if (message.getType() == OFType.STATS_REQUEST) {
                        msgList.add("STATS_REQUEST");
                    } else if (message.getType() == OFType.BARRIER_REQUEST) {
                        msgList.add("BARRIER_REQUEST");
                    } else
                        msgList.add("Others");

                } catch (OFParseError e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    msgList.add("OFParseError");
                }

                offset += length;
            }

            bb.clear();
            return msgList;
        }
    }

    public void extractStateDiagram() {
        // instantiate test driver
        SimplePOJOTestDriver driver = null;
        Method ofRecv = null;

        try {
            driver = new SimplePOJOTestDriver(
                    OpenFlowHandshake.class.getConstructor());
            // create learning alphabet
            ofRecv = OpenFlowHandshake.class.getMethod("recvOFMsg", new Class<?>[]{String.class});
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }


        AbstractMethodInput send_0 = driver.addInput("recv HELLO", ofRecv, "HELLO");
//        AbstractMethodInput send_1 = driver.addInput("recv ERROR", ofRecv, "ERROR");
//        AbstractMethodInput send_3 = driver.addInput("recv ECHO_REPLY", ofRecv, "ECHO_REPLY");
        AbstractMethodInput send_4 = driver.addInput("recv VENDOR", ofRecv, "VENDOR");
        AbstractMethodInput send_6 = driver.addInput("recv FEATURE_REPLY", ofRecv, "FEATURE_REPLY");
        AbstractMethodInput send_8 = driver.addInput("recv GET_CONFIG_REPLY", ofRecv, "GET_CONFIG_REPLY");
//        AbstractMethodInput send_10 = driver.addInput("recv PACKET_IN", ofRecv, "PACKET_IN");
//        AbstractMethodInput send_11 = driver.addInput("recv FLOW_REMOVED", ofRecv, "FLOW_REMOVED");
//        AbstractMethodInput send_12 = driver.addInput("recv PORT_STATUS", ofRecv, "PORT_STATUS");
        AbstractMethodInput send_17 = driver.addInput("recv STATS_REPLY", ofRecv, "STATS_REPLY");
        AbstractMethodInput send_19 = driver.addInput("recv BARRIER_REPLY", ofRecv, "BARRIER_REPLY");


        // oracle for counting queries wraps sul
        StatisticSUL<AbstractMethodInput, AbstractMethodOutput> statisticSul =
                new ResetCounterSUL<>("membership queries", driver);

        SUL<AbstractMethodInput, AbstractMethodOutput> effectiveSul = statisticSul;

        // use caching in order to avoid duplicate queries
        effectiveSul = SULCaches.createCache(driver.getInputs(), effectiveSul);

        SULOracle<AbstractMethodInput, AbstractMethodOutput> mqOracle = new SULOracle<>(effectiveSul);

        // create initial set of suffixes
        List<Word<AbstractMethodInput>> suffixes = new ArrayList<>();
        suffixes.add(Word.fromSymbols(send_0));
//        suffixes.add(Word.fromSymbols(send_1));
//        suffixes.add(Word.fromSymbols(send_3));
        suffixes.add(Word.fromSymbols(send_4));
        suffixes.add(Word.fromSymbols(send_6));
        suffixes.add(Word.fromSymbols(send_8));
//        suffixes.add(Word.fromSymbols(send_10));
//        suffixes.add(Word.fromSymbols(send_11));
//        suffixes.add(Word.fromSymbols(send_12));
        suffixes.add(Word.fromSymbols(send_17));
        suffixes.add(Word.fromSymbols(send_19));

        // construct L* instance (almost classic Mealy version)
        // almost: we use words (Word<String>) in cells of the table
        // instead of single outputs.
        LearningAlgorithm.MealyLearner<AbstractMethodInput, AbstractMethodOutput> lstar
                = new ExtensibleLStarMealyBuilder<AbstractMethodInput, AbstractMethodOutput>()
                .withAlphabet(driver.getInputs())   // input alphabet
                .withOracle(mqOracle)               // membership oracle
                .create();

        // create random walks equivalence test
        EquivalenceOracle.MealyEquivalenceOracle<AbstractMethodInput, AbstractMethodOutput> randomWalks =
                new RandomWalkEQOracle<>(
                        0.05, // reset SUL w/ this probability before a step
                        100, // max steps (overall)
                        false, // reset step count after counterexample
                        new Random(46346293), // make results reproducible
                        driver // system under learning
                );

        // construct a learning experiment from
        // the learning algorithm and the random walks test.
        // The experiment will execute the main loop of
        // active learning
        Experiment.MealyExperiment<AbstractMethodInput, AbstractMethodOutput> experiment =
                new Experiment.MealyExperiment<>(lstar, randomWalks, driver.getInputs());

        // turn on time profiling
        experiment.setProfile(true);

        // enable logging of models
        experiment.setLogModels(true);

        // run experiment
        experiment.run();

        // get learned model
        MealyMachine<?, AbstractMethodInput, ?, AbstractMethodOutput> result =
                experiment.getFinalHypothesis();

        // report results
        System.out.println("———————————————————————————");

        // profiling
        System.out.println(SimpleProfiler.getResults());

        // learning statistics
        System.out.println(experiment.getRounds().getSummary());
        System.out.println(statisticSul.getStatisticalData().getSummary());

        // model statistics
        System.out.println("States: " + result.size());
        System.out.println("Sigma: " + driver.getInputs().size());

        // show model
        System.out.println();
        System.out.println("Model: ");

        try {
            GraphDOT.write(result, driver.getInputs(), System.out); // may throw IOException!
            Writer w = DOT.createDotWriter(true);
            GraphDOT.write(result, driver.getInputs(), w);
            w.close();

            System.out.println("———————————————————————————");
            Runtime.getRuntime().exec("ssh vagrant@10.100.100.11 sudo pkill java");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

