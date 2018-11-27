package org.deltaproject.onosagent.cli;

import iitp2018.AttackGenerator;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.onosproject.cli.AbstractShellCommand;
import org.onosproject.core.CoreService;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.flow.FlowRuleService;
import org.onosproject.net.link.LinkService;
import org.onosproject.openflow.controller.OpenFlowController;

@Command(scope = "onos", name = "attack",
        description = "Execute specific attack cases")
public class AttackGeneratorCommand extends AbstractShellCommand {

    @Argument(index = 0, name = "case_num",
            description = "Attack Case Number",
            required = true, multiValued = false)
    int num = 0;


    @Override
    protected void execute() {

        OpenFlowController controller = get(OpenFlowController.class);
        DeviceService ds = get(DeviceService.class);
        FlowRuleService fs = get(FlowRuleService.class);
        CoreService cs = get(CoreService.class);
        LinkService ls = get(LinkService.class);

        AttackGenerator attackGenerator = new AttackGenerator(controller, ds, fs, cs, ls);

        switch(num) {
            case 1:
                break;
            case 2:
                attackGenerator.modifyOutputActions();
                break;
            case 3:
                break;
            case 4:
                break;
            case 5:
                break;
        }
    }
}
