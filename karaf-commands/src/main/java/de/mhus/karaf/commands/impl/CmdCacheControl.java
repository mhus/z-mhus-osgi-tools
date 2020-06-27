/**
 * Copyright 2018 Mike Hummel
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.mhus.karaf.commands.impl;

import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.ehcache.core.statistics.CacheStatistics;

import de.mhus.lib.core.console.ConsoleTable;
import de.mhus.osgi.api.cache.LocalCache;
import de.mhus.osgi.api.cache.LocalCacheService;
import de.mhus.osgi.api.karaf.AbstractCmd;
import de.mhus.osgi.services.cache.LocalCacheWrapper;

@Command(scope = "mhus", name = "cache", description = "Cache Control Service Control")
@Service
public class CmdCacheControl extends AbstractCmd {

    @Argument(
            index = 0,
            name = "cmd",
            required = true,
            description = "list,clear <name>",
            multiValued = false)
    String cmd;

    @Argument(
            index = 1,
            name = "paramteters",
            required = false,
            description = "Parameters",
            multiValued = true)
    String[] parameters;

    @Reference
    LocalCacheService service;
    
    @Override
    public Object execute2() throws Exception {

        if (service == null) {
            System.out.println("CacheService not found, exiting");
            return null;
        }
        if (cmd.equals("list")) {
            ConsoleTable table = new ConsoleTable(tblOpt);
            table.setHeaderValues("Name", "Size", "Bytes");
            
            for (String name : service.getCacheNames()) {
                LocalCache<Object, Object> cache = service.getCache(name);
                if (cache != null) {
                    CacheStatistics cacheStatistics = ((LocalCacheWrapper<?,?>)cache).getCacheStatistics();
                    table.addRowValues(
                            name, 
                            cacheStatistics.getTierStatistics().get("OnHeap").getMappings(),
                            cacheStatistics.getTierStatistics().get("OnHeap").getOccupiedByteSize()
                            );
                }
            }
            table.print();
        } else if (cmd.equals("clear")) {
            String name = parameters[0];
            service.getCache(name).clear();
            System.out.println("OK");

        }
        
        return null;
    }
}
