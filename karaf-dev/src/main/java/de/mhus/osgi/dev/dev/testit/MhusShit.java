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

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.karaf.shell.api.console.Session;

import de.mhus.lib.core.M;
import de.mhus.lib.core.MCollection;
import de.mhus.lib.core.MHousekeeper;
import de.mhus.lib.core.MHousekeeperTask;
import de.mhus.lib.core.MLog;
import de.mhus.lib.core.MPeriod;
import de.mhus.lib.core.MSystem;
import de.mhus.lib.core.MThread;
import de.mhus.lib.core.MThreadPool;
import de.mhus.lib.core.concurrent.Lock;
import de.mhus.lib.core.console.Console;
import de.mhus.lib.core.console.ConsoleTable;
import de.mhus.lib.core.logging.ITracer;
import de.mhus.lib.core.mapi.DefaultHousekeeper;
import de.mhus.lib.core.service.LockManager;
import de.mhus.lib.core.util.AtomicClockUtil;
import de.mhus.lib.core.util.ValueProvider;
import de.mhus.osgi.api.karaf.CmdInterceptor;
import de.mhus.osgi.api.karaf.CmdInterceptorUtil;
import io.opentracing.References;
import io.opentracing.Scope;
import io.opentracing.SpanContext;
import io.opentracing.Tracer.SpanBuilder;

public class MhusShit extends MLog implements ShitIfc {

    private long doitTime;

    @Override
    public void printUsage() {
        System.out.println("housekeepertest");
        System.out.println("housekeepertasks");
        System.out.println("locks - print known locks from LockManager");
        System.out.println("lock <id> - print details");
        System.out.println("releaselock <id>");
        System.out.println("createlock <name> - create managed lock");
        System.out.println("setlock <id>");
        System.out.println(
                "atomictime [<server>] - without server will use getCurrentTime() and return a cached time");
        System.out.println("atomicservers");
        System.out.println("ask - try console input");
        System.out.println("interceptors - print all session interceptors");
        System.out.println("tracetest");
    }

