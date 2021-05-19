/**
 * Copyright (C) 2020 Mike Hummel (mh@mhus.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.mhus.osgi.dev.dev.testit;

import java.io.File;

import de.mhus.lib.core.util.MMaven;
import de.mhus.lib.core.util.Version;

public class MavenShit implements ShitIfc {

    @Override
    public void printUsage() {
        System.out.println("Artifact syntax: groupdId/artifactId/version[/type]");
        System.out.println("Artifact syntax: groupdId:artifactId:type:version");
        System.out.println("locate <artifact>");
        System.out.println("download <artifact>");
        System.out.println("delete <artifact> - delete local artifact");
        System.out.println("local <group part> - list all local artifacts in the list");
        System.out.println(
                "cleanupsnapshots <group part> <'force'> - delete all local snapshots in this groups");
        System.out.println(
                "cleanupolder <group part> <version> <'force'> - delete all local and older then version artifacts");
    }

    @Override
    public Object doExecute(CmdShitYo base, String cmd, String[] parameters) throws Exception {
        if (cmd.equals("locate")) {
            MMaven.Artifact artifact = MMaven.toArtifact(parameters[0]);
            System.out.println("Artifact: " + artifact);
            System.out.println("Location: " + MMaven.locateArtifact(artifact));
            return null;
        }
        if (cmd.equals("download")) {
            MMaven.Artifact artifact = MMaven.toArtifact(parameters[0]);
            System.out.println("Artifact: " + artifact);
            File location = MMaven.locateArtifact(artifact);
            System.out.println("Location: " + location);
            boolean res = MMaven.downloadArtefact(artifact);
            System.out.println(
                    "Downloaded: " + res + " " + (res ? location.length() + " Bytes" : ""));
        }
        if (cmd.equals("delete")) {
            MMaven.Artifact artifact = MMaven.toArtifact(parameters[0]);
            System.out.println("Artifact: " + artifact);
            if (MMaven.deleteLocalArtifact(artifact)) System.out.println("Deleted");
        }
        if (cmd.equals("local")) {
            for (MMaven.Artifact item : MMaven.findLocalArtifacts(parameters[0]))
                System.out.println(item + " " + item.getEstimatedVersion());
        }
        if (cmd.equals("cleanupsnapshots")) {
            boolean force = parameters.length > 1 && parameters[1].equals("force");
            for (MMaven.Artifact item : MMaven.findLocalArtifacts(parameters[0]))
                if (!item.isRelease()) {
                    if (!force || MMaven.deleteLocalArtifact(item))
                        System.out.println("-- deleted " + item);
                }
        }
        if (cmd.equals("cleanupolder")) {
            boolean force = parameters.length > 2 && parameters[2].equals("force");
            Version v = new Version(parameters[1]);
            for (MMaven.Artifact item : MMaven.findLocalArtifacts(parameters[0]))
                if (item.getEstimatedVersion().compareTo(v) < 0
                        || !item.isRelease() && item.getEstimatedVersion().compareTo(v) == 0)
                    if (!force || MMaven.deleteLocalArtifact(item))
                        System.out.println("-- deleted " + item);
        }
        return null;
    }
}
