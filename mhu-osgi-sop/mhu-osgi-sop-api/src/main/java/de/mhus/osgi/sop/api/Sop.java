package de.mhus.osgi.sop.api;

import de.mhus.lib.core.MCast;
import de.mhus.lib.core.MString;
import de.mhus.lib.jms.JmsConnection;
import de.mhus.lib.karaf.jms.JmsUtil;

public class Sop {

	public static final String PARAMETERS = "parameter.";

	public static final String DEFAULT_GROUP = "default";

	public static final String PARAM_OPERATION_PATH = "path";
	public static final String OPERATION_LIST = "_list";
	public static final String OPERATION_INFO = "_get";
	public static final String PARAM_OPERATION_ID = "id";

	public static final String PARAM_AAA_TICKET = "_aaa_ticket";

	public static final long MAX_MSG_BYTES = 1024 * 1024 * 100; // 100 MB

	public static final String PARAM_SUCCESSFUL = "successful";

	public static final String PARAM_MSG = "msg";

	public static final String PARAM_RC = "rc";
	
/* use MApi.lookup() instead

	private static HashMap<String, Container> apiCache = new HashMap<>();

	public synchronized static <T extends SApi> T getApi(Class<? extends T> ifc) {
		return getApi(ifc, true);
	}
	
	@SuppressWarnings("unchecked")
	public synchronized static <T extends SApi> T getApi(Class<? extends T> ifc, boolean throwException) {
		Container cached = apiCache.get(ifc.getCanonicalName());
		if (cached != null) {
			if (cached.bundle.getState() != Bundle.ACTIVE || cached.modified != cached.bundle.getLastModified()) {
				apiCache.remove(cached.ifc.getCanonicalName());
				cached = null;
			}
		}
		
		if (cached == null) {
			BundleContext context = FrameworkUtil.getBundle(Sop.class).getBundleContext();
			ServiceReference<? extends T> ref = context.getServiceReference(ifc);
			if (ref == null) {
				if (throwException)
					throw new NotFoundException("api reference not found",ifc);
				else
					return null;
			}
			T obj = context.getService(ref);
			if (obj == null) {
				if (throwException)
					throw new NotFoundException("api service not found",ifc);
				else
					return null;
			}
			cached = new Container();
			cached.bundle = ref.getBundle();
			cached.api = obj;
			cached.ifc = ifc;
			cached.modified = cached.bundle.getLastModified();
			apiCache.put(ifc.getCanonicalName(), cached);
		}
		return (T) cached.api;
	}
	
	private static class Container {

		public long modified;
		public Class<? extends SApi> ifc;
		public SApi api;
		public Bundle bundle;
		
	}
*/	
	public static String decodePassword(String password) {
		if (password == null) return null;
		if (password.length() < 2 || !password.startsWith("`")) return password;
		if (password.charAt(1) == 'A') {
			StringBuffer out = new StringBuffer();
			for (int i = 2; i < password.length(); i++) {
				char c = password.charAt(i);
				switch (c) {
				case '0': c = '9'; break;
				case '1': c = '0'; break;
				case '2': c = '1'; break;
				case '3': c = '2'; break;
				case '4': c = '3'; break;
				case '5': c = '4'; break;
				case '6': c = '5'; break;
				case '7': c = '6'; break;
				case '8': c = '7'; break;
				case '9': c = '8'; break;
				}
				out.append(c);
			}
			return out.toString();
		}
		return null;
	}

	public static String asEncodePassword(String alreadyEncoded) {
		return "`A" +alreadyEncoded;
	}

	public static int getPageFromSearch(String search) {
		if (MString.isEmpty(search) || !search.startsWith("page:"))
			return 0;
		search = search.substring(5);
		if (search.indexOf(',') >= 0)
			return MCast.toint(MString.beforeIndex(search, ','), 0);
		return MCast.toint(search, 0);
	}

	public static String getFilterFromSearch(String search) {
		if (MString.isEmpty(search))
			return null;
		if (search.startsWith("page:")) {
			if (search.indexOf(',') >= 0)
				return MString.afterIndex(search, ',');
			return null;
		}
		return search;
	}

	public static JmsConnection getDefaultJmsConnection() {
		return JmsUtil.getConnection(getDefaultJmsConnectionName());
	}

	public static String getDefaultJmsConnectionName() {
		return "mhus"; //TODO configurable
	}

/* use MApi.waitFor() instead

	public static <T extends Object> T waitForApi(Class<? extends T> ifc, long timeout) {
		long start = System.currentTimeMillis();
		while (true) {
			try {
				T api = MApi.lookup(ifc);
				if (api != null) // should not happen
					return api;
			} catch (Throwable t) {}
			if (System.currentTimeMillis() - start > timeout)
				throw new TimeoutRuntimeException("timeout getting API",ifc);
			MThread.sleep(500);
		}
	}
*/
}
