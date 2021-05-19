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

import java.io.File;
import java.io.FileOutputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.LinkedList;
import java.util.Random;
import java.util.UUID;

import javax.sql.DataSource;

import org.osgi.framework.ServiceReference;

import de.mhus.lib.core.IProperties;
import de.mhus.lib.core.MCast;
import de.mhus.lib.core.MPeriod;
import de.mhus.lib.core.MProperties;
import de.mhus.lib.core.MThread;
import de.mhus.lib.core.crypt.MBouncy;
import de.mhus.lib.errors.RuntimeInterruptedException;
import de.mhus.osgi.api.util.DataSourceUtil;

public class StressShit implements ShitIfc {

    private int cnt;

    private int parallelCnt;

    private static Object lock = new Object();

    @Override
    public void printUsage() {
        System.out.println(
                " memkill [permanent=false]\n"
                        + " stackkill\n"
                        + " stress [seconds=1] [threads=auto] [iterations=0] [sleep=1] [tolerance=5]\n"
                        + " parallel [interval=1] [lifetime=10] [silent=true]\n"
                        + " oome - throw OutOfMemoryError\n"
                        + " bigfile [path]\n"
                        + " ctrl-c [iterations=5] [scenario=0: 0-6]"
                        + " threads [next=100: wait until next create] [sleep=10000: sleep inside the thread]");
    }

    long current = 0;
    long sum = 0;

