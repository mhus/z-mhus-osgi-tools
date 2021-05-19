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

import java.util.Arrays;
import java.util.TreeMap;

import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

public class PrintEventHandler implements EventHandler {

    @Override
    public void handleEvent(Event event) {

        if (OsgiShit.blacklist != null)
            for (String black : OsgiShit.blacklist) if (event.getTopic().startsWith(black)) return;

        TreeMap<String, Object> m = new TreeMap<>();
        for (String name : event.getPropertyNames()) {
            Object v = event.getProperty(name);
            if (v == null) // paranoia
            v = "null";
            if (v.getClass().isArray()) v = Arrays.deepToString((Object[]) v);
            m.put(name, v);
        }
        System.out.println("EVENT: " + event.getTopic() + " " + m);
    }
}
