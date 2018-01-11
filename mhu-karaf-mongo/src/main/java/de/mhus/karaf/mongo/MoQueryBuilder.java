package de.mhus.karaf.mongo;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map.Entry;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.mongodb.morphia.query.Criteria;
import org.mongodb.morphia.query.FieldEnd;
import org.mongodb.morphia.query.Query;

import de.mhus.lib.core.MJson;

public class MoQueryBuilder {


	private ObjectNode json;

	public MoQueryBuilder(String search) throws IOException {
		if (search == null) search = "";
		if (!search.startsWith("{") && !search.endsWith("}")) search = "{" + search + "}";
		json = (ObjectNode) MJson.load(search);
	}

	public <T> void create(Query<T> q) throws IOException {
		add(q, json);
	}

	private <T> void add(Query<T> q, ObjectNode j) throws IOException {
		for (Iterator<Entry<String, JsonNode>> ki = j.getFields(); ki.hasNext(); ) {
			Entry<String, JsonNode> ke = ki.next();
			String k = ke.getKey();
			JsonNode v = ke.getValue();
			if (k.equals("$or")) { // "$or":[{"name":"Max"},{"name":"Sabine"}]
				ArrayNode a = (ArrayNode) v;
				Criteria[] criteria = new Criteria[a.size()];
				for (int i = 0; i < criteria.length; i++) { // [{"name":"Max"},{"name":"Sabine"}]
					ObjectNode obj = (ObjectNode) a.get(i); // {"name":"Max"}
					Entry<String, JsonNode> objf = obj.getFields().next(); // "name":"Max"
					FieldEnd<?> c = q.criteria(objf.getKey());
					criteria[i] = (Criteria) setValue(c,objf.getKey(),objf.getValue());
				}
				q.or(criteria);
			} else
			if (k.equals("$and")) {
				ArrayNode a = (ArrayNode) v;
				Criteria[] criteria = new Criteria[a.size()];
				for (int i = 0; i < criteria.length; i++) { // [{"name":"Max"},{"name":"Sabine"}]
					ObjectNode obj = (ObjectNode) a.get(i); // {"name":"Max"}
					Entry<String, JsonNode> objf = obj.getFields().next(); // "name":"Max"
					FieldEnd<?> c = q.criteria(objf.getKey());
					criteria[i] = (Criteria) setValue(c,objf.getKey(),objf.getValue());
				}
				q.and(criteria);
			} else
			if (k.equals("$order")) {
				q.order(v.asText());
			} else
			if (k.equals("$offset")) {
				q.offset(v.asInt());
			} else
			if (k.equals("$limit")) {
				q.limit(v.asInt());
			} else
				setValue(q.field(k), k, v);
		}
	}

	private <T> Object setValue(FieldEnd<?> f, String k, JsonNode v) throws IOException {
		if (v.isObject()) {
			ObjectNode exp = (ObjectNode)v;
			Entry<String, JsonNode> e = exp.getFields().next();
			String c = e.getKey();
			String vv = e.getValue().asText();
			switch (c) {
			case "$lt": return f.lessThan(vv);
			case "$le": return f.lessThanOrEq(vv);
			case "$eq": return f.equal(vv);
			case "$gt": return f.greaterThan(vv);
			case "$ge": return f.greaterThanOrEq(vv);
			case "$ne": return f.notEqual(vv);
			case "$contains": return f.contains(vv);
			case "$containsIgnoreCase":
			case "$text": return f.containsIgnoreCase(vv);
			default: throw new IOException("Unknown condition " + c);
			}
		} else {
			return f.equal(v.asText());
		}
	}

}
