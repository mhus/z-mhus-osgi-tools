package de.mhus.karaf.commands.mhus;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.osgi.framework.Bundle;

import de.mhus.lib.core.MFile;
import de.mhus.lib.core.MProperties;
import de.mhus.lib.core.MString;
import de.mhus.osgi.api.karaf.AbstractCmd;
import de.mhus.osgi.api.services.MOsgi;

@Command(scope = "mhus", name = "dev", description = "Copy example config files from package into karaf evnironment")
@Service
public class CmdDev extends AbstractCmd {

    @Argument(
            index = 0,
            name = "file",
            required = true,
            description = "cp - copy file/dir into workspace, cat cat file, list - site available files",
            multiValued = false)
    String cmd;
    
    @Argument(
            index = 1,
            name = "file",
            required = false,
            description = "file name to copy or 'list'",
            multiValued = false)
    String file;

    @Argument(
            index = 2,
            name = "paramteters",
            required = false,
            description = "Parameters",
            multiValued = true)
    String[] parameters;

    @Option(
            name = "-o",
            aliases = "--output",
            required = false,
            description = "Change target",
            multiValued = false)
    String target;

    @Option(
            name = "-t",
            aliases = "--try",
            required = false,
            description = "try run",
            multiValued = false)
    boolean test;
    
    @Option(
            name = "-y",
            aliases = "--yes-overwrite",
            required = false,
            description = "Overwrite files",
            multiValued = false)
    boolean overwrite;
    
    @Override
    public Object execute2() throws Exception {

        if (cmd.equals("updateall")) {
            for (Bundle bundle : MOsgi.getBundleContext().getBundles()) {
                if (bundle.getVersion().toString().endsWith(".SNAPSHOT")) {
                    System.out.println(">>> " + bundle.getSymbolicName() + ":" + bundle.getVersion());
                    try {
                        bundle.update();
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
            }
        } else
        if (cmd.equals("list")) {
            for (Bundle bundle : MOsgi.getBundleContext().getBundles()) {
                showList(bundle, "/examples");
            }
            return null;
        } else if (cmd.equals("cp")) {
            String in = "examples/" + file;
            if (target == null) {
                target = MString.afterIndex(file, '/');
            }
            Bundle bundle = findBundle(in);

            Enumeration<String> list = bundle.getEntryPaths(in);
            if (list == null)
                copyFile(bundle, in, target);
            else
                copyDir(bundle, in, target);
        } else if (cmd.equals("cat")) {
            String in = "examples/" + file;
            Bundle bundle = findBundle(in);
            URL url = bundle.getEntry(in);
            try (InputStream is = url.openStream()) {
                MFile.copyFile(is, System.out);
            }
            
        }
        return null;
    }

    private void copyDir(Bundle bundle, String in, String target) {
        Enumeration<String> list = bundle.getEntryPaths(in);
        if (list == null) return;
        while (list.hasMoreElements()) {
            String sub = list.nextElement();
            if (sub.endsWith("/"))
                copyDir(bundle, sub, target);
            else {
                String out = target + sub.substring(9 + file.length());
                try {
                    copyFile(bundle, sub, out);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void copyFile(Bundle bundle, String from, String to) throws IOException {
        if (to.startsWith("/")) to = to.substring(1);
        if (!from.startsWith("/")) from = "/" + from;
        System.out.println("cp " + from + " -> " + to);
        URL inUrl = bundle.getEntry(from);
        if (inUrl == null) {
            System.out.println("- not found: " + file);
            return;
        }
        InputStream is = inUrl.openStream();
        if (is == null) {
            System.out.println("- can't open file stream: " + inUrl);
            return;
        }
        try (is) {
            File out = new File(to);
            if (out.exists() && !overwrite)
                System.out.println("- file already exists");
            else {
                long size = -1;
                if (parameters == null) {
                    if (!test) {
                        try (FileOutputStream os = new FileOutputStream(out)) {
                            size = MFile.copyFile(is, os);
                        }
                    }
                } else {
                    MProperties p = MProperties.explodeToMProperties(parameters);
                    String content = MFile.readFile(is);
                    int pos = 0;
                    while (true) {
                        pos = content.indexOf("#{",pos);
                        if (pos < 0) break;
                        if (pos > 0 && content.charAt(pos-1) == '#') {
                            content = content.substring(0, pos) + content.substring(pos+1);
                            pos = pos + 1;
                            continue;
                        }
                        int end = content.indexOf("}",pos);
                        if (end < 0) {
                            System.out.println("- Error: Open parameter definition");
                            break;
                        }
                        String key = content.substring(pos+2,end);
                        String def = null;
                        int pos2 = key.indexOf(':');
                        if (pos2 > 0) {
                            def = key.substring(pos2+1);
                            key = key.substring(0,pos2);
                        }
                        String value = p.getString(key, def);
                        if (value == null) {
                            System.out.println("- Parameter not defined for " + key + " - leave");
                            pos = end + 1;
                            continue;
                        } else if (test)
                            System.out.println("- Set " + key + " to " + value);
                        content = content.substring(0, pos) + value + content.substring(end+1);
                        pos = pos + value.length();
                    }
                    if (!test) {
                        try (FileOutputStream os = new FileOutputStream(out)) {
                            if (MFile.writeFile(os, content))
                                size = content.length();
                        }
                    }
                }
                System.out.println("- " + size + " bytes");
            }
        }

    }

    private Bundle findBundle(String path) {
        for (Bundle bundle : MOsgi.getBundleContext().getBundles()) {
            URL url = bundle.getEntry(path);
            if (url != null) return bundle;
        }
        return null;
    }

    private void showList(Bundle bundle, String path) {
        Enumeration<String> list = bundle.getEntryPaths(path);
        if (list == null) return;
        while (list.hasMoreElements()) {
            String sub = list.nextElement();
            if (sub.endsWith("/"))
                showList(bundle, sub);
            else
                System.out.println(bundle.getBundleId() + " " + sub.substring(9));
        }
    }

}
