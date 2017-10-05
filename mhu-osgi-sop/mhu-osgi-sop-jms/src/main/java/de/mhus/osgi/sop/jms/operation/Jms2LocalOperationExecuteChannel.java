package de.mhus.osgi.sop.jms.operation;

import java.util.LinkedList;
import java.util.List;

import org.osgi.service.component.ComponentContext;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
import aQute.bnd.annotation.component.Reference;
import de.mhus.lib.core.IProperties;
import de.mhus.lib.core.MApi;
import de.mhus.lib.core.cfg.CfgString;
import de.mhus.lib.core.service.ServerIdent;
import de.mhus.lib.core.strategy.Operation;
import de.mhus.lib.core.strategy.OperationDescription;
import de.mhus.lib.core.strategy.OperationResult;
import de.mhus.lib.karaf.jms.JmsDataChannel;
import de.mhus.lib.karaf.jms.JmsManagerService;
import de.mhus.osgi.sop.api.jms.AbstractJmsOperationExecuteChannel;
import de.mhus.osgi.sop.api.jms.TicketAccessInterceptor;
import de.mhus.osgi.sop.api.operation.LocalOperationApi;

@Component(provide=JmsDataChannel.class,immediate=true)
public class Jms2LocalOperationExecuteChannel extends AbstractJmsOperationExecuteChannel {

	public static CfgString queueName = new CfgString(Jms2LocalOperationExecuteChannel.class, "queue", "sop.operation." + MApi.lookup(ServerIdent.class));
	public static CfgString connectionName = new CfgString(Jms2LocalOperationExecuteChannel.class, "connection", "sop");
	static Jms2LocalOperationExecuteChannel instance;
	
	@Override
	@Activate
	public void doActivate(ComponentContext ctx) {
		super.doActivate(ctx);
		if (MApi.getCfg(Jms2LocalOperationExecuteChannel.class).getBoolean("accessControl", true))
			getServer().setInterceptorIn(new TicketAccessInterceptor());
		instance = this;
	}	
	
	@Override
	@Deactivate
	public void doDeactivate(ComponentContext ctx) {
		instance = null;
		super.doDeactivate(ctx);
	}
	
	@Override
	@Reference
	public void setJmsManagerService(JmsManagerService manager) {
		super.setJmsManagerService(manager);
	}

	@Override
	protected void doAfterReset() {
		if (getServer() != null && getServer().getInterceptorIn() == null)
			getServer().setInterceptorIn(new TicketAccessInterceptor()); // for authentication
	}

	@Override
	protected String getQueueName() {
		return  queueName.value();
	}

	@Override
	protected String getJmsConnectionName() {
		return connectionName.value(); 
	}

	@Override
	protected OperationResult doExecute(String path, IProperties properties) {

		log().d("execute operation",path,properties);
		
		LocalOperationApi admin = MApi.lookup(LocalOperationApi.class);
		OperationResult res = admin.doExecute(path, properties);
		
		log().d("operation result",path,res, res == null ? "" : res.getResult());
		return res;
	}

	@Override
	protected List<String> getPublicOperations() {
		LocalOperationApi admin = MApi.lookup(LocalOperationApi.class);
		LinkedList<String> out = new LinkedList<String>();
		for (String path : admin.getOperations()) {
			try {
				Operation oper = admin.getOperation(path).getOperation();
				if (oper.hasAccess())
					out.add(path);
			} catch (Throwable t) {
				log().d(path,t);
			}
		}
			
		return out;
	}

	@Override
	protected OperationDescription getOperationDescription(String path) {
		LocalOperationApi admin = MApi.lookup(LocalOperationApi.class);
		Operation oper = admin.getOperation(path).getOperation();
		if (!oper.hasAccess()) return null;
		return oper.getDescription();
	}

}
