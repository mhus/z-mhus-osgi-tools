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

import de.mhus.lib.core.MString;
import de.mhus.lib.core.io.SOfficeConnector;
import de.mhus.lib.core.parser.StringPropertyReplacerMap;

public class SOfficeShit implements ShitIfc {

    @Override
    public void printUsage() {
        System.out.println("version");
        System.out.println("convert <file> <format> [<output directory>]");
        System.out.println("replace <from> <to> [key=value]*");
        System.out.println("content <from>");
    }

    @Override
    public Object doExecute(CmdShitYo base, String cmd, String[] parameters) throws Exception {
        if (cmd.equals("version")) {
            SOfficeConnector tool = new SOfficeConnector();
            System.out.println("Binary : " + tool.getBinary());
            System.out.println("Valid  : " + tool.isValid());
            System.out.println("Version: " + tool.getVersion());
            return null;
        }
        if (cmd.equals("convert")) {
            SOfficeConnector tool = new SOfficeConnector();
            String res =
                    tool.convertTo(
                            parameters[1],
                            parameters[0],
                            parameters.length > 2 ? parameters[2] : null);
            return res;
        }
        if (cmd.equals("pdf")) {
            SOfficeConnector tool = new SOfficeConnector();
            String res =
                    tool.convertToPdf(parameters[0], parameters.length > 1 ? parameters[1] : null);
            return res;
        }
        if (cmd.equals("replace")) {
            StringPropertyReplacerMap replacer = new StringPropertyReplacerMap();
            for (int i = 2; i < parameters.length; i++) {
                String key = MString.beforeIndex(parameters[i], '=');
                String value = MString.afterIndex(parameters[i], '=');
                replacer.put(key, value);
            }

            SOfficeConnector.replace(new File(parameters[0]), new File(parameters[1]), replacer);
        }
        if (cmd.equals("content")) {
            System.out.println(SOfficeConnector.content(new File(parameters[0])));
        }
        return null;
    }
}
