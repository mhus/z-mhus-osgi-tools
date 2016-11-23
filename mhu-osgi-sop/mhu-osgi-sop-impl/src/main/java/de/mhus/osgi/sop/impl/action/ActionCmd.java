package de.mhus.osgi.sop.impl.action;

import java.util.List;
import java.util.Map;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.lib.core.IProperties;
import de.mhus.lib.core.MProperties;
import de.mhus.lib.core.MString;
import de.mhus.lib.core.console.ConsoleTable;
import de.mhus.lib.core.strategy.Operation;
import de.mhus.lib.core.strategy.OperationDescription;
import de.mhus.lib.core.strategy.OperationResult;
import de.mhus.lib.jms.JmsConnection;
import de.mhus.lib.karaf.jms.JmsUtil;
import de.mhus.osgi.sop.api.Sop;
import de.mhus.osgi.sop.api.SopApi;
import de.mhus.osgi.sop.api.aaa.AaaContext;
import de.mhus.osgi.sop.api.aaa.AccessApi;
import de.mhus.osgi.sop.api.action.ActionApi;
import de.mhus.osgi.sop.api.action.ActionDescriptor;
import de.mhus.osgi.sop.api.operation.JmsOperationApi;
import de.mhus.osgi.sop.api.operation.OperationApi;

@Command(scope = "sop", name = "action", description = "Action commands")
@Service
public class ActionCmd implements Action {

	@Argument(index=0, name="cmd", required=true, description="Command list", multiValued=false)
	String cmd;
	
	@Argument(index=1, name="path", required=false, description="Path to Operation", multiValued=false)
    String path;
	
	@Argument(index=2, name="parameters", required=false, description="More Parameters", multiValued=true)
    String[] parameters;
	
	@Override
	public Object execute() throws Exception {

		if (cmd.equals("list")) {
			ConsoleTable out = new ConsoleTable();
			out.setHeaderValues("Source","Name","Tags", "Class");
			for ( ActionDescriptor a : ActionApiImpl.instance.getActions()) {
				out.addRowValues(a.getSource(), a.getName(), a.getTags(), a.getAction().getClass().getCanonicalName());
			}
			out.print(System.out);
		}
		return null;
	}

	
}
