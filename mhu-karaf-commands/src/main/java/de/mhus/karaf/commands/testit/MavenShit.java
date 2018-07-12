package de.mhus.karaf.commands.testit;

import java.io.File;

import de.mhus.lib.core.util.MMaven;

public class MavenShit implements ShitIfc {

	@Override
	public void printUsage() {
		System.out.println("Artifact syntax: groupdId/artifactId/version[/type]");
		System.out.println("Artifact syntax: groupdId:artifactId:type:version");
		System.out.println("locate <artifact>");
		System.out.println("download <artifact>");
	}

	@Override
	public Object doExecute(String cmd, String[] parameters) throws Exception {
		if (cmd.equals("locate")) {
			MMaven.Artifact artifact = MMaven.toArtifact(parameters[0]);
			System.out.println("Artifact: " + artifact);
			System.out.println("Location: " + MMaven.locateArtifact(artifact) );
			return null;
		}
		if (cmd.equals("download")) {
			MMaven.Artifact artifact = MMaven.toArtifact(parameters[0]);
			System.out.println("Artifact: " + artifact);
			File location = MMaven.locateArtifact(artifact);
			System.out.println("Location: " + location );
			boolean res = MMaven.downloadArtefact(artifact);
			System.out.println("Downloaded: " + res + " " + (res ? location.length() + " Bytes" : ""));
		}
		return null;
	}

}
