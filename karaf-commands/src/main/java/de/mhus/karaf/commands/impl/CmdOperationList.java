package de.mhus.karaf.commands.impl;

import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.lib.core.M;
import de.mhus.lib.core.console.ConsoleTable;
import de.mhus.lib.core.operation.Operation;
import de.mhus.lib.core.operation.OperationDescription;
import de.mhus.osgi.api.karaf.AbstractCmd;
import de.mhus.osgi.api.services.OperationManager;

@Command(
        scope = "mhus",
        name = "operastion-list",
        description = "List all local operation")
@Service
public class CmdOperationList extends AbstractCmd {

	@Override
	public Object execute2() throws Exception {
		ConsoleTable out = new ConsoleTable(tblOpt);
		out.setHeaderValues("Path","Version","Caption","Class","Labels");
		for (Operation oper : M.l(OperationManager.class).getOperations()) {
			OperationDescription desc = oper.getDescription();
			
			if (desc == null)
				out.addRowValues("?","?","?",oper.getClass().getCanonicalName(), "");
			else
				out.addRowValues(desc.getPath(),desc.getVersionString(),desc.getCaption(),oper.getClass().getCanonicalName(),desc.getLabels());
		}
		out.print();
		return null;
	}

}
