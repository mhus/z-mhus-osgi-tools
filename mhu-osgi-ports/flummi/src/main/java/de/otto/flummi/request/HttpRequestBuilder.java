package de.otto.flummi.request;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import com.ning.http.client.AsyncHandler;
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

import de.mhus.lib.core.MLog;
import de.mhus.lib.core.logging.Log;

public class HttpRequestBuilder extends MLog {

	private BoundRequestBuilder instance;

	public HttpRequestBuilder(BoundRequestBuilder boundRequestBuilder) {
		this.instance = boundRequestBuilder;
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

	public <T> ListenableFuture<T> execute(AsyncHandler<T> handler) throws InterruptedException, ExecutionException {
		ListenableFuture<T> ret = instance.execute(handler);
		if (log().isLevelEnabled(Log.LEVEL.DEBUG))
			log().d("result", ret.get() );
		return ret;
	}

	public ListenableFuture<Response> execute() throws InterruptedException, ExecutionException {
		ListenableFuture<Response> ret = instance.execute();
		if (log().isLevelEnabled(Log.LEVEL.DEBUG))
			try {
				Response resp = ret.get();
				if (resp.getStatusCode() != 200)
					log().d("result", resp.getStatusCode(), resp.getStatusText() );
				else
					log().d("result", resp.getResponseBody() );
			} catch (IOException e) {
			}
		return ret;
	}

	public HttpRequestBuilder addBodyPart(Part part) {
		instance.addBodyPart(part);
		return this;
	}

	public HttpRequestBuilder addCookie(Cookie cookie) {
		instance.addCookie(cookie);
		return this;
	}

	public HttpRequestBuilder addHeader(String name, String value) {
		instance.addHeader(name, value);
		return this;
	}

	public HttpRequestBuilder setUri(Uri uri) {
		instance.setUri(uri);
		return this;
	}

	public HttpRequestBuilder addFormParam(String key, String value) {
		instance.addFormParam(key, value);
		return this;
	}

	public HttpRequestBuilder setInetAddress(InetAddress address) {
		instance.setInetAddress(address);
		return this;
	}

	public HttpRequestBuilder setLocalInetAddress(InetAddress address) {
		instance.setLocalInetAddress(address);
		return this;
	}

	public HttpRequestBuilder addQueryParam(String name, String value) {
		instance.addQueryParam(name, value);
		return this;
	}

	public Request build() {
		return instance.build();
	}

	public HttpRequestBuilder setBody(byte[] data) {
		log().d("request",data);
		instance.setBody(data);
		return this;
	}

	public HttpRequestBuilder setBody(InputStream stream) {
		log().d("request is stream");
		instance.setBody(stream);
		return this;
	}

	public HttpRequestBuilder setBody(String data) {
		log().d("request",data);
		instance.setBody(data);
		return this;
	}

	public HttpRequestBuilder setHeader(String name, String value) {
		instance.setHeader(name, value);
		return this;
	}

	public HttpRequestBuilder setHeaders(FluentCaseInsensitiveStringsMap headers) {
		instance.setHeaders(headers);
		return this;
	}

	public HttpRequestBuilder setHeaders(Map<String, Collection<String>> headers) {
		instance.setHeaders(headers);
		return this;
	}

	public HttpRequestBuilder setFormParams(Map<String, List<String>> params) {
		instance.setFormParams(params);
		return this;
	}

	public HttpRequestBuilder setContentLength(int length) {
		instance.setContentLength(length);
		return this;
	}

	public HttpRequestBuilder setFormParams(List<Param> params) {
		instance.setFormParams(params);
		return this;
	}

	public HttpRequestBuilder setCookies(Collection<Cookie> cookies) {
		instance.setCookies(cookies);
		return this;
	}

	public HttpRequestBuilder setUrl(String url) {
		instance.setUrl(url);
		return this;
	}

	public HttpRequestBuilder setVirtualHost(String virtualHost) {
		instance.setVirtualHost(virtualHost);
		return this;
	}

	public HttpRequestBuilder addOrReplaceCookie(Cookie cookie) {
		instance.addOrReplaceCookie(cookie);
		return this;
	}

	public HttpRequestBuilder setSignatureCalculator(SignatureCalculator signatureCalculator) {
		instance.setSignatureCalculator(signatureCalculator);
		return this;
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

	public HttpRequestBuilder setBody(File file) {
		instance.setBody(file);
		return this;
	}

	public HttpRequestBuilder setBody(List<byte[]> data) {
		instance.setBody(data);
		return this;
	}

	public HttpRequestBuilder setBody(BodyGenerator bodyGenerator) {
		instance.setBody(bodyGenerator);
		return this;
	}

	public HttpRequestBuilder addQueryParams(List<Param> params) {
		instance.addQueryParams(params);
		return this;
	}

	public HttpRequestBuilder setQueryParams(Map<String, List<String>> map) {
		instance.setQueryParams(map);
		return this;
	}

	public HttpRequestBuilder setQueryParams(List<Param> params) {
		instance.setQueryParams(params);
		return this;
	}

	public HttpRequestBuilder setProxyServer(ProxyServer proxyServer) {
		instance.setProxyServer(proxyServer);
		return this;
	}

	public HttpRequestBuilder setRealm(Realm realm) {
		instance.setRealm(realm);
		return this;
	}

	public HttpRequestBuilder setFollowRedirects(boolean followRedirects) {
		instance.setFollowRedirects(followRedirects);
		return this;
	}

	public HttpRequestBuilder setRequestTimeout(int requestTimeout) {
		instance.setRequestTimeout(requestTimeout);
		return this;
	}

	public HttpRequestBuilder setRangeOffset(long rangeOffset) {
		instance.setRangeOffset(rangeOffset);
		return this;
	}

	public HttpRequestBuilder setMethod(String method) {
		instance.setMethod(method);
		return this;
	}

	public HttpRequestBuilder setBodyEncoding(String charset) {
		instance.setBodyEncoding(charset);
		return this;
	}

	public HttpRequestBuilder setConnectionPoolKeyStrategy(ConnectionPoolPartitioning connectionPoolKeyStrategy) {
		instance.setConnectionPoolKeyStrategy(connectionPoolKeyStrategy);
		return this;
	}

	public HttpRequestBuilder setNameResolver(NameResolver nameResolver) {
		instance.setNameResolver(nameResolver);
		return this;
	}
}
