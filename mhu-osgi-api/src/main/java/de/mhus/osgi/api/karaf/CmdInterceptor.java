package de.mhus.osgi.api.karaf;

import org.apache.karaf.shell.api.console.Session;

public interface CmdInterceptor {

    void onCmdStart(Session session);
    
    void onCmdEnd(Session session);
    
}
