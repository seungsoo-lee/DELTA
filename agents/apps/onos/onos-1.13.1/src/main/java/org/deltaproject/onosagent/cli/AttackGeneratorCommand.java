package onos.deltaproject.appagent.cli;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.onosproject.cli.AbstractShellCommand;

@Command(scope = "onos", name = "delta",
        description = "Execute specific attack cases")
public class AttackGeneratorCommand extends AbstractShellCommand {

    @Argument(index = 0, name = "name",
            description = "Name of the internal BGP speaker",
            required = true, multiValued = false)
    String name = null;

    @Argument(index = 1, name = "ip",
            description = "IP address of the BGP peer",
            required = true, multiValued = false)
    String ip = null;

    @Override
    protected void execute() {

    }
}
