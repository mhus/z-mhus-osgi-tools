/**
 * Copyright 2018 Mike Hummel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.mhus.karaf.mongo.xdb;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.mongodb.morphia.query.Criteria;
import org.mongodb.morphia.query.FieldEnd;
import org.mongodb.morphia.query.Query;

import de.mhus.lib.adb.query.AAnd;
import de.mhus.lib.adb.query.ACompare;
import de.mhus.lib.adb.query.ADbAttribute;
import de.mhus.lib.adb.query.AEnumFix;
import de.mhus.lib.adb.query.AFix;
import de.mhus.lib.adb.query.ALimit;
import de.mhus.lib.adb.query.ALiteral;
import de.mhus.lib.adb.query.ALiteralList;
import de.mhus.lib.adb.query.AOperation;
import de.mhus.lib.adb.query.AOr;
import de.mhus.lib.adb.query.AOrder;
import de.mhus.lib.adb.query.APart;
import de.mhus.lib.adb.query.APrint;
import de.mhus.lib.adb.query.AQuery;
import de.mhus.lib.core.MJson;
import de.mhus.lib.core.lang.MObject;
import de.mhus.lib.core.parser.StringCompiler;
import de.mhus.lib.errors.MException;
import de.mhus.lib.errors.NotSupportedException;

public class MoQueryBuilder extends MObject {


	private ObjectNode json;

	public MoQueryBuilder(String search) throws IOException {
		if (search == null) search = "";
		if (!search.startsWith("{") && !search.endsWith("}")) search = "{" + search + "}";
		json = (ObjectNode) MJson.load(search);
	}

	public MoQueryBuilder(AQuery<?> query) throws IOException {
		StringBuilder s = new StringBuilder();
		createQuery(query, s);
		json = (ObjectNode) MJson.load(s.toString());
	}

	@SuppressWarnings("incomplete-switch")
	private void createQuery(APrint p, StringBuilder s) {
		if (p instanceof AQuery) {
			//		buffer.append('(');
			boolean first = true;
			{
				s.append("{");
				for (AOperation operation : ((AQuery<?>)p).getOperations() ) {
					if (operation instanceof APart) {
						if (first)
							first = false;
						else
							s.append(",");
						createQuery(operation, s);
					}
				}
			}
			//		buffer.append(')');

			{
				AOperation limit = null;
				for (AOperation operation : ((AQuery<?>)p).getOperations() ) {
					if (operation instanceof AOrder) {
						if (first)
							first = false;
						else
							s.append(",");
						createQuery(operation, s);
					} else
						if (operation instanceof ALimit)
							limit = operation;
				}

				if (limit != null) {
					if (first)
						first = false;
					else
						s.append(",");
					createQuery(limit, s);
				}
				s.append("}");
			}
		} else
		if (p instanceof AAnd) {
			s.append(",\"$and\":[");
			boolean first = true;
			for (APart part : ((AAnd)p).getOperations()) {
				if (first)
					first = false;
				else
					s.append(",");
				createQuery(part, s);
			}
			s.append(']');
		} else
		if (p instanceof ACompare) {
			createQuery( ((ACompare)p).getLeft(), s);
			s.append(":{");
			switch (((ACompare)p).getEq()) {
			case EG:
				s.append("\"$eg\":");
				break;
			case EL:
				s.append("\"$lte\":");
				break;
			case EQ:
				s.append("\"$eq\":");
				break;
			case GT:
				s.append("\"$gt\":");
				break;
			case GE:
				s.append("\"$gte\":");
				break;
			case LIKE:
				s.append("\"$contains\":");
				break;
			case LT:
				s.append("\"$lt\":");
				break;
			case LE:
				s.append("\"$le\":");
				break;
			case NE:
				s.append("\"$ne\":");
				break;
//			case IN:
//				buffer.append(" in ");
//				break;
			}
			createQuery( ((ACompare)p).getRight(), s);
			s.append("}");
		} else
		if (p instanceof ADbAttribute) {
			s.append("\"").append(((ADbAttribute)p).getAttribute()).append("\"");
		} else
//		if (p instanceof ADynValue) {
//			buffer.append('$').append(((ADynValue)p).getName()).append('$');
//		} else
		if (p instanceof AEnumFix) {
			s.append("\"").append(((AEnumFix)p).getValue().ordinal()).append("\"");
		} else
		if (p instanceof AFix) {
			s.append("\"").append(((AFix)p).getValue()).append("\"");
		} else
		if (p instanceof ALimit) {
			s.append(",\"$offset\":\"").append(((ALimit)p).getOffset()).append("\",\"$limit\":\"").append(((ALimit)p).getLimit()).append("\""); //mysql specific !!
		} else
//		if (p instanceof AList) {
//			buffer.append('(');
//			boolean first = true;
//			for (AAttribute part : ((AList)p).getOperations()) {
//				if (first)
//					first = false;
//				else
//					buffer.append(",");
//				createQuery(part, query);
//			}
//			buffer.append(')');
//		} else
		if (p instanceof ALiteral) {
			s.append("\"").append(((ALiteral)p).getLiteral()).append("\"");
		} else
		if (p instanceof ALiteralList) {
			for (APart part : ((ALiteralList)p).getOperations()) {
				createQuery(part, s);
			}
		} else
//		if (p instanceof ANot) {
//			buffer.append("not ");
//			createQuery( ((ANot)p).getOperation(), query);
//		} else
//		if (p instanceof ANull) {
//			createQuery( ((ANull)p).getAttr(), query );
//			buffer.append(" is ");
//			if (!((ANull)p).isIs()) buffer.append("not ");
//			buffer.append("null");
//		} else
		if (p instanceof AOr) {
			s.append(",\"$or\":[");
			boolean first = true;
			for (APart part : ((AOr)p).getOperations()) {
				if (first)
					first = false;
				else
					s.append(",");
				createQuery(part, s);
			}
			s.append(']');
		} else
		if (p instanceof AOrder) {
			s.append(",\"$order\":\"").append(((AOrder)p).getAttribute()).append("\"");
		} else
//		if (p instanceof ASubQuery) {
//			DbManager manager = ((SqlDialectCreateContext)query.getContext()).getManager();
//			String qualification = manager.toQualification(((ASubQuery)p).getSubQuery()).trim();
//			
//			createQuery( ((ASubQuery)p).getLeft(), query);
//			buffer.append(" IN (");
//			
//			StringBuilder buffer2 = new StringBuilder().append("DISTINCT ");
//			
//			AQuery<?> subQuery = ((ASubQuery)p).getSubQuery();
//			subQuery.setContext(new SqlDialectCreateContext(manager, buffer2 ) );
//			createQuery( ((ASubQuery)p).getProjection(), subQuery );
//			
//			buffer.append( manager.createSqlSelect(((ASubQuery)p).getSubQuery().getType(), buffer2.toString() , qualification));
//
//			buffer.append(")");
//		} else
			throw new NotSupportedException(p.getClass());
	}

	public <T> void create(Query<T> q, Map<String,Object> parameterValues) throws IOException {
		add(q, json, parameterValues);
	}

	@SuppressWarnings("deprecation")
	private <T> void add(Query<T> q, ObjectNode j, Map<String,Object> parameterValues) throws IOException {
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
					criteria[i] = (Criteria) setValue(c,objf.getKey(),objf.getValue(), parameterValues);
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
					criteria[i] = (Criteria) setValue(c,objf.getKey(),objf.getValue(), parameterValues);
				}
				q.and(criteria);
			} else
			if (k.equals("$order")) {
				q.order(v.asText());
			} else
			if (k.equals("$where")) {
				q.where(v.asText());
			} else
			if (k.equals("$offset")) {
				q.offset(v.asInt());
			} else
			if (k.equals("$limit")) {
				q.limit(v.asInt());
			} else
				setValue(q.field(k), k, v, parameterValues);
		}
	}

	private <T> Object setValue(FieldEnd<?> f, String k, JsonNode v, Map<String,Object> parameterValues) throws IOException {
		if (v.isObject()) {
			ObjectNode exp = (ObjectNode)v;
			Entry<String, JsonNode> e = exp.getFields().next();
			String c = e.getKey();
			String vv = e.getValue().asText();
			if (parameterValues != null)
				try {
					vv = new StringCompiler().compileString(vv).execute(parameterValues);
				} catch (MException e1) {
					log().d(e1);
				}
			switch (c) {
			case "$lt": return f.lessThan(vv);
			case "$lte": return f.lessThanOrEq(vv);
			case "$eq": return f.equal(vv);
			case "$gt": return f.greaterThan(vv);
			case "$gte": return f.greaterThanOrEq(vv);
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
