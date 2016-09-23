package de.otto.flummi.request;

import static de.otto.flummi.RequestBuilderUtil.toHttpServerErrorException;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.ning.http.client.Response;

import de.mhus.lib.core.logging.Log;
import de.otto.flummi.InvalidElasticsearchResponseException;
import de.otto.flummi.bulkactions.BulkActionBuilder;
import de.otto.flummi.util.HttpClientWrapper;

public class BulkRequestBuilder implements RequestBuilder<Void> {
    private final Gson gson;
    private List<BulkActionBuilder> actions = new ArrayList();

    public static final Log LOG = Log.getLog(BulkRequestBuilder.class);
    private HttpClientWrapper httpClient;

    public BulkRequestBuilder(HttpClientWrapper httpClient) {
        this.httpClient = httpClient;
        this.gson = new Gson();

    }

    public BulkRequestBuilder add(BulkActionBuilder action) {
        this.actions.add(action);
        return this;
    }

    @Override
    public Void execute() {
        try {
            if (actions.isEmpty()) {
                return null;
            }

            StringBuilder postBody = new StringBuilder();

            for (BulkActionBuilder action : this.actions) {
                postBody.append(action.toBulkRequestAction()).append("\n");
            }

            final HttpRequestBuilder boundRequestBuilder = httpClient
                    .preparePost("/_bulk")
                    .setBody(postBody.toString())
                    .setBodyEncoding("UTF-8");

            Response response = boundRequestBuilder.execute().get();
            if (response.getStatusCode() >= 300) {
                throw toHttpServerErrorException(response);
            }
            String jsonString = response.getResponseBody();
            JsonObject responseObject = gson.fromJson(jsonString, JsonObject.class);

            String errors = responseObject.get("errors").getAsString();

            if (("true").equals(errors)) {
                boolean foundError = false;
                JsonArray items = responseObject.get("items") != null ? responseObject.get("items").getAsJsonArray() : new JsonArray();
                for (JsonElement jsonElement : items) {
                    JsonElement updateField = jsonElement.getAsJsonObject().get("update");
                    if (updateField != null) {
                        final JsonElement status = updateField.getAsJsonObject().get("status");
                        final JsonElement error = updateField.getAsJsonObject().get("error");
                        if (status != null && status.getAsInt() != 404 && error != null && !error.getAsString().isEmpty()) {
                            foundError = true;
                        }
                    } else {
                        for (Map.Entry<String, JsonElement> opElement : jsonElement.getAsJsonObject().entrySet()) {
                            JsonObject opObject = opElement.getValue().getAsJsonObject();
                            JsonElement errorObj = opObject.get("error");
                            if (opObject != null && errorObj != null && errorObj.isJsonObject() ) {
                                foundError = true;
                                LOG.d(errorObj);
                            }
                        }
                    }
                }

                if (foundError) {
                    throw new InvalidElasticsearchResponseException("Response contains errors': " + jsonString);
                }
            }
            return null;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public int size() {
        return actions.size();
    }
}
