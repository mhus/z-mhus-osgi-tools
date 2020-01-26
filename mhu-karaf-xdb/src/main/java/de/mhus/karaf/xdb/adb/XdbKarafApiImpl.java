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
package de.mhus.karaf.xdb.adb;

import java.io.File;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import de.mhus.karaf.xdb.cmd.CmdUse;
import de.mhus.lib.core.MApi;
import de.mhus.lib.core.MFile;
import de.mhus.lib.core.lang.MObject;
import de.mhus.lib.core.util.MUri;
import de.mhus.osgi.api.xdb.XdbKarafApi;

@Component
public class XdbKarafApiImpl extends MObject implements XdbKarafApi {

    private String api = AdbXdbApi.NAME;
    private String service = null;
    private String datasource = null;

    @Activate
    public void doActivate() {
        load();
    }

    @Override
    public void load() {
        try {
            File f = getFile();
            if (f.exists()) {
                log().d("load from", f);
                MUri uri = MUri.toUri(MFile.readFile(f).trim());
                if ("xdb".equals(uri.getScheme())) {
                    if (uri.getPathParts().length > 0) api = uri.getPathParts()[0];
                    if (uri.getPathParts().length > 1) service = uri.getPathParts()[1];
                    if (uri.getPathParts().length > 2) datasource = uri.getPathParts()[2];
                }
                log().i("XDB loaded", uri);
            } else {
                log().d("not found", f);
            }
        } catch (Throwable t) {
            log().d(t);
        }
    }

    @Override
    public void save() {
        File f = getFile();
        String content =
                "xdb:"
                        + MUri.encode(api)
                        + "/"
                        + MUri.encode(service)
                        + "/"
                        + MUri.encode(datasource);
        log().d("save to", f);
        MFile.writeFile(f, content);
    }

    private File getFile() {
        return MApi.getFile(MApi.SCOPE.ETC, CmdUse.class.getCanonicalName() + ".cfg");
    }

    @Override
    public String getApi() {
        return api;
    }

    @Override
    public void setApi(String api) {
        this.api = api;
    }

    @Override
    public String getService() {
        return service;
    }

    @Override
    public void setService(String service) {
        this.service = service;
    }

    @Override
    public String getDatasource() {
        return datasource;
    }

    @Override
    public void setDatasource(String datasource) {
        this.datasource = datasource;
    }
}
