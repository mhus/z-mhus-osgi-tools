/**
 * Copyright (C) 2018 Mike Hummel (mh@mhus.de)
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
package de.mhus.karaf.commands.mhus;

import java.util.Date;

import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.lib.core.M;
import de.mhus.lib.core.MPeriod;
import de.mhus.lib.core.concurrent.Lock;
import de.mhus.lib.core.console.ConsoleTable;
import de.mhus.lib.core.service.LockManager;
import de.mhus.osgi.api.karaf.AbstractCmd;

@Command(scope = "mhus", name = "lock-list", description = "List all known locks")
@Service
public class CmdLockList extends AbstractCmd {

    @Override
    public Object execute2() throws Exception {

        ConsoleTable out = new ConsoleTable(tblOpt);
        long now = System.currentTimeMillis();
        out.setHeaderValues("Id", "Name", "Locked", "Owner", "Time", "Since", "Managed", "Cnt");
        for (Lock lock : M.l(LockManager.class).managedLocks())
            out.addRowValues(
                    lock.hashCode(),
                    lock.getName(),
                    lock.isLocked(),
                    lock.getOwner(),
                    lock.isLocked() ? new Date(lock.getLockTime()) : "",
                    lock.isLocked() ? MPeriod.getIntervalAsStringSec(now - lock.getLockTime()) : "",
                    "true",
                    lock.getCnt());
        for (Lock lock : M.l(LockManager.class).getRegisteredLocks())
            out.addRowValues(
                    lock.hashCode(),
                    lock.getName(),
                    lock.isLocked(),
                    lock.getOwner(),
                    lock.isLocked() ? new Date(lock.getLockTime()) : "",
                    lock.isLocked() ? MPeriod.getIntervalAsStringSec(now - lock.getLockTime()) : "",
                    "false",
                    lock.getCnt());
        out.print();
        return null;
    }
}
