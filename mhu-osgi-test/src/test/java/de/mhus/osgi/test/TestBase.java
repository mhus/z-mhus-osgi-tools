/*
 *    Copyright 2015 Achim Nierbeck
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package de.mhus.osgi.test;

import static org.ops4j.pax.exam.CoreOptions.junitBundles;
import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.MavenUtils.asInProject;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.configureConsole;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.features;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.karafDistributionConfiguration;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.keepRuntimeFolder;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.logLevel;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.inject.Inject;

import org.apache.karaf.features.BootFinished;
import org.apache.karaf.features.FeaturesService;
import org.apache.karaf.shell.api.console.Session;
import org.apache.karaf.shell.api.console.SessionFactory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.karaf.options.KarafDistributionOption;
import org.ops4j.pax.exam.karaf.options.LogLevelOption.LogLevel;
import org.ops4j.pax.exam.options.MavenArtifactUrlReference;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// https://ops4j1.jira.com/wiki/spaces/PAXEXAM4/pages/54263875/Karaf+Test+Container+Reference

public class TestBase extends org.junit.Assert {

	protected LinkedList<HashMap<String,Object>> results = new LinkedList<>();
	
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected BundleContext bc;

    @Inject
    protected FeaturesService featuresService;

    /**
     * To make sure the tests run only when the boot features are fully
     * installed
     */
    @Inject
    protected BootFinished bootFinished;

    @Inject
    protected SessionFactory sessionFactory;

    protected ExecutorService executor = Executors.newCachedThreadPool();

    protected ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    protected PrintStream printStream = new PrintStream(byteArrayOutputStream);
    protected PrintStream errStream = new PrintStream(byteArrayOutputStream);
    protected Session session;

    @Configuration
    public Option[] config() {
        return options(
                karafDistributionConfiguration().frameworkUrl(mvnKarafDist())
                        .unpackDirectory(new File("target/paxexam/unpack/")).useDeployFolder(false).runEmbedded(false),
                logLevel(LogLevel.INFO), keepRuntimeFolder(),
                KarafDistributionOption.replaceConfigurationFile("etc/org.apache.karaf.management.cfg", new File("src/test/resources/org.apache.karaf.management.cfg")),
                KarafDistributionOption.replaceConfigurationFile("etc/org.apache.karaf.shell.cfg", new File("src/test/resources/org.apache.karaf.shell.cfg")),
                configureConsole().ignoreLocalConsole(),
                configureConsole().ignoreRemoteShell(),
                junitBundles(),
                features(
                		maven().groupId("de.mhus.osgi").artifactId("mhu-karaf-feature").type("xml")
                		.classifier("features").versionAsInProject()
                		,"") // mhu-osgi-test
                );
    }

    private MavenArtifactUrlReference mvnKarafDist() {
        return maven().groupId("org.apache.karaf").artifactId("apache-karaf").type("tar.gz").version(asInProject());
    }

    @Before
    public void setUpITestBase() throws Exception {
    	System.out.println("************ Create Session");
        if (sessionFactory != null)
        	session = sessionFactory.create(System.in, printStream, errStream);
                
    }

    protected void startKarafTests(final String features, int timeoutSec) throws IOException, InterruptedException, ExecutionException, TimeoutException {
    	
        FutureTask<String> commandFuture = new FutureTask<String>(new Callable<String>() {
            @Override
			public String call() {
				try {
					System.out.println("************** install Custom Features");
					if (features != null) {
						for (String f : features.split(","))
							featuresService.installFeature(f);
					}
					bc = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
					System.out.println("Bundle: " + bc.getBundle().getSymbolicName() + " " + bc.getBundle().getVersion());
//					bc.installBundle("file:" + new File( getTargetDirectory(), "mhu-osgi-test-1.4.1-SNAPSHOT.jar").getAbsolutePath() );
					
					for (Method method : TestBase.this.getClass().getMethods()) {
						if (method.getName().startsWith("karaf")) {
							HashMap<String,Object> result = new HashMap<>();
							result.put("name", method.getName().substring(5));
							
							results.add(result);
							byteArrayOutputStream.flush();
							byteArrayOutputStream.reset();
							System.out.println("===========================================");
							System.out.println(" " + method.getName());
							System.out.println("===========================================");
							try {
								method.invoke(TestBase.this);
							} catch (Throwable t) {
								t.printStackTrace();
								result.put("error", toStr(t));
							}
			                printStream.flush();
			                errStream.flush();
			                String out = byteArrayOutputStream.toString();
			                result.put("output",out);
			                System.out.println(out);
						}
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
                printStream.flush();
                errStream.flush();
                
                return resultsToString();
            }
        });

		executor.submit(commandFuture);
        String response = commandFuture.get(timeoutSec, TimeUnit.SECONDS);
        System.out.println(">>>");
        System.out.println(response);
        System.out.println("<<<");
        
        System.out.println(new File(".").getAbsolutePath());
        
        String errors = "";
        System.out.println();
        System.out.println(" Test                                    | Status");
        System.out.println("----------------------------------------------------");
        for (String part : response.split(" ")) {
        	int pos = part.indexOf('=');
        	String name = part.substring(0, pos);
        	String state = part.substring(pos+1);
        	
        	System.out.print(name);
        	for (int i = name.length(); i < 40; i++) System.out.print(" ");
        	System.out.println(" | " + state);
        	if (state.equals("error")) {
        		if (errors.length() > 0) errors+=",";
        		errors+=name;
        	}
        }
        System.out.println();
        if (errors.length() > 0)
    		throw new IOException("Test in error: " + errors);

	}

	protected String resultsToString() {
		StringBuilder out = new StringBuilder();
		File dir = new File(getTargetDirectory(), "results");
		dir.mkdirs();
		
		for (HashMap<String, Object> result : results) {
			String name = (String)result.get("name");
			name = normalize(name);
			{
				File file = new File(dir, name + ".out");
				if (result.containsKey("output"))
					writeFile(file, (String)result.get("output"));
				else
				if (file.exists())
					file.delete();
			}
			{
				File file = new File(dir, name + ".err");
				if (result.containsKey("error"))
					writeFile(file, (String)result.get("error"));
				else
				if (file.exists())
					file.delete();
			}
			if (out.length() > 0) out.append(" ");
			out.append(name).append("=").append(result.containsKey("error") ? "error" : "ok");
		}
		return out.toString();
	}

	@After
    public void cleanupTest() throws Exception {
        session = null;
    }

    @AfterClass
    public static void tearDown() throws Exception {
        System.out.println("Removing data directory");
        File dataDir = new File("data");
        dataDir.delete();
    }

    protected String executeCommand(final String command) throws IOException {
        byteArrayOutputStream.flush();
        byteArrayOutputStream.reset();

        String response;
        FutureTask<String> commandFuture = new FutureTask<String>(new Callable<String>() {
            @Override
			public String call() {
                try {
                    System.err.println(command);
                    session.execute(command);
                } catch (Exception e) {
                    e.printStackTrace(System.err);
                }
                printStream.flush();
                errStream.flush();
                return byteArrayOutputStream.toString();
            }
        });

        try {
            executor.submit(commandFuture);
            response = commandFuture.get(10000L, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            e.printStackTrace(System.err);
            response = "SHELL COMMAND TIMED OUT: ";
        }

        System.err.println(response);

        return response;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected <T> T getOsgiService(Class<T> type, String filter, long timeout) {
        ServiceTracker tracker = null;
        try {
            String flt;
            if (filter != null) {
                if (filter.startsWith("(")) {
                    flt = "(&(" + Constants.OBJECTCLASS + "=" + type.getName() + ")" + filter + ")";
                } else {
                    flt = "(&(" + Constants.OBJECTCLASS + "=" + type.getName() + ")(" + filter + "))";
                }
            } else {
                flt = "(" + Constants.OBJECTCLASS + "=" + type.getName() + ")";
            }
            Filter osgiFilter = FrameworkUtil.createFilter(flt);
            tracker = new ServiceTracker(bc, osgiFilter, null);
            tracker.open(true);
            // Note that the tracker is not closed to keep the reference
            // This is buggy, as the service reference may change i think
            Object svc = type.cast(tracker.waitForService(timeout));
            if (svc == null) {
                Dictionary dic = bc.getBundle().getHeaders();
                System.err.println("Test bundle headers: " + explode(dic));

                for (ServiceReference ref : asCollection(bc.getAllServiceReferences(null, null))) {
                    System.err.println("ServiceReference: " + ref);
                }

                for (ServiceReference ref : asCollection(bc.getAllServiceReferences(null, flt))) {
                    System.err.println("Filtered ServiceReference: " + ref);
                }

                logger.error("Gave up waiting for service " + flt);
                return null;
            }
            return type.cast(svc);
        } catch (InvalidSyntaxException e) {
            throw new IllegalArgumentException("Invalid filter", e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /*
     * Explode the dictionary into a ,-delimited list of key=value pairs
     */
    @SuppressWarnings("rawtypes")
    private static String explode(Dictionary dictionary) {
        Enumeration keys = dictionary.keys();
        StringBuffer result = new StringBuffer();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            result.append(String.format("%s=%s", key, dictionary.get(key)));
            if (keys.hasMoreElements()) {
                result.append(", ");
            }
        }
        return result.toString();
    }

    /**
     * Provides an iterable collection of references, even if the original array
     * is null
     */
    @SuppressWarnings("rawtypes")
    private static Collection<ServiceReference> asCollection(ServiceReference[] references) {
        return references != null ? Arrays.asList(references) : Collections.<ServiceReference> emptyList();
    }

    public String currentOutput() {
        printStream.flush();
        errStream.flush();
        String out = byteArrayOutputStream.toString();
        return out;
    }
    
    public File getTargetDirectory() {
    	return new File("../../..");
    }
    
	public static String normalize(String name) {
		if (name == null) return null;
		
		if (name.indexOf('\\') >= 0) name = name.replaceAll("\\\\", "_");
		if (name.indexOf('/') >= 0) name = name.replaceAll("/", "_");
		if (name.indexOf('*') >= 0) name = name.replaceAll("\\*", "_");
		if (name.indexOf('?') >= 0) name = name.replaceAll("\\?", "_");
		if (name.indexOf(':') >= 0) name = name.replaceAll(":", "_");
		if (name.indexOf(' ') >= 0) name = name.replaceAll(" ", "_");
		if (name.indexOf("..") >= 0) name = name.replaceAll("..", "_");
		if (name.indexOf('~') >= 0) name = name.replace('~', '_');

		return name;
	}

	public static String toStr(Throwable t) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		t.printStackTrace(pw);
		pw.flush();
		return sw.toString();
	}

	public static boolean writeFile(File _f, String _content) {
		if (_f == null) return false;
		try {
			OutputStream fos = new FileOutputStream(_f);
			if (_content != null) {
				char[] c = _content.toCharArray();
				for (int i = 0; i < c.length; i++)
					fos.write(c[i]);
			}
			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

}
