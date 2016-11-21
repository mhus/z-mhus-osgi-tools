package de.mhus.osgi.sop.api.action;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.mhus.lib.annotations.adb.DbPersistent;
import de.mhus.lib.core.MCast;
import de.mhus.lib.core.MDate;
import de.mhus.lib.core.MSystem;
import de.mhus.lib.errors.MException;
import de.mhus.osgi.sop.api.model.DbMetadata;

public class BpmCase extends DbMetadata {

	public enum STATUS {NEW,PROGRESS,CLOSED,ERROR}
	
	@DbPersistent
	private long bpmId;
	@DbPersistent(ro=true)
	private String process;
	@DbPersistent(ro=true)
	private String mapped;
	@DbPersistent
	private STATUS status;
	@DbPersistent
	private String msg;
	@DbPersistent(ro=true)
	private String customId;
	@DbPersistent
	private Date lastSync;
	@DbPersistent
	private HashMap<String, Object> parameters;
	@DbPersistent
	private HashMap<String, Object> initial;
	@DbPersistent
	private long statusCode;
	@DbPersistent
	private LinkedList<String> comments;
	
	public BpmCase() {}

	
	public BpmCase(long bpmId, String process, String mapped, String customId, Map<String, Object> parameters) {
		super();
		this.bpmId = bpmId;
		this.process = process;
		this.mapped = mapped;
		this.customId = customId;
		this.status = STATUS.NEW;
		this.initial = new HashMap<String, Object>(parameters);
	}


	public long getBpmId() {
		return bpmId;
	}

	public String getProcess() {
		return process;
	}

	public String getMappedProcess() {
		return mapped;
	}
	
	public STATUS getStatus() {
		return status;
	}

	public String getMsg() {
		return msg;
	}

	public void setStatus(STATUS status) {
		this.status = status;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public String getCustomId() {
		return customId;
	}

	public Date getLastSync() {
		return lastSync;
	}


	public void setLastSync(Date lastSync) {
		this.lastSync = lastSync;
	}


	public HashMap<String, Object> getParameters() {
		return parameters;
	}


	public void setParameters(HashMap<String, Object> parameters) {
		this.parameters = parameters;
	}


	public HashMap<String, Object> getInitial() {
		return initial;
	}


	public void setInitial(HashMap<String, Object> initial) {
		this.initial = initial;
	}

	public String toString() {
		return MSystem.toString(this, getId(), process, bpmId );
	}


	public void setBpmId(long bpmId) {
		this.bpmId = bpmId;
	}
	
	@Override
	public DbMetadata findParentObject() throws MException {
		return null;
	}


	public long getStatusCode() {
		return statusCode;
	}


	public void setStatusCode(long statusCode) {
		this.statusCode = statusCode;
	}


	public List<String> getComments() {
		if (comments == null) comments = new LinkedList<String>();
		return Collections.unmodifiableList(comments);
	}	

	public void appendComment(Object source, Object ... msg) {
		String sourceName = null;
		if (source == null)
			sourceName = "?";
		else
		if (source instanceof Class<?>)
			sourceName = ((Class<?>)source).getCanonicalName();
		else
		if (source instanceof String)
			sourceName = (String)source;
		else
			sourceName = source.getClass().getCanonicalName();

		StringBuffer m = new StringBuffer().append(MDate.toIso8601(new Date())).append('|').append(sourceName);
		if (msg != null) {
			for (Object x : msg)
				if (x != null) m.append('|').append(MCast.toString(x));
		}
		if (comments == null) comments = new LinkedList<String>();
		comments.add(m.toString());
	}


	public void clearComments() {
		if (comments == null) return;
		comments.clear();
		setDbHandler(null);
	}

}
