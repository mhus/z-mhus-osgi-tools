package de.otto.flummi.response;

import com.google.gson.JsonObject;

public class SearchHit {

    private final String id;
    private final JsonObject source;
    private JsonObject fields;
    private final Float score;
	private String type;
	private String index;
	
    public SearchHit(final String id, final JsonObject source, final JsonObject fields, final Float score, String type, String index) {
        this.id = id;
        this.source = source;
        this.fields = fields;
        this.score = score;
        this.type = type;
        this.index = index;
    }

    public String getId() {
        return id;
    }

    public JsonObject getSource() {
        return source;
    }

    public Float getScore() {
        return score;
    }

    public JsonObject getFields() {
        return fields;
    }
    
	public String getType() {
		return type;
	}

	public void setIndex(String index) {
		this.index = index;
	}

}
