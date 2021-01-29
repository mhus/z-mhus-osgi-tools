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

import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.api.console.Session;

import de.mhus.lib.core.M;
import de.mhus.lib.core.concurrent.Lock;
import de.mhus.lib.core.service.LockManager;
import de.mhus.osgi.api.karaf.AbstractCmd;

@Command(scope = "mhus", name = "lock-unlock", description = "Unlock a locked lock")
@Service
public class CmdLockUnlock extends AbstractCmd {

    @Reference private Session session;

    @Argument(index = 0, name = "id", required = true, description = "Lock id", multiValued = false)
    long id;

    @Override
    public Object execute2() throws Exception {

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

        return null;
    }
}
