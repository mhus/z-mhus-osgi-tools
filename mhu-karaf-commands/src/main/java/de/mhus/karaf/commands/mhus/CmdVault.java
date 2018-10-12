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
package de.mhus.karaf.commands.mhus;

import java.io.File;
import java.util.UUID;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.lib.core.MFile;
import de.mhus.lib.core.MLog;
import de.mhus.lib.core.MPassword;
import de.mhus.lib.core.console.ConsoleTable;
import de.mhus.lib.core.vault.DefaultEntry;
import de.mhus.lib.core.vault.FileVaultSource;
import de.mhus.lib.core.vault.MVault;
import de.mhus.lib.core.vault.MVaultUtil;
import de.mhus.lib.core.vault.MutableVaultSource;
import de.mhus.lib.core.vault.VaultEntry;
import de.mhus.lib.core.vault.VaultSource;

@Command(scope = "mhus", name = "vault", description = "Vault Manipulation")
@Service
public class CmdVault extends MLog implements Action {

	private static final long MAX_FILE_LENGTH = 1024 * 1024; // max 1 MB

	@Argument(index=0, name="cmd", required=true, description="Create keys with openssl: openssl genrsa -out private.pem 8192\n"
			+ "Commands:\n"
			+ " sources\n"
			+ " list\n"
			+ " get <id>\n"
			+ " move <id> <to source>   - clone and remove from old source\n"
			+ " clone <id> <to source>  - copy with the same id\n"
			+ " copy <id> <to source>   - copy with a new id\n"
			+ " addfile <file> [description]\n"
			+ " addraw <type> <description> <value>\n"
			+ " remove <id>\n"
			+ " save\n"
			+ " load\n"
			+ " addfilesource <file> <passphrase>\n"
			+ " removesource <id>\n"
			+ " encodepasswordrot13 <clear>\n"
			+ " encodepasswordwithkey <key id> <clear>\n"
			+ " encodepasswordmd5 <clear>\n"
			+ " decodepassword <encoded password>\n"
			+ "", multiValued=false)
    String cmd;

	@Argument(index=1, name="paramteters", required=false, description="Parameters", multiValued=true)
    String[] parameters;

	@Option(name="-s", aliases="--source",description="Set vault source name")
	String sourcename = null;
	
	@Option(name="-f", aliases="--full", description="Full output",required=false)
	boolean full = false;

	@Option(name="-id", description="Optiona a existing uuid to import in raw mode",required=false)
	String id = null;
	
