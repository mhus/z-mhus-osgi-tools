package de.mhus.karaf.commands.impl;

import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.lib.core.M;
import de.mhus.osgi.api.karaf.AbstractCmd;
import de.mhus.osgi.api.services.IServiceManager;

@Command(scope = "service", name = "sb-reload", description = "Reload configured services")
@Service
public class CmdServiceReload extends AbstractCmd {

    @Override
    public Object execute2() throws Exception {
        IServiceManager api = M.l(IServiceManager.class);
        api.reloadConfigured();
        System.out.println("OK");
        return null;
    }

}
