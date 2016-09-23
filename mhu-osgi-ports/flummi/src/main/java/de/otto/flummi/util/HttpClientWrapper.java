package de.otto.flummi.util;

import com.ning.http.client.AsyncHttpClient;

import de.otto.flummi.request.HttpRequestBuilder;

public class HttpClientWrapper {

    private final AsyncHttpClient asyncHttpClient;
    private final String baseUrl;

    public HttpClientWrapper(AsyncHttpClient asyncHttpClient, String baseUrl) {
        this.asyncHttpClient = asyncHttpClient;
        this.baseUrl = baseUrl;
    }

    public HttpRequestBuilder prepareGet(String url) {
        return new HttpRequestBuilder( asyncHttpClient.prepareGet(baseUrl + url) );
    }

    public HttpRequestBuilder preparePost(String url) {
        return new HttpRequestBuilder( asyncHttpClient.preparePost(baseUrl + url) );
    }

    public HttpRequestBuilder preparePut(String url) {
        return new HttpRequestBuilder( asyncHttpClient.preparePut(baseUrl + url) );
    }

    public HttpRequestBuilder prepareDelete(String url) {
        return new HttpRequestBuilder( asyncHttpClient.prepareDelete(baseUrl + url) );
    }

    public HttpRequestBuilder prepareHead(String url) {
        return new HttpRequestBuilder( asyncHttpClient.prepareHead(baseUrl + url) );
    }
}
