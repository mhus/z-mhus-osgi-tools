package de.mhus.osgi.commands.shell;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.StringWriter;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.felix.service.command.CommandSession;
import org.apache.karaf.shell.commands.Action;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;

import de.mhus.lib.core.MFile;
import de.mhus.lib.core.console.ConsoleTable;

@Command(scope = "shell", name = "write", description = "Write to file")
public class CmdWrite implements Action {

	@Argument(index=0, name="fileName", required=false, description="FileName or * for std return", multiValued=false)
    String fileName;

    @Option(name = "-a", aliases = { "--append" }, description = "Append to existing file", required = false, multiValued = false)
    boolean append;
    
	@Override
	public Object execute(CommandSession session) throws Exception {

		if (fileName.equals("*")) {
			ByteArrayOutputStream ba = new ByteArrayOutputStream();
			MFile.copyFile(System.in, ba);
			return new String(ba.toByteArray());
		} else {
			File f = new File(fileName);
			FileOutputStream fos = new FileOutputStream(f, append);
			
			MFile.copyFile(System.in, fos);
			fos.close();
			
			return null;
		}
	}

}
