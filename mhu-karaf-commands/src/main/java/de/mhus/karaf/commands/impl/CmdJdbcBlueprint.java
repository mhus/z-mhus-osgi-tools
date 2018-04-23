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
import java.io.FileInputStream;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.lib.core.MFile;
import de.mhus.lib.core.MString;

@Command(scope = "jdbc", name = "blueprint", description = "Print blueprint of the datasource or a list of blueprints")
@Service
public class CmdJdbcBlueprint implements Action {

	@Argument(index=0, name="source", required=false, description="Datasource", multiValued=false)
    String source;

	@Override
	public Object execute() throws Exception {
		
		File karafBase = new File(System.getProperty("karaf.base"));
		File deployFolder = new File(karafBase, "deploy");
		
		if (source == null) {
		
			for (File f : deployFolder.listFiles()) {
				if (f.isFile() && f.getName().startsWith("datasource-")) {
					System.out.println(MString.beforeLastIndex(MString.afterIndex(f.getName(), '-'), '.') );
				}
			}
			
		} else {
		
	        File datasourceFile = new File(deployFolder, "datasource-" + source + ".xml");
	        if (!datasourceFile.exists()) {
	            throw new IllegalArgumentException("The JDBC datasource file "+ datasourceFile.getPath() + " doesn't exist");
	        }
	
	        FileInputStream fis = new FileInputStream (datasourceFile);
			MFile.copyFile(fis, System.out);
			fis.close();
			
		}
		return null;
	}

}
