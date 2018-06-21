/**
 * Copyright 2018 Mike Hummel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.mhus.karaf.commands.impl;

import java.io.File;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.api.console.Session;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import de.mhus.lib.core.MFile;
import de.mhus.lib.core.MString;
import de.mhus.lib.core.MSystem;
import de.mhus.lib.core.MThread;

@Command(scope = "bundle", name = "upgrade", description = "Upgrade bundle version")
@Service
public class CmdBundleUpgrade implements Action {

    @Reference
	private BundleContext context;

	@Argument(index=0, name="bundle", required=true, description="bundle filter (regex)", multiValued=false)
    String bundleFilter;

	@Argument(index=1, name="version", required=false, description="bundle version to install or empty to reinstall", multiValued=false)
    String bundleVersion;

    @Option(name = "-t", aliases = { "--try" }, description = "Try run, download from repository will be enabled", required = false, multiValued = false)
    boolean tryRun = false;

    @Option(name = "-g", aliases = { "--get" }, description = "Download from repository with maven before installing the bundle", required = false, multiValued = false)
    boolean mvnGet;

    @Option(name = "-repo", description = "Set maven repository, e.g. http://nexus:8080/nexus/content/repositories/releases/", required = false, multiValued = false)
    String mvnRepo = "http://central.maven.org/maven2";
    
    @Option(name = "-i", aliases = { "--install" }, description = "Install only, do not start", required = false, multiValued = false)
    boolean installOnly;
    
    @Option(name = "-u", aliases = { "--notuninstall" }, description = "Do not uninstall bundles", required = false, multiValued = false)
    boolean notuninstall;
    
    @Option(name = "-d", aliases = { "--delete" }, description = "Delete in local maven repository before install", required = false, multiValued = false)
    boolean delete;
    
    @Option(name = "-s", aliases = { "--start" }, description = "Stop and Start only", required = false, multiValued = false)
    boolean startOnly;
    
    @Option(name = "-r", aliases = { "--repo" }, description = "Change maven repository location", required = false, multiValued = false)
    String mavenRepoLocation;
    
    @Reference
    private Session session;

	@Override
	public Object execute() throws Exception {
		
		if (mvnGet && delete) {
			System.out.println("The Options get and delete make no sense in the same time!");
		}
		
		File repoHome = null;
		if (delete) {
			// this is not save ....
			if (mavenRepoLocation != null)
				repoHome = new File(mavenRepoLocation);
			else {
				File home = MSystem.getUserHome();
				repoHome = new File(home,".m2/repository");
			}
			if (!repoHome.exists() && repoHome.isDirectory()) {
				System.out.println("Maven local repository not found: " + repoHome);
				return null;
			}
			
			if (tryRun)
				System.out.println("Maven Local Repository: " + repoHome);
		}
		
		
		for (Bundle b : context.getBundles()) {
			if (b.getSymbolicName().matches(bundleFilter) && b.getLocation().startsWith("mvn:")) {
				System.out.println(">>> " + b.getLocation() + " -> " + bundleVersion);
				Cont c = new Cont(b);
				
				// download before via mvn command ...
				if (mvnGet) {
					/*
mvn org.apache.maven.plugins:maven-dependency-plugin:2.1:get
 -DrepoUrl=http://nexus:8080/nexus/content/repositories/releases/
 -Dartifact=de.mhus.lib:mhu-lib-core:3.3.5

					 */
					String cmd = "mvn org.apache.maven.plugins:maven-dependency-plugin:2.1:get -DrepoUrl=" + mvnRepo 
							+ " -Dartifact=" + c.getArtefactPath();
					
					System.out.println("--- Loading " + c.getArtefactPath() + " from " + mvnRepo);
					String[] res = MSystem.execute(cmd);
					if (res[0].indexOf("[ERROR] For more information") > 0) {
						// failed
						System.out.println("Loading artefact from repository failed");
						System.out.println("shell:exec " + cmd);
						System.out.println(res[0]);
						System.out.println(res[1]);
						continue;
					}
				}
				
				// stop
				if (tryRun)
					System.out.println("bundle:stop " + c.bundle.getSymbolicName());
				else
					session.execute("bundle:stop " + c.bundle.getSymbolicName());
				
				if (!startOnly) {
					// uninstall
					if (tryRun)
						System.out.println("bundle:uninstall " + c.bundle.getSymbolicName());
					else
						session.execute("bundle:uninstall " + c.bundle.getSymbolicName());
					MThread.sleep(1000);
	
					// delete
					if (repoHome != null) {
						String loc = c.bundle.getLocation();
						if (loc != null && loc.startsWith("mvn:")) {
							String[] parts = loc.substring(4).split("/");
							if (parts.length >= 3) {
								String path = parts[0].replace('.', '/') + "/" + parts[1] + "/" + parts[2];
								File bundleHome = new File(repoHome,path);
								if (bundleHome.exists()) {
									System.out.println("--- Delete " + bundleHome);
									if (!tryRun)
										MFile.deleteDir(bundleHome);
								}
							}
						}
					}
					
					// install
					String url = c.getNewUrl();
					if (url == null)
						System.out.println("*** Can't install " + c.bundle.getSymbolicName());
					else {
						String cmd = "bundle:install " + (installOnly ? "" : "-s ") + url;
						try {
							if (tryRun)
								System.out.println(cmd);
							else
								session.execute(cmd);
						} catch (Exception e) {
							e.printStackTrace();
						}
						MThread.sleep(1000);
					}
					
				}
			}
		}
		
		if (!installOnly) {
			for (Bundle b : context.getBundles()) {
				if (b.getSymbolicName().matches(bundleFilter) && b.getLocation().startsWith("mvn:") && b.getState() != Bundle.ACTIVE) {
					System.out.println("--- Start " + b.getLocation());
					String cmd = "bundle:start " + b.getBundleId();
					try {
						if (tryRun)
							System.out.println(cmd);
						else
							session.execute(cmd);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		
		return null;
	}
	
	class Cont {

		private Bundle bundle;

		public Cont(Bundle b) {
			this.bundle = b;
		}

		public String getNewUrl() {
			String loc = bundle.getLocation();
			if (loc == null || !loc.startsWith("mvn:")) return null;
			String[] parts = loc.split("/");
			if (parts.length < 3) return null;
			if (bundleVersion != null)
				parts[2] = bundleVersion;
			return MString.join(parts, '/');
		}
		
		public String getArtefactPath() {
			String loc = bundle.getLocation();
			if (loc == null || !loc.startsWith("mvn:")) return null;
			String[] parts = loc.split("/");
			if (parts.length < 3) return null;
			if (bundleVersion != null)
				parts[2] = bundleVersion;
			
			return parts[0].substring(4) + ":" + parts[1] + ":" + parts[2];
		}
		
	}

}
