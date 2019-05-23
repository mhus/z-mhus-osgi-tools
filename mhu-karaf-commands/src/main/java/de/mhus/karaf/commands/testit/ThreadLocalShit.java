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

import de.mhus.lib.core.M;
import de.mhus.lib.core.MApi;
import de.mhus.lib.core.MLog;
import de.mhus.lib.core.MStopWatch;
import de.mhus.lib.core.MThread;
import de.mhus.lib.core.logging.LevelMapper;
import de.mhus.lib.core.logging.TrailLevelMapper;

public class ThreadLocalShit extends MLog implements ShitIfc, Runnable {

    private static Thread myThread = null;
    private static boolean close = false;
    private static long interval = 10000;
    private static ThreadLocal<String> threadLocal = new ThreadLocal<>();
    private static String msg = "Crazy Shit";
    
    @Override
    public void printUsage() {
        System.out.println("status, start, stop, interval <msec>, msg <string>");
    }

    @Override
    public Object doExecute(CmdShitYo base, String cmd, String[] parameters) throws Exception {
        if (cmd.equals("status")) {
            if (myThread == null)
                System.out.println("Stopped");
            else
                System.out.println("Started");
        } else if (cmd.equals("start")) {
            if (myThread == null) {
                close = false;
                myThread = new Thread(this);
                myThread.start();
                System.out.println("STARTED");
            }
        } else if (cmd.equals("stop")) {
            if (myThread != null) {
                System.out.println("Wait for stop");
                close = true;
            }
        } else if (cmd.equals("interval")) {
            interval = M.c(parameters[0], 10000);
            System.out.println("Interval: " + interval);
        } else if (cmd.equals("msg")) {
            msg = parameters[0];
            System.out.println("Msg: " + msg);
        }
        return null;
    }

    @Override
    public void run() {
        log().i("Start Thread");
        threadLocal.set(msg);
        {
            LevelMapper lm = MApi.get().getLogFactory().getLevelMapper();
            if (lm != null && lm instanceof TrailLevelMapper) {
                log().i("Set trail level");
                ((TrailLevelMapper)lm).doConfigureTrail(null,"MAP,T,D,I,W,E,F,G,0,TEST");
            }
        }
        MStopWatch time = new MStopWatch().start();
        while (!close) {
            MThread.sleep(interval);
            String trail = null;
            LevelMapper lm = MApi.get().getLogFactory().getLevelMapper();
            if (lm != null && lm instanceof TrailLevelMapper) {
                trail = ((TrailLevelMapper)lm).getTrailId();
            }
            log().i("Content",time,threadLocal.get(), trail);
        }
        log().i("Exit Thread",time);
        myThread = null; // cleanup before exit
    }

}
