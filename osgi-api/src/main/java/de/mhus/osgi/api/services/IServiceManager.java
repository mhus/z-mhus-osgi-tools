package de.mhus.osgi.api.services;

import java.util.List;

public interface IServiceManager {

    boolean create(String implClass, String bundleName) throws Exception;

    boolean delete(String implClass);

    List<String> list();

}
