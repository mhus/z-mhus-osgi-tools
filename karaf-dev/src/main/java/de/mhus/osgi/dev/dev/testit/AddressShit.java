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

import java.util.Locale;

import de.mhus.lib.core.IProperties;
import de.mhus.lib.core.MProperties;
import de.mhus.lib.core.util.Address;

public class AddressShit implements ShitIfc {

    @Override
    public void printUsage() {
        System.out.println(
                "reload - reload definitions file\n"
                        + "definition - print definition dump\n"
                        + "parse <string> - parse the string and find the salutation\n"
                        + "tostring <string> [locale] - parse salutation and convert to string\n"
                        + "name [key=value]* - create address and print full name (lacale=)\n"
                        + "letter [key=value]* - create address and letter salutation (lacale=)");
    }

    @Override
    public Object doExecute(CmdShitYo base, String cmd, String[] parameters) throws Exception {
        if (cmd.equals("reload")) {
            Address.reloadDefinition();
            return "OK";
        }
        if (cmd.equals("definition")) {
            System.out.println(Address.getDefinition());
            return null;
        }
        if (cmd.equals("parse")) {
            return Address.toSalutation(parameters[0]).name();
        }
        if (cmd.equals("tostring")) {
            Locale l = parameters.length > 1 ? Locale.forLanguageTag(parameters[1]) : null;
            return Address.toSalutationString(Address.toSalutation(parameters[0]), l);
        }
        if (cmd.equals("name")) {
            MProperties attr = IProperties.explodeToMProperties(parameters);
            Address addr = new Address(attr);
            Locale l =
                    attr.isProperty("locale")
                            ? Locale.forLanguageTag(attr.getString("locale"))
                            : null;
            return addr.getFullName(l);
        }
        if (cmd.equals("letter")) {
            MProperties attr = IProperties.explodeToMProperties(parameters);
            Address addr = new Address(attr);
            Locale l =
                    attr.isProperty("locale")
                            ? Locale.forLanguageTag(attr.getString("locale"))
                            : null;
            return addr.getLetterSalutation(l);
        }
        System.out.println("Command not found");
        return null;
    }
}
