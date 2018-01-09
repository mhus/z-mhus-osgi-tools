package de.mhus.karaf.mongo;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import com.mongodb.MongoClient;

@Command(scope = "mongo", name = "ds-info", description = "List Mongo Datasources")
@Service
public class CmdMongoInfo implements Action {

	@Argument(index=0, name="datasource", required=true, description="Data Source Name", multiValued=false)
    String name;

	@Override
	public Object execute() throws Exception {
		
		MongoDataSource ds = MongoUtil.getDatasource(name);
		MongoClient con = ds.getConnection();
		System.out.println("Read: " + con.getReadConcern());
		System.out.println("Write: " + con.getWriteConcern());
		System.out.println("Databases: " + con.getDatabaseNames());
		return null;
	}
	
}
