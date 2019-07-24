package de.mhus.osgi.api.karaf;

import java.util.List;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.console.Session;

import de.mhus.lib.core.lang.MObject;

public abstract class AbstractCmd extends MObject implements Action {

    @Option(name = "-tbl", aliases = { "--table" }, description = "Console table options", required = false, multiValued = false)
    protected String tblOpt;

    @Option(name = "-ta", aliases = { "--table-all" }, description = "Console table print all", required = false, multiValued = false)
    private boolean tableAll;

    @Reference
    private Session session;

    @Override
    public final Object execute() throws Exception {
        if (tableAll) {
            if (tblOpt == null) tblOpt = "all=1"; else tblOpt = "all=1 " + tblOpt;
        }
        @SuppressWarnings("unchecked")
        List<CmdInterceptor> interceptors = (List<CmdInterceptor>) session.get(CmdInterceptorUtil.SESSION_KEY);
        if (interceptors != null) {
            for (CmdInterceptor interceptor : interceptors)
                interceptor.onCmdStart(session);
        }
        Object ret = null;
        try {
            ret = execute2();
        } finally {
            if (interceptors != null) {
                for (CmdInterceptor interceptor : interceptors)
                    interceptor.onCmdEnd(session);
            }
        }
        return ret;
    }

    public abstract Object execute2() throws Exception;
    
}
