package de.mhus.karaf.commands.shell;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.osgi.api.karaf.AbstractCmd;

@Command(scope = "shell", name = "nslookup", description = "Lookup dns names and ip")
@Service
public class CmdNsLookup extends AbstractCmd {

    @Argument(index=0, name="name", required=true, description="DNS name or IP", multiValued=false)
    String name;

    @Override
    public Object execute2() throws Exception {
        
        try {
            InetAddress ipAddress = InetAddress.getByName(name);
            System.out.println("Hostname : " + ipAddress.getHostName());
            System.out.println("Canonical: " + ipAddress.getCanonicalHostName());
            System.out.println("IP       : " + ipAddress.getHostAddress());
        } catch (UnknownHostException e) {
            System.out.println("IP address not found for: " + name );
        }
        return null;
    }

}
