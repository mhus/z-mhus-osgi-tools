/**
 * Copyright 2018 Mike Hummel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.mhus.karaf.commands.testit;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.LinkedList;
import java.util.Random;
import java.util.UUID;

import javax.sql.DataSource;

import org.osgi.framework.ServiceReference;

import de.mhus.lib.core.MCast;
import de.mhus.lib.core.MProperties;
import de.mhus.lib.core.MThread;
import de.mhus.osgi.api.util.DataSourceUtil;

public class SystemShit implements ShitIfc {

	private int cnt;

	private int parallelCnt;

    private static Object lock = new Object();

	@Override
	public void printUsage() {
		System.out.println(
		  " memkill\n"
		+ " stackkill\n"
		+ " stress [seconds=1] [threads=auto] [iterations=0] [sleep=1] [tolerance=5]\n"
		+ " parallel [interval=1] [lifetime=10] [silent=true]"
		);
		
	}

	@Override
	public Object doExecute(CmdShitYo base, String cmd, String[] parameters) throws Exception {
	    if (cmd.equals("uuid"))
	        return UUID.randomUUID().toString();
	    if (cmd.equals("threadid")) {
	        synchronized (lock) {
    	        String threadid = (String)base.session.get("threadid");
    	        if (threadid == null) threadid = "";
    	        threadid = threadid + " " + Thread.currentThread().getId();
    	        base.session.put("threadid", threadid);
    	        System.out.println(threadid);
	        }
	        return null;
	    }
		if (cmd.equals("memkill")) {
			String kill = "killkill";
			int len = kill.length();
			long freeStart = Runtime.getRuntime().freeMemory();
			long free = 0;
			System.gc();
			try {
				while (true) {
					kill = kill + kill;
					len = kill.length();
					free = Runtime.getRuntime().freeMemory();
					System.out.println(len + " " + free );
				}
			} catch (OutOfMemoryError e) {
				kill = "";
				System.out.println("Buffer     : " + MCast.toUnit(len) + " Characters" );
				System.out.println("Memory lost: " + MCast.toByteUnit(freeStart - free) + "B");
				System.gc();
			}
		} else
		if (cmd.equals("stackkill")) {
			cnt = 0;
			try {
				doInfinity();
			} catch (Throwable t) {}
			System.out.println("Depth: " + cnt);
		} else
		if (cmd.equals("stress")) {
			MProperties p = MProperties.explodeToMProperties(parameters);
			int sec  = (int)(p.getDouble("seconds", 1d) * 1000d); // to milliseconds
			int iter = p.getInt("iterations", 0);
			int thr  = p.getInt("threads", -1);
			int sleep = (int)(p.getDouble("sleep", 1d) * 1000d);
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
						System.out.println("CPU Time: " + autoThrAllLast + " -> " + autoThrAll + " = " + (d > 0 ? ( (autoThrAll-autoThrAllLast) / d ) : "0") +"%, Threads: " + autoThr);
					}
					// auto mode
					if (autoIgnoreNext) {
						// ignore
						autoIgnoreNext = false;
					}else
					if (autoThrAllLast == 0 || autoThrAll > (autoThrAllLast * (100+tolerance) / 100) ) {
						if (autoInfo)
							System.out.println("Auto up");
						autoThr++;
					} else
					if ((autoThrAll * (100+tolerance) / 100) < autoThrAllLast) {
						if (autoInfo)
							System.out.println("Auto down");
						autoThr--;
						if (autoThr < 1) autoThr = 1;
						autoIgnoreNext = true;
					}
					thrx = autoThr;
				}
				for (int i = 0; i < thrx; i++)
					list.add(new CalculationThread(sec));
				
				list.forEach(t -> {t.thread = new Thread(t);t.thread.start();});
				
				for (CalculationThread t : list) t.thread.join();
				
				cnt2++;
				System.out.print(cnt2 + ": [" + list.size() + "] " );
				long all = 0;
				for (CalculationThread t : list) {
					t.print();
					all+= t.cpuTimePerSecond;
				}
				// rotate last cpu time counters
				autoThrAllLast = autoThrAll;
				autoThrAll = all;
				
				System.out.println("= " + MCast.toUnit(all) );
				
				if (cnt > 0) {
					cnt--;
					if (cnt <= 0) break;
				}
				
				Thread.sleep(sleep);
				
			}
			
						
		} else
		if (cmd.equals("parallel")) {
			MProperties p = MProperties.explodeToMProperties(parameters);
			int lifetime  = p.getInt("lifetime", 10);
			int interval  = (int)(p.getDouble("interval", 1d) * 1000d); // to milliseconds
			boolean silent = p.getBoolean("silent", true);
			
			parallelCnt = 0;
			int cnt = 0;
			try {
				while (true) {
					new Thread(new ParallelThread(lifetime, silent)).start();
					Thread.sleep(interval);
					cnt++;
					System.out.println(cnt + " Current Threads: " + parallelCnt);
				}
			} catch (InterruptedException e) {
				System.out.println("Stopping ...");
				MThread.sleep(1000); // will not interrupt
				while (parallelCnt > 0) {
					Thread.sleep(1000);
					cnt++;
					System.out.println(cnt + " Stopping ... Threads left: " + parallelCnt);
				}
				
			}

		} else
		if (cmd.equals("datasource")) {
			DataSource ds = new DataSourceUtil().getDataSource(parameters[0]);
			if (ds == null) {
				System.out.println("Data source not found");
			} else {
				System.out.println("Data source: " + ds);
			}
		} else
		if (cmd.equals("datasources")) {
			for (ServiceReference<DataSource> dsRef : new DataSourceUtil().getDataSources()) {
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
			while(lifetime > 0) {
				try {
					Thread.sleep(1000);
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
	public static class CalculationThread implements Runnable
    {
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
