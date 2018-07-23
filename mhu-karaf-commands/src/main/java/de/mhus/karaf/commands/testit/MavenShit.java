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
