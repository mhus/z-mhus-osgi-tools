package de.mhus.osgi.api.karaf;

import java.util.LinkedList;
import java.util.List;

import org.apache.karaf.shell.api.console.Session;

public class CmdInterceptorUtil {

    public static final String SESSION_KEY = "_de.mhus.osgi.api.karaf.CmdInterceptors";

//    public void addInterceptor(Session session, CmdInterceptor interceptor) {
//        @SuppressWarnings("unchecked")
//        List<CmdInterceptor> interceptors = (List<CmdInterceptor>) session.get(SESSION_KEY);
//        if (interceptors == null) {
//            interceptors = new LinkedList<>();
//            session.put(SESSION_KEY, interceptors);
//        }
//        interceptors.add(interceptor);
//    }
    
    public static void setInterceptor(Session session, CmdInterceptor interceptor) {
        @SuppressWarnings("unchecked")
        List<CmdInterceptor> interceptors = (List<CmdInterceptor>) session.get(SESSION_KEY);
        if (interceptors == null) {
            interceptors = new LinkedList<>();
            session.put(SESSION_KEY, interceptors);
        } else {
            interceptors.removeIf(i -> i.getClass().getCanonicalName().equals(interceptor.getClass().getCanonicalName()) );
        }
        interceptors.add(interceptor);
    }
    
    @SuppressWarnings("unchecked")
    public static <T extends CmdInterceptor> T getInterceptor(Session session, Class<?> clazz) {
        List<CmdInterceptor> interceptors = (List<CmdInterceptor>) session.get(SESSION_KEY);
        if (interceptors == null) return null;
        for (CmdInterceptor interceptor : interceptors) {
            if (interceptor.getClass().getCanonicalName().equals(clazz.getCanonicalName()))
                return (T)interceptor;
        }
        return null;
    }
    
}
