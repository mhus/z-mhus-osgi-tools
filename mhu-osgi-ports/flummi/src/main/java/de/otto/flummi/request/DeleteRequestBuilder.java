package de.otto.flummi.request;

import static de.otto.flummi.RequestBuilderUtil.buildUrl;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.concurrent.ExecutionException;

import com.ning.http.client.Response;

import de.mhus.lib.core.logging.Log;
import de.otto.flummi.RequestBuilderUtil;
import de.otto.flummi.util.HttpClientWrapper;

public class DeleteRequestBuilder implements RequestBuilder<Void> {
    private final HttpClientWrapper httpClient;
    private String indexName;
    private String documentType;
    private String id;

    public static final Log LOG = Log.getLog(DeleteRequestBuilder.class);

    public DeleteRequestBuilder(HttpClientWrapper httpClient) {
        this.httpClient = httpClient;
    }

    public DeleteRequestBuilder setIndexName(final String indexName) {
        this.indexName = indexName;
        return this;
    }

    public DeleteRequestBuilder setDocumentType(final String documentType) {
        this.documentType = documentType;
        return this;
    }

    public DeleteRequestBuilder setId(String id) {
        this.id = id;
        return this;
    }

    public Void execute() {
        try {
            if (indexName==null || indexName.isEmpty()) {
                throw new RuntimeException("missing property 'indexName'");
            }
            if (documentType==null || documentType.isEmpty()) {
                throw new RuntimeException("missing property 'type'");
            }
            if (id==null || id.isEmpty()) {
                throw new RuntimeException("missing property 'id'");
            }
            Response response = httpClient.prepareDelete(buildUrl(indexName, documentType, URLEncoder.encode(id, "UTF-8")))
                    .execute().get();
            if (response.getStatusCode() >= 300) {
                throw RequestBuilderUtil.toHttpServerErrorException(response);
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
