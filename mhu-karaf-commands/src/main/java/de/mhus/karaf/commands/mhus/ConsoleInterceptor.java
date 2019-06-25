package de.mhus.karaf.commands.mhus;

import org.apache.karaf.shell.api.console.Session;

import de.mhus.lib.core.console.Console;
import de.mhus.osgi.api.karaf.CmdInterceptor;

public class ConsoleInterceptor implements CmdInterceptor {

    private Console console;
    private Console old;
    
    public ConsoleInterceptor(Console console) {
        this.console = console;
    }
    
    @Override
    public void onCmdStart(Session session) {
        old = Console.get();
        Console.set(console);
    }

    @Override
    public void onCmdEnd(Session session) {
        Console.set(old);
    }

    public Console get() {
        return console;
    }

}
