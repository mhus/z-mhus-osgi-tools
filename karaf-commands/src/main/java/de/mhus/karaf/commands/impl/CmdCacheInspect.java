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
package de.mhus.karaf.commands.impl;

import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.lib.core.cache.ICache;
import de.mhus.lib.core.cache.ICacheService;
import de.mhus.osgi.api.karaf.AbstractCmd;

@Command(scope = "mhus", name = "cache-inspect", description = "Cache Control Service - Print all entries of a cache")
@Service
public class CmdCacheInspect extends AbstractCmd {

    @Argument(
            index = 0,
            name = "name",
            required = true,
            description = "Cache name",
            multiValued = false)
    String name;

    @Reference ICacheService service;

    @Override
    public Object execute2() throws Exception {

        if (service == null) {
            System.out.println("CacheService not found, exiting");
            return null;
        }

        ICache<Object, Object> cache = service.getCache(name);
        if (cache == null) {
            System.out.println("Cache not found");
            return null;
        }
        cache.forEach(e -> System.out.println("[" + e.getKey() + "] -> [" + e.getValue() + "]"));

        return null;
    }
}
