package de.mhus.osgi.commands.shell;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;

import org.apache.felix.service.command.CommandSession;
import org.apache.karaf.shell.commands.Action;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import de.mhus.lib.core.MFile;

@Command(scope = "shell", name = "read", description = "Read a file")
public class CmdRead implements Action {

	@Argument(index=0, name="fileName", required=false, description="FileName", multiValued=false)
    String fileName;

	@Override
	public Object execute(CommandSession session) throws Exception {

		File f = new File(fileName);
		FileInputStream is = new FileInputStream(f);
		ByteArrayOutputStream ba = new ByteArrayOutputStream();
		MFile.copyFile(is, ba);
		is.close();
		return new String(ba.toByteArray());
	}

	
}