    @Override
    public Object doExecute(CmdShitYo base, String cmd, String[] parameters) throws Exception {
        if (cmd.equals("threads")) {
            MProperties p = IProperties.explodeToMProperties(parameters);
            final long next = p.getLong("next", 100);
            final long sleep = p.getLong("sleep", 10000);
            long nextOutput = System.currentTimeMillis();
            current = 0;
            sum = 0;
            while (true) {
                new Thread(
                                new Runnable() {

                                    @Override
                                    public void run() {
                                        sum++;
                                        current++;
                                        MThread.sleepForSure(sleep);
                                        current--;
                                    }
                                })
                        .start();
                MThread.sleepInLoop(next);
                if (System.currentTimeMillis() > nextOutput) {
                    System.out.println("Current: " + current + " Sum: " + sum);
                    nextOutput = System.currentTimeMillis() + 5000;
                }
            }
        }
        if (cmd.equals("ctrl-c")) {
            MProperties p = IProperties.explodeToMProperties(parameters);
            int cnt = p.getInt("iterations", 3);
            long sleep = p.getLong("sleep", 3000);
            int scenario = p.getInt("scenario", 0);
            try {
                System.out.println("--- Thread ID " + Thread.currentThread().getId());
                while (true) {
                    System.out.println(">>> Loop " + cnt);
                    if (scenario == 0 || scenario == 1) {
                        System.out.println("1: Wait for Ctrl-C - MThread.sleepForSure");
                        if (MThread.sleepForSure(sleep)) {
                            if (Thread.interrupted()) {
                                System.out.println("Thread was interrupted - wrong state 1");
                                break;
                            }
                            System.out.println("Thread was interrupted");
                            break;
                        }
                        if (Thread.interrupted()) {
                            System.out.println("Thread was interrupted - wrong state 2");
                            break;
                        }
                    }
                    if (scenario == 0 || scenario == 2) {
                        System.out.println("2: Wait for Ctrl-C - MThread.sleep");
                        MThread.sleep(sleep);
                    }
                    if (scenario == 0 || scenario == 3) {
                        System.out.println("3: Wait for Ctrl-C - Thread.sleep");
                        Thread.sleep(sleep);
                    }
                    if (scenario == 0 || scenario == 4) {
                        System.out.println("4: Wait for Ctrl-C - generateRsaKey");
                        long start = System.currentTimeMillis();
                        while (!MPeriod.isTimeOut(start, sleep)) {
                            MBouncy.generateRsaKey(MBouncy.RSA_KEY_SIZE.B2048);
                        }
                        if (Thread.interrupted()) {
                            System.out.println("Thread was interrupted");
                            break;
                        }
                    }
                    if (scenario == 5) {
                        System.out.println(
                                "5: Wait for Ctrl-C - generateRsaKey and MThread.sleepInLoop");
                        long start = System.currentTimeMillis();
                        while (!MPeriod.isTimeOut(start, sleep)) {
                            MBouncy.generateRsaKey(MBouncy.RSA_KEY_SIZE.B2048);
                        }
                        System.out.println("Finish loop");
                        if (Thread.interrupted()) {
                            System.out.println("Thread was interrupted");
                            throw new InterruptedException();
                        }
                        Thread.sleep(100);
                        // MThread.sleepInLoop(100); // only to test exception - not for waiting
                    }
                    if (scenario == 6) {
                        System.out.println("6: Wait for Ctrl-C - generateRsaKey and Thread.sleep");
                        long start = System.currentTimeMillis();
                        while (!MPeriod.isTimeOut(start, sleep)) {
                            MBouncy.generateRsaKey(MBouncy.RSA_KEY_SIZE.B2048);
                        }
                        System.out.println("Finish loop");
                        Thread.sleep(100); // only to test exception - not for waiting
                    }

                    cnt--;
                    if (cnt <= 0) {
                        System.out.println("<<< Loop ends");
                        return null;
                    }
                }
            } catch (InterruptedException | RuntimeInterruptedException e) {
                System.out.println("<<< Interrupted !!!!");
                e.printStackTrace();
            } catch (Throwable t) {
                t.printStackTrace();
            }
            System.out.println("<<< End");
        }
        if (cmd.equals("bigfile")) {
            File f = new File(parameters[0]);
            FileOutputStream fos = new FileOutputStream(f);
            long size = 0;
            long round = 0;
            byte[] buffer = new byte[1024 * 10];
            try {
                while (true) {
                    fos.write(buffer);
                    fos.flush();
                    size += buffer.length;
                    round++;
                    if (round % 100 == 0) System.out.println("Size " + MCast.toByteUnit(size));
                    MThread.checkInterruptedException();
                }
            } finally {
                fos.close();
                System.out.println("Final Size " + MCast.toByteUnit(f.length()));
                f.delete();
            }
        }
        if (cmd.equals("oome")) {
            throw new OutOfMemoryError("test");
        }
        if (cmd.equals("uuid")) return UUID.randomUUID().toString();
        if (cmd.equals("threadid")) {
            synchronized (lock) {
                String threadid = (String) base.session.get("threadid");
                if (threadid == null) threadid = "";
                threadid = threadid + " " + Thread.currentThread().getId();
                base.session.put("threadid", threadid);
                System.out.println(threadid);
            }
            return null;
        }
        if (cmd.equals("memkill")) {
            MProperties p = IProperties.explodeToMProperties(parameters);
            String kill = "killkill";
            String small = null;
            int len = kill.length();
            long freeStart = Runtime.getRuntime().freeMemory();
            long free = 0;
            System.gc();
            try {
                while (true) {
                    try {
                        while (true) {
                            MThread.checkInterruptedException();
                            if (small != null) {
                                small = small + small;
                                kill = kill + small;
                            } else kill = kill + kill;
                            len = kill.length();
                            free = Runtime.getRuntime().freeMemory();
                            System.out.println(len + " " + free);
                            MThread.checkInterruptedException();
                        }
                    } catch (OutOfMemoryError e) {
                        free = Runtime.getRuntime().freeMemory();
                        System.out.println(
                                "Buffer     : " + MCast.toUnit(len) + " Characters (" + len + ")");
                        System.out.println(
                                "Memory lost: " + MCast.toByteUnit(freeStart - free) + "B");
                        MThread.checkInterruptedException();
                    }
                    if (!p.getBoolean("permanent", false)) break;
                    MThread.sleepInLoop(500);
                    small = "a";
                }
            } catch (InterruptedException e) {
            }
            small = "";
            kill = "";
            System.gc();
            System.out.println("Exit memkill, free memory");

        } else if (cmd.equals("stackkill")) {
            cnt = 0;
            try {
                doInfinity();
            } catch (Throwable t) {
                System.out.println(t.getMessage());
            }
            System.out.println("Depth: " + cnt);
        } else if (cmd.equals("stress")) {
            MProperties p = IProperties.explodeToMProperties(parameters);
            int sec = (int) (p.getDouble("seconds", 1d) * 1000d); // to milliseconds
            int iter = p.getInt("iterations", 0);
            int thr = p.getInt("threads", -1);
            int sleep = (int) (p.getDouble("sleep", 1d) * 1000d);
            long tolerance = p.getInt("tolerance", 5);
            boolean autoInfo = p.getBoolean("autoInfo", false);

            System.out.println("Used cpu nanoseconds per second ...");
            int cnt = iter;
            int cnt2 = 0;
            int autoThr = 1;
            long autoThrAll = 0;
            long autoThrAllLast = 0;
            boolean autoIgnoreNext = false;
            while (true) {

                LinkedList<CalculationThread> list = new LinkedList<>();
                int thrx = thr;
                if (thr == -1) {
                    if (autoInfo) {
                        long d = autoThrAll / 100;
                        System.out.println(
                                "CPU Time: "
                                        + autoThrAllLast
                                        + " -> "
                                        + autoThrAll
                                        + " = "
                                        + (d > 0 ? ((autoThrAll - autoThrAllLast) / d) : "0")
                                        + "%, Threads: "
                                        + autoThr);
                    }
                    // auto mode
                    if (autoIgnoreNext) {
                        // ignore
                        autoIgnoreNext = false;
                    } else if (autoThrAllLast == 0
                            || autoThrAll > (autoThrAllLast * (100 + tolerance) / 100)) {
                        if (autoInfo) System.out.println("Auto up");
                        autoThr++;
                    } else if ((autoThrAll * (100 + tolerance) / 100) < autoThrAllLast) {
                        if (autoInfo) System.out.println("Auto down");
                        autoThr--;
                        if (autoThr < 1) autoThr = 1;
                        autoIgnoreNext = true;
                    }
                    thrx = autoThr;
                }
                for (int i = 0; i < thrx; i++) list.add(new CalculationThread(sec));

                list.forEach(
                        t -> {
                            t.thread = new Thread(t);
                            t.thread.start();
                        });

                for (CalculationThread t : list) t.thread.join();

                cnt2++;
                System.out.print(cnt2 + ": [" + list.size() + "] ");
                long all = 0;
                for (CalculationThread t : list) {
                    t.print();
                    all += t.cpuTimePerSecond;
                }
                // rotate last cpu time counters
                autoThrAllLast = autoThrAll;
                autoThrAll = all;

                System.out.println("= " + MCast.toUnit(all));

                if (cnt > 0) {
                    cnt--;
                    if (cnt <= 0) break;
                }
                MThread.sleepInLoop(sleep);
            }

        } else if (cmd.equals("parallel")) {
            MProperties p = IProperties.explodeToMProperties(parameters);
            int lifetime = p.getInt("lifetime", 10);
            int interval = (int) (p.getDouble("interval", 1d) * 1000d); // to milliseconds
            boolean silent = p.getBoolean("silent", true);

            parallelCnt = 0;
            int cnt = 0;
            try {
                while (true) {
                    new Thread(new ParallelThread(lifetime, silent)).start();
                    MThread.sleepInLoop(interval);
                    cnt++;
                    System.out.println(cnt + " Current Threads: " + parallelCnt);
                }
            } catch (InterruptedException e) {
                System.out.println("Stopping ...");
                MThread.sleepForSure(1000); // will not interrupt
                while (parallelCnt > 0) {
                    Thread.sleep(1000);
                    cnt++;
                    System.out.println(cnt + " Stopping ... Threads left: " + parallelCnt);
                }
            }

        } else if (cmd.equals("datasource")) {
            DataSource ds = DataSourceUtil.getDataSource(parameters[0]);
            if (ds == null) {
                System.out.println("Data source not found");
            } else {
                System.out.println("Data source: " + ds);
            }
        } else if (cmd.equals("datasources")) {
            for (ServiceReference<DataSource> dsRef : DataSourceUtil.getDataSources()) {
                System.out.println(dsRef);
                for (String key : dsRef.getPropertyKeys())
                    System.out.println(" " + key + "=" + dsRef.getProperty(key));
            }
        }

        return null;
    }

