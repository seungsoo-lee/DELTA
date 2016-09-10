package org.deltaproject.webui;

import org.deltaproject.manager.core.AttackConductor;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by changhoon on 7/5/16.
 */
public class TestCase {

    public enum Status {
        UNKNOWN,
        UNAVAILABLE,
        READY,
        QUEUED,
        RUNNING,
        COMPLETE,
    }

    public enum Category {
        CONTROL_PLANE_OF,
        DATA_PLANE_OF,
        ADVANCED,
        UNSPECIFIED,
    }

    public enum ControllerType {
        ONOS,
        OPENDAYLIGHT,
        FLOODLIGHT,
        UNSPECIFIED,
    }

    public enum TestResult {
        UNKNOWN,
        PASS,
        FAIL,
    }

    private Integer index = -1;
    private Category category;
    private String casenum;
    private String name;
    private ControllerType controllerType;
    private String controllerVer;
    private Status status;
    private Date time;
    private TestResult result;

    public TestCase(String casenum) {
        this.casenum = casenum;
        TestCase testCase = TestCaseDirectory.getDirectory().get(casenum);
        this.name = testCase.getName();
        this.category = testCase.getCategory();
        this.controllerType = testCase.getControllerType();
        this.controllerVer = testCase.getControllerVersion();
        this.status = testCase.getStatus();
        this.time = new Date();
        this.result = TestResult.UNKNOWN;
    }

    public TestCase(Category category, String casenum, String name) {
        this.category = category;
        this.casenum = casenum;
        this.name = name;
        this.controllerType = ControllerType.UNSPECIFIED;
        this.controllerVer = "";
        this.status = Status.UNKNOWN;
        this.time = new Date();
        this.result = TestResult.UNKNOWN;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public void setcasenum(String casenum) {
        this.casenum = casenum;
    }

    public void setControllerType(ControllerType controllerType) {
        this.controllerType = controllerType;
    }

    public void setControllerVersion(String controllerVer) {
        this.controllerVer = controllerVer;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setStatus(Status status) {
        this.time = new Date();
        this.status = status;
    }

    public void setResult(TestResult result) {
        this.result = result;
    }

    public Integer getIndex() {
        return index;
    }

    public Category getCategory() {
        return category;
    }

    public String getcasenum() {
        return casenum;
    }

    public String getName() {
        return name;
    }

    public ControllerType getControllerType() {
        return controllerType;
    }

    public String getControllerVersion() {
        return controllerVer;
    }

    public Status getStatus() {
        return status;
    }

    public String getTime() {

        Format formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return formatter.format(time);
    }

    public TestResult getResult() {
        return result;
    }

    @Override
    public String toString() {
        return "[category=" + category.name() +
                ", index=" + index +
                ", casenum=" + casenum +
                ", controllerType=" + controllerType.name() +
                ", controllerVer=" + controllerVer +
                ", status=" + status.name() +
                "]";
    }
}
