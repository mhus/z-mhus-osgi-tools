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
package de.mhus.osgi.dev.dev.testit;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.api.console.Session;

import de.mhus.lib.errors.NotFoundException;
import de.mhus.osgi.api.karaf.AbstractCmd;
import de.mhus.osgi.api.util.OsgiBundleClassLoader;

@Command(scope = "mhus", name = "shityo", description = "Command to do test some shit")
@Service
public class CmdShitYo extends AbstractCmd {

    @Argument(
            index = 0,
            name = "module",
            required = false,
            description = "help or module class or module shortname",
            multiValued = false)
    String module;

    @Argument(
            index = 1,
            name = "cmd",
            required = false,
            description = "module cmd or help",
            multiValued = false)
    String cmd;

    @Argument(
            index = 2,
            name = "paramteters",
            required = false,
            description = "Parameters",
            multiValued = true)
    String[] parameters;

    @Reference Session session;

    HashMap<String, Class<? extends ShitIfc>> shortcuts = new HashMap<>();

    {
        shortcuts.put("system", SystemShit.class);
        shortcuts.put("stress", StressShit.class);
        shortcuts.put("jaas", JaasShit.class);
        shortcuts.put("address", AddressShit.class);
        shortcuts.put("maven", MavenShit.class);
        shortcuts.put("soffice", SOfficeShit.class);
        shortcuts.put("mhus", MhusShit.class);
        shortcuts.put("crypt", CryptShit.class);
        shortcuts.put("jdbcmeta", JdbcMetaShit.class);
        shortcuts.put("threadlocal", ThreadLocalShit.class);
        shortcuts.put("osgi", OsgiShit.class);
        shortcuts.put("log", LogShit.class);
    }

    @Override
    public Object execute2() throws Exception {

        if (module == null || module.equals("help")) {
            System.out.println("Known module shortcuts:");
            System.out.println(shortcuts.keySet());
            return null;
        }

        ShitIfc mod = getModule(module);

        if (cmd == null || cmd.equals("help")) {
            mod.printUsage();
            return null;
        }

        Object oldIgnoreInterrupts = session.get(Session.IGNORE_INTERRUPTS);
        try {
            session.put(Session.IGNORE_INTERRUPTS, Boolean.TRUE);
            return mod.doExecute(this, cmd, parameters);
        } finally {
            session.put(Session.IGNORE_INTERRUPTS, oldIgnoreInterrupts);
        }
    }

    @SuppressWarnings("unchecked")
    private ShitIfc getModule(String name)
            throws NotFoundException, InstantiationException, IllegalAccessException,
                    IllegalArgumentException, InvocationTargetException, NoSuchMethodException,
                    SecurityException {
        Class<? extends ShitIfc> clazz = shortcuts.get(name);
        if (clazz == null) {
            try {
                clazz = (Class<? extends ShitIfc>) new OsgiBundleClassLoader().loadClass(name);
            } catch (ClassNotFoundException e) {
                System.out.println(e.toString());
            }
        }
        if (clazz == null) throw new NotFoundException("module not found", name);
        return clazz.getDeclaredConstructor().newInstance();
    }

    public Session getSession() {
        return session;
    }

    public String getTblOpt() {
        return tblOpt;
    }
}
