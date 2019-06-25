package de.mhus.osgi.api.karaf;

import org.apache.karaf.shell.api.action.Action;

import de.mhus.lib.core.lang.MObject;

public abstract class AbstractCmd extends MObject implements Action {

    @Override
    public final Object execute() throws Exception {
        return execute2();
    }

    public abstract Object execute2() throws Exception;
    
}
