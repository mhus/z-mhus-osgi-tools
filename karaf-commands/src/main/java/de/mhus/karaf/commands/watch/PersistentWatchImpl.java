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
package de.mhus.karaf.commands.watch;

import java.io.IOException;
import java.util.Dictionary;
import java.util.LinkedList;
import java.util.List;

import org.apache.karaf.bundle.core.BundleWatcher;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;

import de.mhus.lib.core.MCollection;
import de.mhus.lib.core.MLog;
import de.mhus.lib.errors.NotFoundException;
import de.mhus.osgi.api.services.MOsgi;
import de.mhus.osgi.api.services.PersistentWatch;

@Component(
        immediate = true,
        name = PersistentWatchImpl.PID,
        service = PersistentWatch.class)
public class PersistentWatchImpl extends MLog implements PersistentWatch {

    public static final String PID = "de.mhus.osgi.commands.watch.PersistentWatch";
    private static final String CONFIG_LIST = "watch";
 
    @Reference
    private BundleWatcher bundleWatcher;

    @Activate
    public void doActivate(ComponentContext ctx) {
        doConfigure();
    }

    @Modified
    public void modified(ComponentContext ctx) {
        doConfigure();
    }

    private void doConfigure() {
        log().i("doConfigure");
        try {
            synchronized (this) {
                List<String> watched = bundleWatcher.getWatchURLs();
                for (String line : readFile()) {
                    try {
                        if (!watched.contains(line)) {
                            log().i("add", line);
                            bundleWatcher.add(line);
                        }
                    } catch (Throwable t) {
                        log().d(t);
                    }
                }
            }
        } catch (Throwable t) {
            log().d(t);
        }
    }

    private List<String> readFile() throws IOException {
        Dictionary<String, Object> prop = MOsgi.loadConfiguration(PID);
        String[] list = (String[])prop.get(CONFIG_LIST);
        if (list != null)
            return MCollection.toList(list);
        return new LinkedList<>();
    }

    private void writeFile(List<String> content) throws IOException {
        Dictionary<String, Object> prop = MOsgi.loadConfiguration(PID);
        String[] list = content.toArray(new String[content.size()]);
        prop.put(CONFIG_LIST, list);
        MOsgi.saveConfiguration(PID, prop);
    }

    @Override
    public void add(String line) throws IOException {
        synchronized (this) {
            List<String> content = readFile();
            if (content.contains(line)) content.remove(line);
            content.add(line);
            writeFile(content);
        }
    }

    @Override
    public void remove(String line) throws IOException {
        synchronized (this) {
            List<String> content = readFile();
            content.remove(line);
            writeFile(content);
        }
    }

    @Override
    public String[] list() throws IOException {
        synchronized (this) {
            return readFile().toArray(new String[0]);
        }
    }

    @Override
    public void watch() {
        doConfigure();
    }

    @Override
    public void clear() throws IOException {
        writeFile(new LinkedList<String>());
    }

    @Override
    public void remember() throws IOException, NotFoundException {
        synchronized (this) {
            BundleWatcher bundleWatcher = MOsgi.getService(BundleWatcher.class);
            List<String> watched = bundleWatcher.getWatchURLs();
            writeFile(watched);
        }
    }

}
