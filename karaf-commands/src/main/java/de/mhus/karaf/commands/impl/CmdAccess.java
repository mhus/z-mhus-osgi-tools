package de.mhus.karaf.commands.impl;

import java.util.Locale;

import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.api.console.Session;
import org.apache.shiro.subject.Subject;

import de.mhus.lib.core.M;
import de.mhus.lib.core.MApi;
import de.mhus.lib.core.console.Console;
import de.mhus.lib.core.shiro.AccessApi;
import de.mhus.lib.core.shiro.AccessUtil;
import de.mhus.lib.core.shiro.SubjectEnvironment;
import de.mhus.osgi.api.karaf.AbstractCmd;
import de.mhus.osgi.api.karaf.CmdInterceptor;
import de.mhus.osgi.api.karaf.CmdInterceptorUtil;

@Command(scope = "shiro", name = "access", description = "Access Control")
@Service
public class CmdAccess extends AbstractCmd {

    @Reference Session session;

    @Argument(
            index = 0,
            name = "cmd",
            required = true,
            description =
                    "Command to execute"
                    + "\n  admin - try to login as admin"
                    + "\n  login <user> [password]"
                    + "\n  logout"
                    + "\n  id - print current id"
                    + "\n  subject - print current subject and session information"
                    + "\n  restart - restart engine"
            ,
            multiValued = false)
    String cmd;

    @Argument(
            index = 1,
            name = "paramteters",
            required = false,
            description = "Parameters",
            multiValued = true)
    String[] parameters;
    
    @Override
    public Object execute2() throws Exception {

        if (cmd.equals("restart")) {
            M.l(AccessApi.class).restart();
            System.out.println("OK");
        } else
        if (cmd.equals("admin")) {
            String user = "admin";
            String pass = MApi.get().getCfgString(AccessApi.class, "adminPassword", "secret");
            Subject subject = M.l(AccessApi.class).createSubject();
            AccessUtil.login(subject, user, pass, true, Locale.getDefault());
            CmdInterceptorUtil.setInterceptor(session, new AaaInterceptor(subject));
            System.out.println("OK");
        } else
        if (cmd.equals("logout")) {
            CmdInterceptorUtil.removeInterceptor(session, AaaInterceptor.class);
//            CmdInterceptorUtil.setInterceptor(session, new AaaInterceptor(null));
            System.out.println("OK");
        } else
        if (cmd.equals("login")) {
            String user = parameters[0];
            String pass = null;
            if (parameters.length > 1)
                pass = parameters[1];
            else
                pass = Console.get().readPassword();
            Subject subject = M.l(AccessApi.class).createSubject();
            AccessUtil.login(subject, user, pass, true, Locale.getDefault());
            CmdInterceptorUtil.setInterceptor(session, new AaaInterceptor(subject));
            System.out.println("OK");
        } else
        if (cmd.equals("id")) {
            System.out.println(AccessUtil.getPrincipal());
        } else if (cmd.equals("subject")) {
            Subject subject = AccessUtil.getSubject();
            System.out.println("Subject: " + subject);
            System.out.println("Principal: " + String.valueOf(subject.getPrincipal()));
            System.out.println("Authenticated: " + subject.isAuthenticated());
            System.out.println("Session: " + subject.getSession(false));
        }
        return null;
    }

    
    private static class AaaInterceptor implements CmdInterceptor {

        private Subject subject;
        private SubjectEnvironment env;

        public AaaInterceptor(Subject subject) {
            this.subject = subject;
        }

        @Override
        public void onCmdStart(Session session) {
            if (subject != null)
                env = AccessUtil.useSubject(subject);
        }

        @Override
        public void onCmdEnd(Session session) {
            if (env != null)
                env.close();
            env = null;
        }
    }    
}