    @Override
    public Object doExecute(CmdShitYo base, String cmd, String[] parameters) throws Exception {

        if (cmd.equals("tracetest")) {
            ITracer tracer = ITracer.get();
            try (Scope t1 = tracer.enter("test1", "a", "b")) {
                log().f("FATAL Log Entry");
                log().e("ERROR Log Entry");
                log().w("WARN Log Entry");
                log().i("INFO Log Entry");
                log().d("DEBUG Log Entry");
                log().t("TRACE Log Entry");
                log().i("Sleep");
                MThread.sleep(1000);
                log().i("Awaken");
                for (int i = 0; i < 5; i++)
                    try (Scope t2 = tracer.enter("test2", "loop", "" + i)) {
                        log().i("Sleep");
                        MThread.sleep(1000);
                        log().i("Awaken");
                        MThread.sleep(1000);
                    }
                MThread.sleep(1000);
                log().i("End");
            }
        }
        if (cmd.equals("tracetest2")) {
            ITracer tracer = ITracer.get();
            SpanContext ctx = null;
            try (Scope t1 = tracer.enter("test1", "a", "b")) {
                log().f("FATAL Log Entry");
                log().e("ERROR Log Entry");
                log().w("WARN Log Entry");
                log().i("INFO Log Entry");
                log().d("DEBUG Log Entry");
                log().t("TRACE Log Entry");
                log().i("Sleep");
                MThread.sleep(1000);
                log().i("Awaken");
                ctx = t1.span().context();
                t1.span().finish();
                log().i("Finished 1");
            }

            for (int i = 0; i < 1; i++) {
                final Scope t2 =
                        tracer.tracer()
                                .buildSpan("test2")
                                .addReference(References.CHILD_OF, ctx)
                                .withTag("loop", "" + i)
                                .startActive(true);
                // final Scope t2 = tracer.start("test2", "yes", "loop", ""+i);
                try (t2) {
                    log().i("Sleep");
                    MThread.sleep(1000);
                    log().i("Awaken");
                    MThread.sleep(1000);
                }
            }
            MThread.sleep(1000);
            log().i("End");
        }
        if (cmd.equals("tracetest3")) {
            ITracer tracer = ITracer.get();
            SpanContext ctx = null;
            SpanBuilder s1 = tracer.createSpan(null, "test1", "a", "b");
            try (Scope t1 = s1.startActive(false)) {
                log().f("FATAL Log Entry");
                log().e("ERROR Log Entry");
                log().w("WARN Log Entry");
                log().i("INFO Log Entry");
                log().d("DEBUG Log Entry");
                log().t("TRACE Log Entry");
                log().i("Sleep");
                MThread.sleep(1000);
                log().i("Awaken");
                ctx = t1.span().context();
                log().i("Finished 1");
            }
            for (int i = 0; i < 2; i++) {
                final Scope t2 =
                        tracer.tracer()
                                .buildSpan("test2")
                                .addReference(References.CHILD_OF, ctx)
                                .withTag("loop", "" + i)
                                .startActive(true);
                // final Scope t2 = tracer.start("test2", "yes", "loop", ""+i);
                try (t2) {
                    System.gc();
                    log().i("Sleep");
                    MThread.sleep(1000);
                    log().i("Awaken");
                    MThread.sleep(1000);
                }
            }
            MThread.sleep(1000);
            log().i("End");
        }
        if (cmd.equals("interceptors")) {
            Session session = base.getSession();
            @SuppressWarnings("unchecked")
            List<CmdInterceptor> interceptors =
                    (List<CmdInterceptor>) session.get(CmdInterceptorUtil.SESSION_KEY);
            for (CmdInterceptor inter : interceptors)
                System.out.println(inter.getClass().getCanonicalName() + " = " + inter);
        } else if (cmd.equals("ask")) {
            char result =
                    Console.askQuestion("Really do something?", new char[] {'y', 'N'}, true, false);
            System.out.println("Result: " + result);
        } else if (cmd.equals("atomicservers")) {
            AtomicClockUtil.TIME_SERVERS.forEach(s -> System.out.println(s));
        } else if (cmd.equals("atomictime")) {
            long time =
                    MCollection.isEmpty(parameters)
                            ? AtomicClockUtil.getCurrentTime()
                            : AtomicClockUtil.getAtomicTime(parameters[0]);
            System.out.println("Time: " + time);
            System.out.println("Date: " + new Date(time));
        } else if (cmd.equals("createlock")) {
            Lock lock = M.l(LockManager.class).getLock(parameters[0]);
            System.out.println("Created " + lock.hashCode());
        } else if (cmd.equals("setlock")) {
            int id = M.to(parameters[0], 0);
            for (Lock lock : M.l(LockManager.class).getRegisteredLocks())
                if (id == lock.hashCode()) {
                    System.out.println("Set " + id);
                    lock.lock();
                    return null;
                }
            for (Lock lock : M.l(LockManager.class).managedLocks())
                if (id == lock.hashCode()) {
                    System.out.println("Set " + id);
                    lock.lock();
                    return null;
                }
            System.out.println("Not found");
        } else if (cmd.equals("releaselock")) {
            int id = M.to(parameters[0], 0);
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
        } else if (cmd.equals("lock")) {
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
        } else if (cmd.equals("locks")) {
            ConsoleTable out = new ConsoleTable(base.getTblOpt());
            long now = System.currentTimeMillis();
            out.setHeaderValues("Id", "Name", "Locked", "Owner", "Time", "Since", "Managed", "Cnt");
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
        } else if (cmd.equals("housekeepertasks")) {
            Map<MHousekeeperTask, Long> map = DefaultHousekeeper.getAll();
            ConsoleTable table = new ConsoleTable(base.getTblOpt());
            table.setHeaderValues("Name", "Class", "Sleep");
            for (Entry<MHousekeeperTask, Long> entry : map.entrySet())
                table.addRowValues(
                        entry.getKey().getName(),
                        MSystem.getCanonicalClassName(entry.getKey().getClass()),
                        entry.getValue());
            table.print(System.out);
        } else if (cmd.equals("housekeepertest")) {
            MHousekeeper housekeeper = M.l(MHousekeeper.class);
            doitTime = 0;
            MHousekeeperTask task =
                    new MHousekeeperTask() {

                        @Override
                        protected void doit() throws Exception {
                            System.out.println("--- doit");
                            doitTime = System.currentTimeMillis();
                        }
                    };
            housekeeper.register(task, 1000);

            String res =
                    MThreadPool.getWithTimeout(
                            new ValueProvider<String>() {

                                @Override
                                public String getValue() throws Exception {
                                    System.out.println("--- check");
                                    if (doitTime == 0) return null;
                                    return "ok";
                                }
                            },
                            30000,
                            false);
            System.out.println(res + " " + doitTime);
        }
        return null;
    }
}
