/**
 * Copyright (C) 2020 Mike Hummel (mh@mhus.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.mhus.osgi.dev.dev;

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

import de.mhus.lib.core.IProperties;
import de.mhus.lib.core.MCollection;
import de.mhus.lib.core.MFile;
import de.mhus.lib.core.MProperties;
import de.mhus.lib.core.MString;
import de.mhus.osgi.api.MOsgi;
import de.mhus.osgi.api.karaf.AbstractCmd;

@Command(
        scope = "mhus",
        name = "dev-res",
        description = "Copy example config files from package into karaf evnironment")
@Service
public class CmdDevResources extends AbstractCmd {

    @Argument(
            index = 0,
            name = "file",
            required = true,
            description =
                    "cp - copy file/dir into workspace, cat cat file, list - site available files",
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

    @Option(
            name = "-e",
            aliases = "--env",
            required = false,
            description = "Substitute environment parameter values",
            multiValued = false)
    boolean env;
    
    @Option(
            name = "-x",
            aliases = "--extensions",
            required = false,
            description = "File extensions to substitute: xml,txt,cfg,properties",
            multiValued = false)
    String substituteExtensions = "xml,txt,cfg,properties";

    private MProperties p;

    @Override
    public Object execute2() throws Exception {

        if (cmd.equals("list")) {
            for (Bundle bundle : MOsgi.getBundleContext().getBundles()) {
                showList(bundle, "/examples");
            }
            return null;
        } else if (cmd.equals("cp")) {

            p = new MProperties();
            if (env) {
                p.putAll( System.getenv() );
            }
            if (parameters != null) {
                p.putReadProperties(IProperties.explodeToMProperties(parameters));
            }
            String bundleName = MString.beforeIndex(file, '/');
            file = MString.afterIndex(file, '/');

            String in = "examples/" + file;
            if (target == null) {
                target = MString.afterIndex(file, '/');
            }
            Bundle bundle = findBundleForName(bundleName);

            Enumeration<String> list = bundle.getEntryPaths(in);
            if (list == null) copyFile(bundle, in, target);
            else copyDir(bundle, in, target);
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
            if (sub.endsWith("/")) copyDir(bundle, sub, target);
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
            if (out.exists() && !overwrite) System.out.println("- file already exists");
            else {
                if (!out.getParentFile().exists())
                    out.getParentFile().mkdirs();
                long size = -1;
                if (p.size() == 0 || !substituteFileExtension(out)) {
                    if (!test) {
                        try (FileOutputStream os = new FileOutputStream(out)) {
                            size = MFile.copyFile(is, os);
                        }
                    }
                } else {
                    System.out.println("- substitute");
                    String content = MFile.readFile(is);
                    int pos = 0;
                    while (true) {
                        pos = content.indexOf("{{", pos);
                        if (pos < 0) break;
//                        if (pos > 0 && content.charAt(pos - 1) == '#') {
//                            content = content.substring(0, pos) + content.substring(pos + 1);
//                            pos = pos + 1;
//                            continue;
//                        }
                        int end = content.indexOf("}}", pos);
                        if (end < 0) {
                            System.out.println("- Error: Open parameter definition");
                            break;
                        }
                        String key = content.substring(pos + 2, end);
                        String[] parts = key.split(":",3);
                        String value = null;
                        if (parts[0].equals("env")) {
                            value = p.getString(parts[1], parts.length > 2 ? parts[2] : null);
                        }
                        if (value == null) {
                            System.out.println("- Parameter not defined for " + key + " - set to empty");
                            value = "";
                        } else 
                            System.out.println("- Set: " + key + " -> " + value);
                        content = content.substring(0, pos) + value + content.substring(end + 2);
                        pos = pos + value.length();
                    }
                    if (!test) {
                        try (FileOutputStream os = new FileOutputStream(out)) {
                            if (MFile.writeFile(os, content)) size = content.length();
                        }
                    }
                }
                System.out.println("- " + size + " bytes");
            }
        }
    }

    private boolean substituteFileExtension(File file) {
        String ext = MFile.getFileExtension(file).toLowerCase();
        return MCollection.contains(substituteExtensions, ',', ext);
    }

    private Bundle findBundleForName(String bundleName) {
        for (Bundle bundle : MOsgi.getBundleContext().getBundles()) {
            if (bundle.getSymbolicName().equals(bundleName))
                return bundle;
        }
        return null;
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
            if (sub.endsWith("/")) showList(bundle, sub);
            else System.out.println(bundle.getBundleId() + " " + bundle.getSymbolicName() + "/" + sub.substring(9));
        }
    }
}
