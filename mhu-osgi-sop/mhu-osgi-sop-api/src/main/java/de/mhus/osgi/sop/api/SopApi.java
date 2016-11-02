package de.mhus.osgi.sop.api;

import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import de.mhus.lib.adb.DbManager;
import de.mhus.lib.adb.query.AQuery;
import de.mhus.lib.core.IProperties;
import de.mhus.lib.core.security.Account;
import de.mhus.lib.errors.MException;
import de.mhus.osgi.sop.api.aaa.AaaContext;
import de.mhus.osgi.sop.api.aaa.Trust;
import de.mhus.osgi.sop.api.adb.DbSchemaService;
import de.mhus.osgi.sop.api.adb.ReferenceCollector;
import de.mhus.osgi.sop.api.model.ActionTask;
import de.mhus.osgi.sop.api.model.DbMetadata;
import de.mhus.osgi.sop.api.model.ObjectParameter;
import de.mhus.osgi.sop.api.rest.CallContext;
import de.mhus.osgi.sop.api.rest.RestResult;

public interface SopApi extends SApi {

	IProperties getMainConfiguration();

}
