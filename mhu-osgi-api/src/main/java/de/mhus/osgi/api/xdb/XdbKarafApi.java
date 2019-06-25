package de.mhus.osgi.api.xdb;

public interface XdbKarafApi {

    void setApi(String api);

    String getService();

    void setService(String service);

    String getDatasource();

    void setDatasource(String datasource);

    String getApi();

    void load();

    void save();

}
