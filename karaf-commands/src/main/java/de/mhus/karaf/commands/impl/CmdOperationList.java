package de.mhus.karaf.commands.impl;

import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.lib.core.IReadProperties;
import de.mhus.lib.core.M;
import de.mhus.lib.core.MProperties;
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


    @Option(
            name = "-a",
            aliases = "--all",
            description = "Print all labels",
            required = false)
    private boolean full;

	@Override
	public Object execute2() throws Exception {
		ConsoleTable out = new ConsoleTable(tblOpt);
		out.setHeaderValues("PathVersion","Caption","Class","Labels", "Uuid");
		for (Operation oper : M.l(OperationManager.class).getOperations()) {
			OperationDescription desc = oper.getDescription();
			
			if (desc == null)
				out.addRowValues("?","?","?",oper.getClass().getCanonicalName(), "");
			else
				out.addRowValues(
						desc.getPathVersion(),
						desc.getCaption(),
						oper.getClass().getCanonicalName(),
						reduceLabels(desc.getLabels()),
						desc.getUuid());
		}
		out.print();
		return null;
	}

	private Object reduceLabels(IReadProperties labels) {
		if (full) return labels;
		MProperties p = new MProperties(labels);
		p.keys().removeIf(k -> k.startsWith("@") || k.startsWith("_") );
		return p;
	}

}
