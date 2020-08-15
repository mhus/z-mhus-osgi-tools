package de.mhus.karaf.commands.impl;

import java.util.List;

import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.lib.core.M;
import de.mhus.osgi.api.karaf.AbstractCmd;
import de.mhus.osgi.api.services.IServiceManager;

@Command(scope = "service", name = "sb-list", description = "List managed service blueprint")
@Service
public class CmdServiceList extends AbstractCmd {

    @Override
    public Object execute2() throws Exception {
        IServiceManager api = M.l(IServiceManager.class);
        List<String> list = api.list();
        for (String entry : list) {
            System.out.println(entry);
        }
        return null;
    }
}
