package de.mhus.karaf.commands.impl;

import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.lib.core.M;
import de.mhus.osgi.api.karaf.AbstractCmd;
import de.mhus.osgi.api.services.IServiceManager;

@Command(scope = "service", name = "sb-delete", description = "Delete a service blueprint")
@Service
public class CmdServiceDelete extends AbstractCmd {

    @Argument(
            index = 0,
            name = "implementation",
            required = true,
            description = "Canonical name of implementation class",
            multiValued = false)
    String impl;

    @Override
    public Object execute2() throws Exception {
        IServiceManager api = M.l(IServiceManager.class);
        boolean ret = api.delete(impl);
        if (ret) System.out.println("Deleted");
        else System.out.println("Skipped");
        return null;
    }
}
