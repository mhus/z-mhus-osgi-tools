package de.otto.flummi.request;

import java.util.concurrent.ExecutionException;

import com.ning.http.client.Response;

import de.otto.flummi.RequestBuilderUtil;
import de.otto.flummi.util.HttpClientWrapper;

public class DeleteIndexRequestBuilder implements RequestBuilder<Void> {
    private final HttpClientWrapper httpClient;
    private final String[] indexNames;

    public DeleteIndexRequestBuilder(HttpClientWrapper httpClient, String... indexNames) {
        this.httpClient = httpClient;
        this.indexNames = indexNames;
    }

    public Void execute() {
        try {
            String url = RequestBuilderUtil.buildUrl(indexNames, null, null);
            Response response = httpClient.prepareDelete(url).execute().get();
            if (response.getStatusCode() >= 300 && response.getStatusCode() != 404) {
                throw RequestBuilderUtil.toHttpServerErrorException(response);
            }
            return null;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
