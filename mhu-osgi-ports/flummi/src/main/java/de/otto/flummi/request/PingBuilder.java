package de.otto.flummi.request;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.ning.http.client.Response;

import de.otto.flummi.response.PingResponse;
import de.otto.flummi.util.HttpClientWrapper;

public class PingBuilder implements RequestBuilder<PingResponse> {

	private HttpClientWrapper httpClient;
	private Gson gson;

	public PingBuilder(HttpClientWrapper httpClient) {
		this.httpClient = httpClient;
		this.gson = new Gson();
	}

	@Override
	public PingResponse execute() {
		
		try {
			Response response = httpClient.prepareGet("/").execute().get();
			JsonObject jsonResponse = gson.fromJson(response.getResponseBody(), JsonObject.class);
			
			return new PingResponse(
					jsonResponse.get("cluster_name").getAsString(),
					jsonResponse.get("name").getAsString(),
					jsonResponse.get("tagline").getAsString(),
					jsonResponse.getAsJsonObject("version").get("build_hash").getAsString(),
					jsonResponse.getAsJsonObject("version").get("build_snapshot").getAsBoolean(),
					jsonResponse.getAsJsonObject("version").get("build_timestamp").getAsString(),
					jsonResponse.getAsJsonObject("version").get("lucene_version").getAsString(),
					jsonResponse.getAsJsonObject("version").get("number").getAsString()
					);
		} catch (Exception e) {
            throw new RuntimeException(e);
		}
	}

}
