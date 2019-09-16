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

import de.mhus.lib.core.MSystem;

public class KarafShit implements ShitIfc {

    @Override
    public void printUsage() {
        System.out.println("sessionid - print current session id");
    }

    @Override
    public Object doExecute(CmdShitYo base, String cmd, String[] parameters) throws Exception {
        switch (cmd) {
        case "sessionid": {
            System.out.println(MSystem.getObjectId(base.getSession()));
        } break;
        }
        return null;
    }

}
