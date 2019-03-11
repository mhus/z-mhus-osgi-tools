package de.mhus.karaf.commands.testit;

import de.mhus.lib.core.M;
import de.mhus.lib.core.MLog;
import de.mhus.lib.core.MStopWatch;
import de.mhus.lib.core.MThread;

public class ThreadLocalShit extends MLog implements ShitIfc, Runnable {

    private static Thread myThread = null;
    private static boolean close = false;
    private long interval = 10000;
    private ThreadLocal<String> threadLocal = new ThreadLocal<>();
    
    @Override
    public void printUsage() {
        System.out.println("status, start, stop");
    }

    @Override
    public Object doExecute(String cmd, String[] parameters) throws Exception {
        if (cmd.equals("status")) {
            if (myThread == null)
                System.out.println("Stopped");
            else
                System.out.println("Started");
        } else if (cmd.equals("start")) {
            if (myThread == null) {
                close = false;
                myThread = new Thread(this);
                System.out.println("STARTED");
            }
        } else if (cmd.equals("stop")) {
            if (myThread != null) {
                System.out.println("Wait for stop");
                close = true;
            }
        } else if (cmd.equals("interval")) {
            interval = M.c(parameters[0], 10000);
        }
        return null;
    }

    @Override
    public void run() {
        log().i("Start Thread");
        threadLocal.set("Crazy Shit");
        MStopWatch time = new MStopWatch().start();
        while (!close) {
            MThread.sleep(interval);
            log().i("Content",time,threadLocal.get());
        }
        log().i("Exit Thread",time);
        myThread = null; // cleanup before exit
    }

}