	@Override
	public Object execute() throws Exception {
		MVault vault = MVaultUtil.loadDefault();
		
		if (cmd.equals("copy")) {
			VaultEntry entry = null;
			if (sourcename != null)
				entry = vault.getSource(sourcename).getEntry(UUID.fromString(parameters[0]));
			else
				entry = vault.getEntry(UUID.fromString(parameters[0]));
			if (entry == null) {
				System.out.println("Entry not found");
				return null;
			}
			entry = new DefaultEntry(entry.getType(), entry.getDescription(), entry.getValue());
			vault.getSource(parameters[1]).getEditable().addEntry(entry);
			System.out.println("OK " + entry.getId());
		} else
		if (cmd.equals("move")) {
			if (sourcename == null) sourcename = MVault.SOURCE_DEFAULT;
			VaultSource source = vault.getSource(sourcename);
			if (source == null) {
				System.out.println("Source not found " + sourcename);
				return null;
			}
			MutableVaultSource editable = source.getEditable();
			if (editable == null) {
				System.out.println("Source is not editable " + sourcename);
				return null;
			}
			VaultEntry entry = source.getEntry(UUID.fromString(parameters[0]));
			if (entry == null) {
				System.out.println("Entry not found in source " + sourcename);
				return null;
			}
			vault.getSource(parameters[1]).getEditable().addEntry(entry);
			editable.removeEntry(UUID.fromString(parameters[0]));
			System.out.println("OK");
		} else
		if (cmd.equals("clone")) {
			VaultEntry entry = null;
			if (sourcename != null)
				entry = vault.getSource(sourcename).getEntry(UUID.fromString(parameters[0]));
			else
				entry = vault.getEntry(UUID.fromString(parameters[0]));
			if (entry == null) {
				System.out.println("Entry not found");
				return null;
			}
			vault.getSource(parameters[1]).getEditable().addEntry(entry);
			System.out.println("OK");
		} else
		if (cmd.equals("sources")) {
			ConsoleTable out = new ConsoleTable(full);
			out.setHeaderValues("Source","Info","Mutable","MemoryBased");
			for (String sourceName : vault.getSourceNames()) {
				VaultSource source = vault.getSource(sourceName);
				
				boolean isMutable = false;
				boolean isMemoryBased = false;
				try {
					MutableVaultSource mutable = source.getEditable();
					isMutable = mutable != null;
					isMemoryBased = mutable.isMemoryBased();
					if (isMutable) {
					}
				} catch (Throwable t) {}
				out.addRowValues(sourceName,source,isMutable,isMemoryBased);
			}
			out.print(System.out);
		} else
		if (cmd.equals("list")) {
			if (sourcename == null) {
				ConsoleTable out = new ConsoleTable(full);
				out.setHeaderValues("Source","Id","Type","Description");
				for (String sourceName : vault.getSourceNames()) {
					try {
						VaultSource source = vault.getSource(sourceName);
						for (UUID id : source.getEntryIds()) {
							VaultEntry entry = source.getEntry(id);
							out.addRowValues(sourceName,id,entry.getType(),entry.getDescription());
						}
					} catch (Throwable t) {
						log().d(sourceName,t);
					}
				}
				out.print(System.out);
			} else {
				VaultSource source = vault.getSource(sourcename);
				if (source == null) {
					System.out.println("Source not found!");
					return null;
				}
				ConsoleTable out = new ConsoleTable(full);
				out.setHeaderValues("Source","Id","Type","Description");
				for (UUID id : source.getEntryIds()) {
					VaultEntry entry = source.getEntry(id);
					out.addRowValues(sourcename,id,entry.getType(),entry.getDescription());
				}
				out.print(System.out);
			}
		} else
		if (cmd.equals("addraw")) {
			if (sourcename == null) sourcename = MVault.SOURCE_DEFAULT;
			VaultSource source = vault.getSource(sourcename);
			if (source == null) {
				System.out.println("Source not found!");
				return null;
			}
			MutableVaultSource mutable = source.getEditable();
			
			String type = parameters[0];
			String description = parameters[1];
			String value = parameters[2];
			DefaultEntry entry = null;
			if (id != null)
				entry = new DefaultEntry(UUID.fromString(id), type, description, value);
			else
				entry = new DefaultEntry(type, description, value);
			mutable.addEntry(entry);
			System.out.println("Created " + entry + ". Don't forget to save!");
		} else
		if (cmd.equals("addfile")) {
			if (sourcename == null) sourcename = MVault.SOURCE_DEFAULT;
			VaultSource source = vault.getSource(sourcename);
			if (source == null) {
				System.out.println("Source not found!");
				return null;
			}
			MutableVaultSource mutable = source.getEditable();
			
			File file = new File(parameters[0]);
			if (!file.exists()) {
				System.out.println("File not found");
				return null;
			}
			if (!file.isFile()) {
				System.out.println("File is not an file");
				return null;
			}
			if (file.length() > MAX_FILE_LENGTH) {
				System.out.println("File to large to load");
				return null;
			}
			String content = MFile.readFile(file);
			String desc = (parameters.length > 1 ? parameters[1] + ";" : "") + file.getName() ;
			VaultEntry entry = null;
			if (content.contains("-----BEGIN RSA PRIVATE KEY-----")) {
				entry = new DefaultEntry(MVault.TYPE_RSA_PRIVATE_KEY,desc,content);
			} else
			if (content.contains("-----BEGIN RSA PUBLIC KEY-----")) {
				entry = new DefaultEntry(MVault.TYPE_RSA_PUBLIC_KEY,desc,content);
			} else
				entry = new DefaultEntry(MVault.TYPE_TEXT,desc,content);
			
			mutable.addEntry(entry);
			System.out.println("Create " + entry.getId() + " " + entry.getType() + ". Don't forget to save!");
			
		} else
		if (cmd.equals("save")) {
			if (sourcename == null) sourcename = MVault.SOURCE_DEFAULT;
			VaultSource source = vault.getSource(sourcename);
			if (source == null) {
				System.out.println("Source not found!");
				return null;
			}
			MutableVaultSource mutable = source.getEditable();
			mutable.doSave();
			System.out.println("OK");
		} else
		if (cmd.equals("load")) {
			if (sourcename == null) sourcename = MVault.SOURCE_DEFAULT;
			VaultSource source = vault.getSource(sourcename);
			if (source == null) {
				System.out.println("Source not found!");
				return null;
			}
			MutableVaultSource mutable = source.getEditable();
			mutable.doLoad();
			System.out.println("OK");
		} else
		if (cmd.equals("get")) {
			VaultEntry entry = null;
			if (sourcename != null)
				entry = vault.getSource(sourcename).getEntry(UUID.fromString(parameters[0]));
			else
				entry = vault.getEntry(UUID.fromString(parameters[0]));
			if (entry == null) {
				System.out.println("Entry not found");
				return null;
			}
			System.out.println("Id         : " + entry.getId());
			System.out.println("Type       : " + entry.getType());
			System.out.println("Description: " + entry.getDescription());
			System.out.println(" Value");
			System.out.println("-------");
			System.out.println(entry.getValue().value());
			System.out.println("-------");
		} else
		if (cmd.equals("remove")) {
			if (sourcename == null) sourcename = MVault.SOURCE_DEFAULT;
			VaultSource source = vault.getSource(sourcename);
			if (source == null) {
				System.out.println("Source not found!");
				return null;
			}
			MutableVaultSource mutable = source.getEditable();
			
			mutable.removeEntry(UUID.fromString(parameters[0]));
			System.out.println("OK");
		} else
		if (cmd.equals("addfilesource")) {
			FileVaultSource source = new FileVaultSource(new File(parameters[0]), parameters[1]);
			vault.registerSource(source);
			System.out.println("Registered " + source);
		} else
		if (cmd.equals("removesource")) {
			vault.unregisterSource(parameters[0]);
			System.out.println("OK");
		} else
		if (cmd.equals("encodepasswordrot13")) {
			System.out.println( MPassword.encode(MPassword.METHOD.ROT13, parameters[0]) );
		}
		if (cmd.equals("encodepasswordwithkey")) {
			System.out.println( MPassword.encode(MPassword.METHOD.RSA, parameters[1], parameters[0]) );
		} else
		if (cmd.equals("encodepasswordmd5")) {
			System.out.println( MPassword.encode(MPassword.METHOD.HASH_MD5, parameters[1]) );
		} else
		if (cmd.equals("decodepassword")) {
			System.out.println( MPassword.decode(parameters[0]));
		}
		
		return null;
	}

}
