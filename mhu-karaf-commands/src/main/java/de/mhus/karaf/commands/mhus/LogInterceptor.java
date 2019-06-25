package de.mhus.karaf.commands.mhus;

import org.apache.karaf.shell.api.console.Session;

import de.mhus.lib.core.logging.MLogUtil;
import de.mhus.osgi.api.karaf.CmdInterceptor;

public class LogInterceptor implements CmdInterceptor {

    private String cfg;

    public LogInterceptor(String cfg) {
        MLogUtil.setTrailConfig(MLogUtil.TRAIL_SOURCE_SHELL, cfg);
        this.cfg = MLogUtil.getTrailConfig();
        MLogUtil.releaseTrailConfig();
    }

    @Override
    public void onCmdStart(Session session) {
        MLogUtil.setTrailConfig(MLogUtil.TRAIL_SOURCE_SHELL, cfg);
    }

    @Override
    public void onCmdEnd(Session session) {
        MLogUtil.releaseTrailConfig();
    }

    public String getConfig() {
        return cfg;
    }
    
}
