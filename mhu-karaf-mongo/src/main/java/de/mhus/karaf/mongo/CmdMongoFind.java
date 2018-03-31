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
package de.mhus.karaf.mongo;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.api.console.Session;
import org.bson.Document;
import org.bson.json.JsonWriterSettings;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import de.mhus.karaf.xdb.cmd.XdbUtil;
import de.mhus.lib.mongo.MoUtil;

@Command(scope = "mongo", name = "find", description = "Execute mongo find query")
@Service
public class CmdMongoFind implements Action {

	@Argument(index=0, name="collection", required=true, description="Collection name", multiValued=false)
    String collectionName;
	
	@Argument(index=1, name="find", required=false, description="Find query", multiValued=false)
    String query;

	@Option(name="-s", description="Service Name",required=false)
    String dbName;
	
	@Option(name="-d", description="Datasource Name",required=false)
	String dsName;

    @Reference
    private Session session;
	
	@Override
	public Object execute() throws Exception {

		dsName = XdbUtil.getDatasourceName(session, dsName);
		dbName = XdbUtil.getServiceName(session, dbName);

		MongoDataSource ds = MongoUtil.getDatasource(dsName);
		MongoClient con = ds.getConnection();
		
		MongoDatabase db = con.getDatabase(dbName);

		MongoCollection<Document> collection = db.getCollection(collectionName);
		
		FindIterable<Document> res = null;
		if (query == null) {
			res = collection.find();
		} else {
			BasicDBObject find = MoUtil.jsonMarshall(query);
			res = collection.find(find);
		}
		@SuppressWarnings("deprecation")
		JsonWriterSettings writerSettings = new JsonWriterSettings(true);
		for (Document r : res) {
			System.out.println(r.toJson(writerSettings));
		}
		
		return null;
	}

}
