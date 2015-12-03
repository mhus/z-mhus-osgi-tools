package de.mhus.osgi.commands.shell;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Scanner;

import org.apache.felix.service.command.CommandSession;
import org.apache.karaf.shell.commands.Action;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;

import de.mhus.lib.core.MFile;

@Command(scope = "shell", name = "read", description = "Read a line from stdin or a file")
public class CmdRead implements Action {

	@Argument(index=0, name="fileName", required=false, description="FileName to read from", multiValued=false)
    String fileName;

    @Option(name = "-o", aliases = { "--out" }, description = "Store content into variable name", required = false, multiValued = false)
    String out;

    @Option(name = "-p", aliases = { "--prompt" }, description = "Prompt", required = false, multiValued = false)
    String prompt;
    
    @Option(name = "-s", aliases = { "--secure" }, description = "Echo stars instead of the characters", required = false, multiValued = false)
    boolean secure;
    
	@Override
	public Object execute(CommandSession session) throws Exception {

		String content = null;
		
		if (fileName == null) {
			try {
				
				if (prompt != null) {
					System.out.print(prompt);
					System.out.flush();
				}
				Reader isr = new InputStreamReader(System.in);
				StringBuilder sb = new StringBuilder();
				while (true) {
					int c = isr.read();
					if (c < 0 || c == '\n' || c == '\r') {
						break;
					} else
					if (c == '\u007F') {
						if (sb.length() > 0) {
							sb.setLength(sb.length()-1);
							System.out.print('\b');
							System.out.print(' ');
							System.out.print('\b');
							System.out.flush();
						} else {
							System.out.print('\7');
							System.out.flush();
						}
					} else {
						sb.append((char)c);
						if (secure)
							System.out.print('*');
						else
							System.out.print((char)c);
						System.out.flush();
					}
					
				}
				System.out.println();
				content = sb.toString();
			} catch (IOException t) {
				
			}
		} else {
			File f = new File(fileName);
			FileInputStream is = new FileInputStream(f);
			ByteArrayOutputStream ba = new ByteArrayOutputStream();
			MFile.copyFile(is, ba);
			is.close();

			content = new String(ba.toByteArray());
		}
		
		if (out != null)
			session.put(out, content);
		else
			return content;
		
		return null;
	}

	
}
