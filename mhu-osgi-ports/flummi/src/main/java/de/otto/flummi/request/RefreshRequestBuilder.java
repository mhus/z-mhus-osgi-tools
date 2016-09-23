package de.otto.flummi.request;

import static de.otto.flummi.RequestBuilderUtil.toHttpServerErrorException;

import java.util.concurrent.ExecutionException;

import com.ning.http.client.Response;

import de.mhus.lib.core.logging.Log;
import de.otto.flummi.util.HttpClientWrapper;

public class RefreshRequestBuilder {
    private HttpClientWrapper httpClient;
    private final String indexName;

    public static final Log LOG = Log.getLog(RefreshRequestBuilder.class);

    public RefreshRequestBuilder(HttpClientWrapper httpClient, String indexName) {
        this.httpClient = httpClient;
        this.indexName = indexName;
    }

    public void execute() {
        try {
            Response response = httpClient.preparePost("/" + indexName + "/_refresh").execute().get();
            if (response.getStatusCode() >= 300) {
                throw toHttpServerErrorException(response);
            }
            return;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