    private void doInfinity() {
        cnt++;
        doInfinity();
    }

    public class ParallelThread implements Runnable {

        private int lifetime;
        private boolean silent;

        public ParallelThread(int lifetime, boolean silent) {
            this.lifetime = lifetime;
            this.silent = silent;
        }

        @Override
        public void run() {
            if (!silent) System.out.println(">>> " + Thread.currentThread().getId());
            parallelCnt++;
            while (lifetime > 0) {
                try {
                    MThread.sleepInLoop(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                lifetime--;
                if (!silent) System.out.println("--- " + Thread.currentThread().getId());
            }
            parallelCnt--;
            if (!silent) System.out.println("<<< " + Thread.currentThread().getId());
        }
    }

    public static class CalculationThread implements Runnable {
        public Thread thread;
        private final Random rng;
        private long sec;
        private long deltaCpuTime;

        @SuppressWarnings("unused")
        private long rounds = 0;

        private long cpuTimePerSecond;

        public CalculationThread(long sec) {
            this.rng = new Random();
            this.sec = sec;
        }

        public void print() {
            cpuTimePerSecond = deltaCpuTime / (sec / 1000);
            System.out.print(MCast.toUnit(cpuTimePerSecond) + " ");
        }

        @Override
        public void run() {
            rounds = 0;
            @SuppressWarnings("unused")
            double store = 1;
            long threadId = Thread.currentThread().getId();
            ThreadMXBean tmxb = ManagementFactory.getThreadMXBean();
            long startCpuTime = tmxb.getThreadCpuTime(threadId);
            long start = System.currentTimeMillis();
            while (System.currentTimeMillis() - start <= sec) {
                double r = this.rng.nextFloat();
                double v = Math.sin(Math.cos(Math.sin(Math.cos(r))));
                store *= v;
                rounds++;
            }
            deltaCpuTime = tmxb.getThreadCpuTime(threadId) - startCpuTime;
        }
    }
}
