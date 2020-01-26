/**
 * Copyright 2018 Mike Hummel
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.mhus.karaf.commands.mhus;

import java.util.Date;

import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.api.console.Session;

import de.mhus.lib.core.M;
import de.mhus.lib.core.MPeriod;
import de.mhus.lib.core.base.service.LockManager;
import de.mhus.lib.core.concurrent.Lock;
import de.mhus.lib.core.console.ConsoleTable;
import de.mhus.osgi.api.karaf.AbstractCmd;

@Command(scope = "mhus", name = "lock", description = "Locks management")
@Service
public class CmdLock extends AbstractCmd {

    @Reference private Session session;

    @Argument(
            index = 0,
            name = "cmd",
            required = true,
            description = "Command:\n list",
            multiValued = false)
    String cmd;

    @Argument(
            index = 1,
            name = "paramteters",
            required = false,
            description = "Parameters",
            multiValued = true)
    String[] parameters;

    // private Appender appender;

    @Override
    public Object execute2() throws Exception {

        switch (cmd) {
            case "get":
                {
                    long id = Long.parseLong(parameters[0]);
                    for (Lock lock : M.l(LockManager.class).managedLocks()) {
                        if (lock.hashCode() == id) {
                            System.out.println("Lock: " + lock);
                            System.out.println("Name: " + lock.getName());
                            System.out.println("Locked: " + lock.isLocked());
                            System.out.println("Owner: " + lock.getOwner());
                            System.out.println("Time: " + lock.getLockTime());
                            System.out.println("Start StackTrace:");
                            System.out.println(lock.getStartStackTrace());
                        }
                    }
                    return null;
                }
            case "list":
                {
                    ConsoleTable out = new ConsoleTable(tblOpt);
                    long now = System.currentTimeMillis();
                    out.setHeaderValues(
                            "Id", "Name", "Locked", "Owner", "Time", "Since", "Managed", "Cnt");
                    for (Lock lock : M.l(LockManager.class).managedLocks())
                        out.addRowValues(
                                lock.hashCode(),
                                lock.getName(),
                                lock.isLocked(),
                                lock.getOwner(),
                                lock.isLocked() ? new Date(lock.getLockTime()) : "",
                                lock.isLocked()
                                        ? MPeriod.getIntervalAsStringSec(now - lock.getLockTime())
                                        : "",
                                "true",
                                lock.getCnt());
                    for (Lock lock : M.l(LockManager.class).getRegisteredLocks())
                        out.addRowValues(
                                lock.hashCode(),
                                lock.getName(),
                                lock.isLocked(),
                                lock.getOwner(),
                                lock.isLocked() ? new Date(lock.getLockTime()) : "",
                                lock.isLocked()
                                        ? MPeriod.getIntervalAsStringSec(now - lock.getLockTime())
                                        : "",
                                "false",
                                lock.getCnt());
                    out.print();
                    return null;
                }
            case "unlock":
                {
                    int id = M.c(parameters[0], 0);
                    for (Lock lock : M.l(LockManager.class).getRegisteredLocks())
                        if (id == lock.hashCode()) {
                            System.out.println("Unlock " + id);
                            lock.unlockHard();
                            return null;
                        }
                    for (Lock lock : M.l(LockManager.class).managedLocks())
                        if (id == lock.hashCode()) {
                            System.out.println("Unlock " + id);
                            lock.unlockHard();
                            return null;
                        }
                    System.out.println("Not found");
                }
            default:
                System.out.println("Command unknown");
        }

        return null;
    }
}
