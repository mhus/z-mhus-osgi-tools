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
package de.mhus.osgi.services.deploy;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;

import org.osgi.service.component.annotations.Component;
import de.mhus.lib.core.MFile;
import de.mhus.lib.core.MLog;
import de.mhus.lib.core.MProperties;
import de.mhus.lib.core.io.Unzip;

@Component(service = FileDeployer.class, properties="extension=zip")
public class FileDeployUnzipService extends MLog implements FileDeployer {

	@Override
	public void doDeploy(File root, String path, URL entry, MProperties config) {
		File f = new File(root, path);
		root = f.getParentFile();
		root.mkdirs();
		
		try {
			// copy file
			InputStream is = entry.openStream();
			FileOutputStream os = new FileOutputStream(f);
			MFile.copyFile(is, os);
			is.close();
			os.close();
			
			// unzip
			new Unzip().unzip(f, root, null);
			
			// delete zip
			f.delete();
		} catch (Throwable t) {
			log().w(f,t);
		}
	}

}
