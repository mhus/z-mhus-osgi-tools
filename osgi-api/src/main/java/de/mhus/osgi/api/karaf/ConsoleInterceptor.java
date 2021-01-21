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
package de.mhus.osgi.api.karaf;

import org.apache.karaf.shell.api.console.Session;

import de.mhus.lib.core.console.Console;

public class ConsoleInterceptor implements CmdInterceptor {

    private Console console;
    private Console old;

    public ConsoleInterceptor(Console console) {
        this.console = console;
    }

    @Override
    public void onCmdStart(Session session) {
        old = Console.isInitialized() ? Console.get() : null;
        Console.set(console);
    }

    @Override
    public void onCmdEnd(Session session) {
        Console.set(old);
    }

    public Console get() {
        return console;
    }
}
