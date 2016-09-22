package de.otto.flummi.request;

import java.io.File;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.ning.http.client.AsyncHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClient.BoundRequestBuilder;
import com.ning.http.client.BodyGenerator;
import com.ning.http.client.ConnectionPoolPartitioning;
import com.ning.http.client.FluentCaseInsensitiveStringsMap;
import com.ning.http.client.ListenableFuture;
import com.ning.http.client.NameResolver;
import com.ning.http.client.Param;
import com.ning.http.client.ProxyServer;
import com.ning.http.client.Realm;
import com.ning.http.client.Request;
import com.ning.http.client.Response;
import com.ning.http.client.SignatureCalculator;
import com.ning.http.client.cookie.Cookie;
import com.ning.http.client.multipart.Part;
import com.ning.http.client.uri.Uri;

public class HttpRequest {

	private BoundRequestBuilder instance;

	public HttpRequest(AsyncHttpClient.BoundRequestBuilder instance) {
		this.instance = instance;
	}

	public int hashCode() {
		return instance.hashCode();
	}

	public boolean equals(Object obj) {
		return instance.equals(obj);
	}

	public String toString() {
		return instance.toString();
	}

	public <T> ListenableFuture<T> execute(AsyncHandler<T> handler) {
		return instance.execute(handler);
	}

	public ListenableFuture<Response> execute() {
		return instance.execute();
	}

	public BoundRequestBuilder addBodyPart(Part part) {
		return instance.addBodyPart(part);
	}

	public BoundRequestBuilder addCookie(Cookie cookie) {
		return instance.addCookie(cookie);
	}

	public BoundRequestBuilder addHeader(String name, String value) {
		return instance.addHeader(name, value);
	}

	public BoundRequestBuilder setUri(Uri uri) {
		return instance.setUri(uri);
	}

	public BoundRequestBuilder addFormParam(String key, String value) {
		return instance.addFormParam(key, value);
	}

	public BoundRequestBuilder setInetAddress(InetAddress address) {
		return instance.setInetAddress(address);
	}

	public BoundRequestBuilder setLocalInetAddress(InetAddress address) {
		return instance.setLocalInetAddress(address);
	}

	public BoundRequestBuilder addQueryParam(String name, String value) {
		return instance.addQueryParam(name, value);
	}

	public Request build() {
		return instance.build();
	}

	public BoundRequestBuilder setBody(byte[] data) {
		return instance.setBody(data);
	}

	public BoundRequestBuilder setBody(InputStream stream) {
		return instance.setBody(stream);
	}

	public BoundRequestBuilder setBody(String data) {
		return instance.setBody(data);
	}

	public BoundRequestBuilder setHeader(String name, String value) {
		return instance.setHeader(name, value);
	}

	public BoundRequestBuilder setHeaders(FluentCaseInsensitiveStringsMap headers) {
		return instance.setHeaders(headers);
	}

	public BoundRequestBuilder setHeaders(Map<String, Collection<String>> headers) {
		return instance.setHeaders(headers);
	}

	public BoundRequestBuilder setFormParams(Map<String, List<String>> params) {
		return instance.setFormParams(params);
	}

	public BoundRequestBuilder setContentLength(int length) {
		return instance.setContentLength(length);
	}

	public BoundRequestBuilder setFormParams(List<Param> params) {
		return instance.setFormParams(params);
	}

	public BoundRequestBuilder setCookies(Collection<Cookie> cookies) {
		return instance.setCookies(cookies);
	}

	public BoundRequestBuilder setUrl(String url) {
		return instance.setUrl(url);
	}

	public BoundRequestBuilder setVirtualHost(String virtualHost) {
		return instance.setVirtualHost(virtualHost);
	}

	public BoundRequestBuilder addOrReplaceCookie(Cookie cookie) {
		return instance.addOrReplaceCookie(cookie);
	}

	public BoundRequestBuilder setSignatureCalculator(SignatureCalculator signatureCalculator) {
		return instance.setSignatureCalculator(signatureCalculator);
	}

	public void resetCookies() {
		instance.resetCookies();
	}

	public void resetQuery() {
		instance.resetQuery();
	}

	public void resetFormParams() {
		instance.resetFormParams();
	}

	public void resetNonMultipartData() {
		instance.resetNonMultipartData();
	}

	public void resetMultipartData() {
		instance.resetMultipartData();
	}

	public BoundRequestBuilder setBody(File file) {
		return instance.setBody(file);
	}

	public BoundRequestBuilder setBody(List<byte[]> data) {
		return instance.setBody(data);
	}

	public BoundRequestBuilder setBody(BodyGenerator bodyGenerator) {
		return instance.setBody(bodyGenerator);
	}

	public BoundRequestBuilder addQueryParams(List<Param> params) {
		return instance.addQueryParams(params);
	}

	public BoundRequestBuilder setQueryParams(Map<String, List<String>> map) {
		return instance.setQueryParams(map);
	}

	public BoundRequestBuilder setQueryParams(List<Param> params) {
		return instance.setQueryParams(params);
	}

	public BoundRequestBuilder setProxyServer(ProxyServer proxyServer) {
		return instance.setProxyServer(proxyServer);
	}

	public BoundRequestBuilder setRealm(Realm realm) {
		return instance.setRealm(realm);
	}

	public BoundRequestBuilder setFollowRedirects(boolean followRedirects) {
		return instance.setFollowRedirects(followRedirects);
	}

	public BoundRequestBuilder setRequestTimeout(int requestTimeout) {
		return instance.setRequestTimeout(requestTimeout);
	}

	public BoundRequestBuilder setRangeOffset(long rangeOffset) {
		return instance.setRangeOffset(rangeOffset);
	}

	public BoundRequestBuilder setMethod(String method) {
		return instance.setMethod(method);
	}

	public BoundRequestBuilder setBodyEncoding(String charset) {
		return instance.setBodyEncoding(charset);
	}

	public BoundRequestBuilder setConnectionPoolKeyStrategy(ConnectionPoolPartitioning connectionPoolKeyStrategy) {
		return instance.setConnectionPoolKeyStrategy(connectionPoolKeyStrategy);
	}

	public BoundRequestBuilder setNameResolver(NameResolver nameResolver) {
		return instance.setNameResolver(nameResolver);
	}
}
