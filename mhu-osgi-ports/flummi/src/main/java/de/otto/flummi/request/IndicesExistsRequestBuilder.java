package de.otto.flummi.request;


import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.concurrent.ExecutionException;

import com.ning.http.client.Response;

import de.mhus.lib.core.logging.Log;
import de.otto.flummi.response.HttpServerErrorException;
import de.otto.flummi.util.HttpClientWrapper;

public class IndicesExistsRequestBuilder implements RequestBuilder<Boolean> {
    private final String indexName;

    public static final Log LOG = Log.getLog(IndicesExistsRequestBuilder.class);
    private HttpClientWrapper httpClient;

    public IndicesExistsRequestBuilder(HttpClientWrapper httpClient, String indexName) {
        this.indexName = indexName;
        this.httpClient = httpClient;
    }

    public Boolean execute() {
        try {
            Response response = httpClient.prepareHead("/" + indexName).execute().get();
            int statusCode = response.getStatusCode();
            if (statusCode >= 300 && response.getStatusCode() != 404) {
                throw new HttpServerErrorException(response.getStatusCode(), response.getStatusText(), response.getResponseBody());
            }
            return statusCode < 300;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
