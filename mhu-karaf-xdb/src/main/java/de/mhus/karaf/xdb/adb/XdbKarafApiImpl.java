package de.mhus.karaf.xdb.adb;

import java.io.File;

import org.osgi.service.component.annotations.Component;

import de.mhus.karaf.xdb.cmd.CmdUse;
import de.mhus.lib.core.MFile;
import de.mhus.lib.core.lang.MObject;
import de.mhus.lib.core.util.MUri;
import de.mhus.osgi.api.xdb.XdbKarafApi;

@Component
public class XdbKarafApiImpl extends MObject implements XdbKarafApi {

    private String api = AdbXdbApi.NAME;
    private String service = null;
    private String datasource = null;

    public void doActivate() {
        load();
    }
    
    @Override
    public void load() {
        try {
            File f = getFile();
            if (f.exists()) {
                MUri uri = MUri.toUri(MFile.readFile(f).trim());
                if ("xdb".equals(uri.getScheme())) {
                    if (uri.getPathParts().length > 0)
                        api = uri.getPathParts()[0];
                    if (uri.getPathParts().length > 1)
                        service = uri.getPathParts()[1];
                    if (uri.getPathParts().length > 2)
                        datasource = uri.getPathParts()[2];
                }
                log().i("XDB loaded",uri);
            }
        } catch (Throwable t) {}
    }

    @Override
    public void save() {
        File f = getFile();
        String content = "xdb:" + MUri.encode(api) + "/" + MUri.encode(service) + "/" + MUri.encode(datasource);
        MFile.writeFile(f, content);
    }
    
    private File getFile() {
        return new File("etc/" + CmdUse.class.getCanonicalName() + ".cfg");
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
