package de.mhus.karaf.mongo;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.bson.Document;
import org.bson.json.JsonWriterSettings;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import de.mhus.karaf.xdb.cmd.CmdUse;

@Command(scope = "mongo", name = "find", description = "Execute mongo find query")
@Service
public class CmdMongoFind implements Action {

	@Argument(index=0, name="collection", required=true, description="Collection name", multiValued=false)
    String collectionName;
	
	@Argument(index=1, name="find", required=false, description="Find query", multiValued=false)
    String query;

	@Option(name="-s", description="Service Name",required=false)
    String dbName = CmdUse.service;
	
	@Option(name="-d", description="Datasource Name",required=false)
	String dsName = CmdUse.datasource;

	
	@Override
	public Object execute() throws Exception {

		MongoDataSource ds = MongoUtil.getDatasource(dsName);
		MongoClient con = ds.getConnection();
		
		MongoDatabase db = con.getDatabase(dbName);

		MongoCollection<Document> collection = db.getCollection(collectionName);
		
		FindIterable<Document> res = null;
		if (query == null) {
			res = collection.find();
		} else {
			BasicDBObject find = MongoUtil.jsonMarshall(query);
			res = collection.find(find);
		}
		JsonWriterSettings writerSettings = new JsonWriterSettings(true);
		for (Document r : res) {
			System.out.println(r.toJson(writerSettings));
		}
		
		return null;
	}

}
