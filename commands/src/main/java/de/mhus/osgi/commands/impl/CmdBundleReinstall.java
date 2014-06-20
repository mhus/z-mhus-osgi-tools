package de.mhus.osgi.commands.impl;

import java.io.File;
import java.net.URL;
import java.util.Enumeration;

import org.apache.felix.service.command.CommandSession;
import org.apache.karaf.shell.commands.Action;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.w3c.dom.Document;

import de.mhus.lib.core.MFile;
import de.mhus.lib.core.MString;
import de.mhus.lib.core.MXml;

@Command(scope = "bundle", name = "reinstall", description = "Remove from local repository and reinstall bundles from the remote repository")
public class CmdBundleReinstall implements Action {

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
    
	@Override
	public Object execute(CommandSession session) throws Exception {
		

		for (Bundle b : context.getBundles())
			for (String bn : bundleName) {
				if (MString.compareFsLikePattern(b.getSymbolicName(),bn) || String.valueOf(b.getBundleId()).equals(bn)) {
					try {
						doIt(session, b);
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
	
	public void doIt(CommandSession session, Bundle bundle) throws Exception {
		
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
			
			Document pom = MXml.loadXml(pomUrl.openStream());
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
			MFile.deleteDir(file);
		}
		
		if (!notUpdate) {
			String loc = bundle.getLocation();
			session.execute("bundle:uninstall " + bundle.getSymbolicName());
			session.execute("bundle:install -s " + loc);
		}
		if (setWatch)
			session.execute("bundle:watch " + bundle.getSymbolicName());
		
	}
	
	public void setContext(BundleContext context) {
        this.context = context;
    }


}
