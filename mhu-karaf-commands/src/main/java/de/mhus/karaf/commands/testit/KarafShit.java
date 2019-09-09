package de.mhus.karaf.commands.testit;

import de.mhus.lib.core.MSystem;

public class KarafShit implements ShitIfc {

    @Override
    public void printUsage() {
        System.out.println("sessionid - print current session id");
    }

    @Override
    public Object doExecute(CmdShitYo base, String cmd, String[] parameters) throws Exception {
        switch (cmd) {
        case "sessionid": {
            System.out.println(MSystem.getObjectId(base.getSession()));
        } break;
        }
        return null;
    }

}
