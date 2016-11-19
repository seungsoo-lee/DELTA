package org.deltaproject.channelagent.testcase;

/**
 * Created by seungsoo on 10/26/16.
 */
public class TestCase {
    public static final int TEST = -1;

    public static final int EMPTY = 0;
    public static final int MITM = 1;
    public static final int EVAESDROP = 2;
    public static final int LINKFABRICATION = 3;
    public static final int CONTROLMESSAGEMANIPULATION = 4;
    public static final int MALFORMEDCONTROLMESSAGE = 5;

    public static final int CONTROLPLANE_FUZZING = 6;
    public static final int DATAPLANE_FUZZING = 7;

    public static final int SEED_BASED_FUZZING = 8;
}
