package de.mhus.osgi.sop.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;

import org.codehaus.jackson.node.ObjectNode;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
import de.mhus.lib.adb.DbCollection;
import de.mhus.lib.adb.DbManager;
import de.mhus.lib.adb.DbSchema;
import de.mhus.lib.adb.Persistable;
import de.mhus.lib.adb.query.AQuery;
import de.mhus.lib.adb.query.Db;
import de.mhus.lib.core.IProperties;
import de.mhus.lib.core.MCollection;
import de.mhus.lib.core.MConstants;
import de.mhus.lib.core.MLog;
import de.mhus.lib.core.MPassword;
import de.mhus.lib.core.MString;
import de.mhus.lib.core.MTimeInterval;
import de.mhus.lib.core.cfg.CfgProperties;
import de.mhus.lib.core.pojo.MPojo;
import de.mhus.lib.core.security.Account;
import de.mhus.lib.core.security.AccountSource;
import de.mhus.lib.core.security.AuthorizationSource;
import de.mhus.lib.errors.AccessDeniedException;
import de.mhus.lib.errors.MException;
import de.mhus.osgi.sop.api.Sop;
import de.mhus.osgi.sop.api.SopApi;
import de.mhus.osgi.sop.api.aaa.AaaContext;
import de.mhus.osgi.sop.api.aaa.ContextCachedItem;
import de.mhus.osgi.sop.api.aaa.Trust;
import de.mhus.osgi.sop.api.aaa.TrustSource;
import de.mhus.osgi.sop.api.action.ActionApi;
import de.mhus.osgi.sop.api.action.BpmCase;
import de.mhus.osgi.sop.api.adb.DbSchemaService;
import de.mhus.osgi.sop.api.adb.Reference;
import de.mhus.osgi.sop.api.adb.ReferenceCollector;
import de.mhus.osgi.sop.api.adb.Reference.TYPE;
import de.mhus.osgi.sop.api.model.ActionTask;
import de.mhus.osgi.sop.api.model.DbMetadata;
import de.mhus.osgi.sop.api.model.ObjectParameter;
import de.mhus.osgi.sop.api.rest.AbstractNode;
import de.mhus.osgi.sop.api.rest.CallContext;
import de.mhus.osgi.sop.api.rest.JsonResult;
import de.mhus.osgi.sop.api.rest.RestResult;
import de.mhus.osgi.sop.api.rest.RestUtil;
import de.mhus.osgi.sop.api.util.TicketUtil;
import de.mhus.osgi.sop.impl.adb.SopDbImpl;

@Component(immediate=true,provide=SopApi.class,name="SopApi")
public class SopApiImpl extends MLog implements SopApi {
	

	@SuppressWarnings("unused")
	private BundleContext context;
		
	private CfgProperties config = new CfgProperties(SopApi.class, "sop");
	
	@Activate
	public void doActivate(ComponentContext ctx) {
		context = ctx.getBundleContext();
		// TODO set synchronizer
	}
	
	@Deactivate
	public void doDeactivate(ComponentContext ctx) {
		context = null;
	}
	
	@Override
	public IProperties getMainConfiguration() {
		return config.value();
	}
	
}