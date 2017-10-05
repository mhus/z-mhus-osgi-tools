package de.mhus.osgi.sop.impl.action;

import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.Map.Entry;

import de.mhus.lib.adb.DbManager;
import de.mhus.lib.adb.query.Db;
import de.mhus.lib.basics.Versioned;
import de.mhus.lib.core.IProperties;
import de.mhus.lib.core.MApi;
import de.mhus.lib.core.MLog;
import de.mhus.lib.core.MProperties;
import de.mhus.lib.core.MTimeInterval;
import de.mhus.lib.core.MValidator;
import de.mhus.lib.core.cfg.CfgLong;
import de.mhus.lib.core.strategy.Monitor;
import de.mhus.lib.core.strategy.OperationResult;
import de.mhus.lib.core.strategy.Successful;
import de.mhus.lib.core.util.ParameterDefinition;
import de.mhus.lib.core.util.ParameterDefinitions;
import de.mhus.lib.errors.MException;
import de.mhus.osgi.sop.api.action.Action;
import de.mhus.osgi.sop.api.action.ActionDescriptor;
import de.mhus.osgi.sop.api.action.ActionProvider;
import de.mhus.osgi.sop.api.adb.AdbApi;
import de.mhus.osgi.sop.api.model.BpmCase;
import de.mhus.osgi.sop.api.model.BpmDefinition;

public abstract class BpmActionProvider extends MLog implements ActionProvider {

	private CfgLong updateInterval = new CfgLong(BpmActionProvider.class, "updateInterval", MTimeInterval.MINUTE_IN_MILLISECOUNDS * 10 );

	@Override
	public <I> I adaptTo(Class<? extends Object> ifc) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Return the unique name of the provider
	 */
	@Override
	public abstract String getName();

	@Override
	public Collection<ActionDescriptor> getActions() {

		LinkedList<ActionDescriptor> out = new LinkedList<ActionDescriptor>();
		try {
			for (BpmDefinition def : MApi.lookup(AdbApi.class).getManager().getByQualification( Db.query(BpmDefinition.class).eq(BpmDefinition::getProcessor, getName()) )) {
				LinkedList<String> tags = new LinkedList<>();
				String tagsStr = def.getOptions().getString("tags",null);
				if (tagsStr != null) {
					for (String t : tagsStr.split(","))
						tags.add(t);
				}

				ParameterDefinitions pDefs = ParameterDefinitions.create(def.getParameters());
				out.add(new ActionDescriptor(
						new BpmAction(def), 
						tags, getName(), 
						def.getProcess(), 
						Versioned.DEFAULT_VERSION,
						pDefs, 
						null, 
						def, 
						getName() 
					));
			}
		} catch (Throwable t) {
			log().e(t);
		}
		
		return out;
	}

	private class BpmAction implements Action {

		private BpmDefinition def;

		public BpmAction(BpmDefinition def) {
			this.def = def;
		}

		@Override
		public <I> I adaptTo(Class<? extends Object> ifc) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getName() {
			return def.getProcess();
		}

		@Override
		public boolean canExecute(IProperties properties) {
			return true;
		}

		@Override
		public OperationResult doExecute(IProperties properties, Monitor monitor) throws Exception {

			String process = def.getProcess();
			String mapped = def.getOptions().getString("processMapping", process);
			
			MProperties parameters = new MProperties();
			String customId = properties.getString("_customId", "");
			
			DbManager manager = MApi.lookup(AdbApi.class).getManager();
			BpmCase caze = manager.inject(new BpmCase(null, BpmActionProvider.this.getName(), process, mapped, customId, parameters));
			caze.save();

			ParameterDefinitions pDefs = ParameterDefinitions.create(def.getParameters());
			
			for (Entry<String, ParameterDefinition> pDefEntry : pDefs.entrySet()) {
				String fromName = pDefEntry.getKey();
				ParameterDefinition pDef = pDefEntry.getValue();
				String toName = pDef.getMapping() == null ? fromName : pDef.getMapping();
				
				Object value = properties.get(fromName);
				value = pDef.transform(value);
				
				parameters.put(toName, value);
			}
				
			String id = createCase(mapped, parameters);
			caze.setBpmId(id);
			caze.save();
			sync(id, true);
			
			return new Successful(caze.getProcess(), "ok", 0, caze);
		}
		
	}

	/**
	 * Create a process with the given parameters and return the caseId
	 * 
	 * @param process Process name
	 * @param parameters Parameter set
	 * @return The id of the created process
	 * @throws Exception 
	 */
	public abstract String createCase(String process, MProperties parameters) throws Exception;

	public boolean sync(String id, boolean forced) {
		try {
			BpmCase caze = getCase(id);
			if (caze == null) return false;
		
			Date last = caze.getLastSync();
			if (!forced && last != null && System.currentTimeMillis() - last.getTime() < updateInterval.value()  )
				return false;

			if (syncCase(caze)) {
				caze.setLastSync(new Date());
				caze.save();
				return true;
			}
			return false;
			
		} catch (Throwable t) {
			log().d(t);
			return false;
		}
	}

	/**
	 * Synchronize the case with the remote BPM. Do not save the case object
	 * 
	 * @param caze
	 * @return true if sync was successful
	 */
	protected abstract boolean syncCase(BpmCase caze);

	public BpmCase getCase(String id) throws MException {
		DbManager manager = MApi.lookup(AdbApi.class).getManager();
		if (MValidator.isUUID(id))
			return manager.getObject(BpmCase.class, id);
		else
			return manager.getObjectByQualification((Db.query(BpmCase.class).eq(BpmCase::getProcessor, getName()).eq(BpmCase::getBpmId, id) ) );
	}
	
}
