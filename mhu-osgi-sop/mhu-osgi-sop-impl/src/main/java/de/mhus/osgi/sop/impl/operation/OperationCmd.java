package de.mhus.osgi.sop.impl.operation;

import java.util.List;
import java.util.Map;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.lib.core.IProperties;
import de.mhus.lib.core.MApi;
import de.mhus.lib.core.MProperties;
import de.mhus.lib.core.MString;
import de.mhus.lib.core.strategy.Operation;
import de.mhus.lib.core.strategy.OperationDescription;
import de.mhus.lib.core.strategy.OperationResult;
import de.mhus.lib.jms.JmsConnection;
import de.mhus.lib.karaf.jms.JmsUtil;
import de.mhus.osgi.sop.api.Sop;
import de.mhus.osgi.sop.api.aaa.AaaContext;
import de.mhus.osgi.sop.api.aaa.AccessApi;
import de.mhus.osgi.sop.api.operation.JmsOperationApi;
import de.mhus.osgi.sop.api.operation.LocalOperationApi;

@Command(scope = "sop", name = "operation", description = "Operation commands")
@Service
public class OperationCmd implements Action {

	@Argument(index=0, name="cmd", required=true, description="Command list, action, info <path>, execute <path> [key=value]*, search", multiValued=false)
	String cmd;
	
	@Argument(index=1, name="path", required=false, description="Path to Operation", multiValued=false)
    String path;
	
	@Argument(index=2, name="parameters", required=false, description="More Parameters", multiValued=true)
    String[] parameters;

	@Option(name="-c", aliases="--connection", description="JMS Connection Name",required=false)
	String conName = null;

	@Option(name="-q", aliases="--queue", description="JMS Connection Queue OperationChannel",required=false)
	String queueName = null;
	
	@Override
	public Object execute() throws Exception {

		JmsConnection con = Sop.getDefaultJmsConnection();
		if (conName != null)
			con = JmsUtil.getConnection(conName);
		
		AaaContext acc = MApi.lookup(AccessApi.class).getCurrentOrGuest();
		
		LocalOperationApi api = MApi.lookup(LocalOperationApi.class);
		JmsOperationApi jms = MApi.lookup(JmsOperationApi.class);
		
		if (cmd.equals("list")) {
			if (MString.isEmpty(path) && MString.isEmpty(queueName)) {
				for (String path : api.getOperations()) {
					System.out.println(path);
				}
			} else {
				if (MString.isSet(path)) queueName = path;
				List<String> list = jms.doGetOperationList(con, queueName, acc);
				if (list != null) {
					for (String item : list)
						System.out.println(item);
					System.out.println("OK");
				} else {
					System.out.println("ERROR");
				}
			}
		} else
//		if (cmd.equals("action")) {
//			ConsoleTable table = new ConsoleTable();
//			table.setHeaderValues("Name","Register name", "Service Class");
//			for (OperationBpmDefinition def : api.getActionDefinitions()) {
//				table.addRowValues(def.getName(), def.getRegisterName(), def.getServiceClass());
//			}
//			table.print(System.out);
//		}
		if (cmd.equals("info")) {
			
			if (MString.isEmpty(queueName)) {
				Operation oper = api.getOperation(path).getOperation();
				if (oper == null) {
					System.out.println("Operation not found");
				} else {
					OperationDescription des = oper.getDescription();
					System.out.println("Description  : " + des);
					System.out.println("Form         : " + des.getForm());
//				System.out.println("BpmDefinition: " + oper.get);
				}
			} else {
				IProperties pa = new MProperties();
				pa.setString("id", path);
				OperationResult ret = jms.doExecuteOperation(con, queueName, "_get", pa, acc, JmsOperationApi.OPT_NEED_ANSWER);
				if (ret.isSuccessful()) {
					Object res = ret.getResult();
					if (res != null && res instanceof Map<?,?>) {
						Map<?, ?> map = (Map<?,?>)res;
						System.out.println("Description  : " + map.get("group") + "," + map.get("id"));
						System.out.println("Form         : " + map.get("form") );
					} else {
						System.out.println("Result not a map");
					}
				} else {
					System.out.println("ERROR " + ret.getMsg());
				}
			}
		} else
		if (cmd.equals("execute")) {
			MProperties properties = MProperties.explodeToMProperties(parameters);
			OperationResult res = api.doExecute(path, properties);
			System.out.println("Result: "+res);
			System.out.println("RC: " + res.getReturnCode());
			System.out.println("Object: " + res.getResult());
		} else
		if (cmd.equals("search")) {
			for (String name : jms.lookupOperationQueues())
				System.out.println("Queue: " + name);
			System.out.println("OK");
		} else {
			System.out.println("Command not found");
		}
		return null;
	}

	
}
