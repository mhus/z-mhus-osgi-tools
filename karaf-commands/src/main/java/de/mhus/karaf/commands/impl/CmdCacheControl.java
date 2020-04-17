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
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.ehcache.config.ResourceType;
import org.osgi.service.component.annotations.Reference;

import de.mhus.lib.core.console.ConsoleTable;
import de.mhus.osgi.api.cache.CacheService;
import de.mhus.osgi.api.cache.CloseableCache;
import de.mhus.osgi.api.karaf.AbstractCmd;

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
    CacheService service;
    
    @Override
    public Object execute2() throws Exception {

        if (cmd.equals("list")) {
            ConsoleTable table = new ConsoleTable(tblOpt);
            table.setHeaderValues("Name", "Size");
            
            for (String name : service.getCaches()) {
                CloseableCache<Object, Object> cache = service.getCache(name);
                if (cache != null) {
                    table.addRowValues(name, cache.getRuntimeConfiguration().getResourcePools()
                            .getPoolForResource(ResourceType.Core.HEAP).getSize());
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
