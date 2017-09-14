package de.mhus.osgi.sop.impl.rest;

import java.util.Arrays;
import java.util.Map.Entry;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.lib.core.MApi;
import de.mhus.lib.core.console.ConsoleTable;
import de.mhus.osgi.sop.api.rest.RestApi;
import de.mhus.osgi.sop.api.rest.RestNodeService;

@Command(scope = "sop", name = "rest", description = "REST Call")
@Service
public class RestCmd implements Action {

	@Override
	public Object execute() throws Exception {

        RestApi restService = MApi.lookup(RestApi.class);

        ConsoleTable table = new ConsoleTable();
        table.setHeaderValues("Registered","Node Id","Parents","Class");
        for (Entry<String, RestNodeService> entry : restService.getRestNodeRegistry().entrySet()) {
        	table.addRowValues(entry.getKey(),entry.getValue().getNodeId(), Arrays.toString( entry.getValue().getParentNodeIds() ),entry.getValue().getClass().getCanonicalName() );
        }
        table.print(System.out);
		return null;
	}

}
