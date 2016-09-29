package de.otto.flummi.request;

import static de.otto.flummi.RequestBuilderUtil.buildUrl;
import static de.otto.flummi.RequestBuilderUtil.toHttpServerErrorException;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.concurrent.ExecutionException;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.ning.http.client.Response;

import de.mhus.lib.core.logging.Log;
import de.otto.flummi.util.HttpClientWrapper;

public class IndexRequestBuilder implements RequestBuilder<Void> {
    private final Gson gson;
    private JsonPrimitive id;
    private String indexName;
    private String documentType;
    private JsonObject source;
    private String parent;

    public static final Log LOG = Log.getLog(IndexRequestBuilder.class);
    private HttpClientWrapper httpClient;

    public IndexRequestBuilder(HttpClientWrapper httpClient) {
        this.httpClient = httpClient;
        this.gson = new Gson();
    }

    public IndexRequestBuilder setId(String id) {
        this.id = new JsonPrimitive(id);
        return this;
    }

    public IndexRequestBuilder setId(int id) {
        this.id = new JsonPrimitive(id);
        return this;
    }

    public IndexRequestBuilder setIndexName(String indexName) {
        this.indexName = indexName;
        return this;
    }

    public IndexRequestBuilder setDocumentType(String documentType) {
        this.documentType = documentType;
        return this;
    }

    public IndexRequestBuilder setSource(JsonObject source) {
        this.source = source;
        return this;
    }

    public IndexRequestBuilder setParent(String parent) {
        this.parent = parent;
        return this;
    }

    @Override
    public Void execute() {
        try {
            HttpRequestBuilder reqBuilder;
            if (id != null) {
                String url = buildUrl(indexName, documentType, URLEncoder.encode(id.getAsString(), "UTF-8"));
                reqBuilder = httpClient.preparePut(url);
            } else {
                String url = buildUrl(indexName, documentType);
                reqBuilder = httpClient.preparePost(url);
            }
            if (parent != null) {
                reqBuilder.addQueryParam("parent", parent);
            }
            Response response = reqBuilder.setBody(gson.toJson(source)).setBodyEncoding("UTF-8").execute().get();
            if (response.getStatusCode() >= 300) {
                throw toHttpServerErrorException(response);
            }
            return null;
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

}
