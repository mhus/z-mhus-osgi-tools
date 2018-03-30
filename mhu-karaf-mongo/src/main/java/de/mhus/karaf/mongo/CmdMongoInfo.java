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
