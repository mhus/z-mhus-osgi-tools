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
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.osgi.api.karaf.AbstractCmd;

@Command(scope = "mhus", name = "log-log", description = "Log message")
@Service
public class CmdLogLog extends AbstractCmd {

    @Argument(
            index = 0,
            name = "level",
            required = true,
            description = "trace,debug,info,warn,error,fatal",
            multiValued = false)
    String level;

    @Argument(
            index = 1,
            name = "message",
            required = true,
            description = "Message to log",
            multiValued = false)
    String msg;
    
    @Argument(
            index = 2,
            name = "param",
            required = false,
            description = "Parameters to log",
            multiValued = true)
    Object[] param;

    @Override
    public Object execute2() throws Exception {

        switch (level.toLowerCase()) {
            case "trace":
            case "t":
                {
                    log().t(msg, param);
                }
                break;
            case "debug":
            case "d":
                {
                    log().d(msg, param);
                }
                break;
            case "info":
            case "i":
                {
                    log().i(msg, param);
                }
                break;
            case "warn":
            case "w":
                {
                    log().w(msg, param);
                }
                break;
            case "error":
            case "e":
                {
                    log().e(msg, param);
                }
                break;
            case "fatal":
            case "f":
                {
                    log().f(msg, param);
                }
                break;
            default:
                System.out.println("Unknown level");
        }

        return null;
    }
}
