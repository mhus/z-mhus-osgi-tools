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
import java.net.URL;
import java.util.Enumeration;

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
import de.mhus.osgi.api.karaf.AbstractCmd;

@Command(scope = "bundle", name = "reinstall", description = "Remove from local repository and reinstall bundles from the remote repository")
@Service
public class CmdBundleReinstall extends AbstractCmd {

    @Reference
	private BundleContext context;

	@Argument(index=0, name="bundle", required=true, description="(Raw) Name of the bundle to redeploy, use placeholder asterisk", multiValued=true)
    String[] bundleName;

    @Option(name = "-p", aliases = { "--pom" }, description = "Read POM File Instead Of Location Information", required = false, multiValued = false)
    boolean usePom;

    @Option(name = "-n", aliases = { "--noupdate" }, description = "Do not update automatically", required = false, multiValued = false)
    boolean notUpdate;
    
    @Option(name = "-v", aliases = { "--verbose" }, description = "Print errors and verbose information", required = false, multiValued = false)
    boolean printVerbose;
    
    @Option(name = "-w", aliases = { "--watch" }, description = "Set bundle:watch option", required = false, multiValued = false)
    boolean setWatch;
    
    @Option(name = "-c", aliases = { "--clean" }, description = "Clean Local Maven Repository", required = false, multiValued = false)
    boolean cleanLocalRepo;
    
    @Reference
    private Session session;

	@Override
	public Object execute2() throws Exception {
		

		for (Bundle b : context.getBundles())
			for (String bn : bundleName) {
				if (MString.compareFsLikePattern(b.getSymbolicName(),bn) || String.valueOf(b.getBundleId()).equals(bn)) {
					try {
						doIt(b);
					} catch (Throwable t) {
						System.out.println("ERROR: Bundle: " + bn + " " + t.toString());
						if (printVerbose)
							t.printStackTrace();
					}
					break;
				}
		}
		
		return null;
	}
	
	public void doIt(Bundle bundle) throws Exception {
		
		String bn = bundle.getSymbolicName();
		
		if (printVerbose)
			System.out.println("Reinstall " + bn);
		
		String groupId = null;
		String artifactId = null;
		String version = null;

		if (usePom) {
			Enumeration<String> mavenUrl = null;
			try {
				mavenUrl = bundle.getEntryPaths("/META-INF/maven");
			} catch (Throwable t) {}
			if (mavenUrl == null || !mavenUrl.hasMoreElements()) {
				System.out.println("maven meta directory not found:" + bn);
				return;
			}
			String groupPath = mavenUrl.nextElement();
			Enumeration<String> packageUrl = bundle.getEntryPaths(groupPath);
			if (packageUrl == null || !packageUrl.hasMoreElements()) {
				System.out.println("group meta directory not found: " + bn);
				return;
			}
			String artifactPath = packageUrl.nextElement();
			String pomPath = artifactPath + "pom.xml";
			URL pomUrl = bundle.getResource(pomPath);
			if (pomUrl == null) {
				System.out.println("pom.xml not found: + bn");
				return;
			}
			
//			Document pom = MXml.loadXml(pomUrl.openStream());
	//		System.out.println(MXml.dump(pom.getDocumentElement()));
			
			groupId = groupPath.substring("/META-INF/maven".length(), groupPath.length()-1);
			artifactId = artifactPath.substring(groupPath.length(),artifactPath.length()-1);
			version = bundle.getVersion().toString();
			if (version.endsWith(".SNAPSHOT")) version = version.substring(0, version.length()-9) + "-SNAPSHOT"; //hack!
		
		} else {
		
			String location = bundle.getLocation();
			if (location.startsWith("wrap:")) {
				// remove wrap
				location = location.substring(5);
				int pos = location.indexOf('$');
				if (pos > 0) location = location.substring(0, pos);
			}
			if (location.startsWith("mvn:")) {
				location = location.substring(4);
				String[] parts = location.split("\\/");
				groupId = parts[0];
				artifactId = parts[1];
				version = parts[2];
			} else {
				System.out.println("Bundle has no maven location: " + bn);
				return;
			}
			
		}
		
		String path = System.getProperty("user.home") + "/.m2/repository/" + groupId.replace('.', '/') + "/" + artifactId + "/" + version;
		File file = new File(path);
		
		if (printVerbose)
			System.out.println("Redeploy: " + groupId + ":" + artifactId + ":" + version + " " + path);
		if (!file.exists()) {
			System.out.println("Local repository directory not found: " + bn);
		} else {
			if (cleanLocalRepo)
				MFile.deleteDir(file);
		}
		
		if (!notUpdate) {
			String loc = bundle.getLocation();
			// session.execute("bundle:uninstall " + bundle.getSymbolicName());
			bundle.uninstall();
			
			//session.execute("bundle:install -s " + loc);
			Bundle newBundle = context.installBundle(loc, null);
			newBundle.start();
		}
		if (setWatch)
			session.execute("bundle:watch " + bundle.getSymbolicName());
		
	}
	
	public void setContext(BundleContext context) {
        this.context = context;
    }


}
