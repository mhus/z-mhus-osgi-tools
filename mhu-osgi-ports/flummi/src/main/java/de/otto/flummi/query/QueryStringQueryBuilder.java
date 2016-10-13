package de.otto.flummi.query;

import com.google.gson.JsonObject;

public class QueryStringQueryBuilder implements QueryBuilder {
    private final String query;

    public QueryStringQueryBuilder(String query) {
        this.query = query;
    }

    @Override
    public JsonObject build() {
        if (query == null) {
            throw new RuntimeException("missing property 'query'");
        }
        JsonObject jsonObject = new JsonObject();
        JsonObject term = new JsonObject();
        jsonObject.add("query_string", term);
        term.addProperty("query", query);
        return jsonObject;
    }
}
