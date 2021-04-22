package de.mhus.karaf.commands.impl;

import java.util.Map.Entry;

import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.osgi.framework.FrameworkUtil;

import de.mhus.lib.core.M;
import de.mhus.lib.core.operation.Operation;
import de.mhus.lib.core.operation.OperationDescription;
import de.mhus.osgi.api.MOsgi;
import de.mhus.osgi.api.karaf.AbstractCmd;
import de.mhus.osgi.api.services.OperationManager;

@Command(
        scope = "mhus",
        name = "operation-info",
        description = "Show operation information")
@Service
public class CmdOperationInfo extends AbstractCmd {

    @Argument(
            index = 0,
            name = "id/pathVersion",
            required = true,
            description = "Ident of the operation",
            multiValued = false)
    String name;

	@Override
	public Object execute2() throws Exception {
		
		OperationManager api = M.l(OperationManager.class);
		if (api == null) {
			System.out.println("OperationManager not found");
			return null;
		}
		Operation oper = api.getOperation(name);
		if (oper == null) {
			System.out.println("Operation not found");
			return null;
		}
		OperationDescription desc = oper.getDescription();
		if (desc == null)
			System.out.println("Operation has no description");
		else {
			System.out.println("Name      : " + desc.getPathVersion());
			System.out.println("Path      : " + desc.getPath());
			System.out.println("Version   : " + desc.getVersion());
			System.out.println("Caption   : " + desc.getCaption());
			System.out.println("Title     : " + desc.getTitle());
			System.out.println("Id        : " + desc.getUuid());
			System.out.println("Parameters: " + desc.getParameterDefinitions());
			System.out.println("Form      : " + desc.getForm());
			System.out.println("Labels:");
			for (Entry<String, Object> label : desc.getLabels().entrySet())
				System.out.println("  " + label.getKey() + "=" + label.getValue());
		}
			System.out.println("Class     : " + oper.getClass().getCanonicalName());
			System.out.println("Bundle    : " + MOsgi.getBundleCaption( FrameworkUtil.getBundle(oper.getClass()) ));

		return oper;
	}

}
