package de.otto.flummi;

import de.mhus.lib.core.logging.Log;
import de.otto.flummi.util.HttpClientWrapper;

public class AdminClient {

    public static final Log LOG = Log.getLog(AdminClient.class);
    private HttpClientWrapper httpClient;

    public AdminClient(HttpClientWrapper httpClient) {
        this.httpClient = httpClient;
    }

    public IndicesAdminClient indices() {
        return new IndicesAdminClient(httpClient);
    }

    public ClusterAdminClient cluster() {
        return new ClusterAdminClient(httpClient);
    }
}
