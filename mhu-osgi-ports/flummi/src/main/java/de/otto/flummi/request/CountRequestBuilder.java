package de.otto.flummi.request;

import static de.otto.flummi.RequestBuilderUtil.toHttpServerErrorException;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.concurrent.ExecutionException;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.ning.http.client.Response;

import de.mhus.lib.core.logging.Log;
import de.otto.flummi.RequestBuilderUtil;
import de.otto.flummi.util.HttpClientWrapper;

public class CountRequestBuilder implements RequestBuilder<Long> {

    private final String[] indices;
    private final Gson gson;
    private String[] types;

    public static final Log LOG = Log.getLog(CountRequestBuilder.class);
    private HttpClientWrapper httpClient;

    public CountRequestBuilder(HttpClientWrapper httpClient, String... indices) {
        this.httpClient = httpClient;
        this.indices = indices;
        this.gson = new Gson();
    }

    public CountRequestBuilder setTypes(String... types) {
        this.types = types;
        return this;
    }

    @Override
    public Long execute() {
        try {
            String url = RequestBuilderUtil.buildUrl(indices, types, "_count");
            Response response = httpClient.prepareGet(url).execute().get();
            if (response.getStatusCode() >= 300) {
                throw toHttpServerErrorException(response);
            }
            String jsonString = response.getResponseBody();
            JsonObject responseObject = gson.fromJson(jsonString, JsonObject.class);
            return responseObject.get("count").getAsLong();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}