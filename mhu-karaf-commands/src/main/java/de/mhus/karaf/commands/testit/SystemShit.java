package de.mhus.karaf.commands.testit;

import java.lang.reflect.Method;
import java.net.InetAddress;

import de.mhus.lib.core.M;
import de.mhus.lib.core.MApi;
import de.mhus.osgi.api.util.OsgiBundleClassLoader;

public class SystemShit implements ShitIfc {

    @Override
    public void printUsage() {
        System.out.println("lookup <ifc> [<def>]");
        System.out.println("myip - Print my Ip and hostname");
    }

    @Override
    public Object doExecute(CmdShitYo base, String cmd, String[] parameters) throws Exception {
        if ("myip".equals(cmd)) {
            InetAddress inetAddress = InetAddress.getLocalHost();
            System.out.println("IP Address: " + inetAddress.getHostAddress());
            System.out.println("Host Name : " + inetAddress.getHostName());
        } else if (cmd.equals("lookup")) {
            OsgiBundleClassLoader loader = new OsgiBundleClassLoader();
            Class<?> ifc = loader.loadClass(parameters[0]);
            Object obj = null;
            if (parameters.length > 1) {
                Class<?> def = loader.loadClass(parameters[1]);
                Method method = MApi.class.getMethod("lookup", Class.class, Class.class);
                obj = method.invoke(null, ifc, def);
            } else {
                obj = M.l(ifc);
            }

            if (obj != null) {
                System.out.println(obj.getClass());
            }
            return obj;
        }
        return null;
    }

}
