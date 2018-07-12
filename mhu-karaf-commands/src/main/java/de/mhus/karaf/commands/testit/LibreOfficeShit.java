package de.mhus.karaf.commands.testit;

import java.io.File;

import de.mhus.lib.core.MString;
import de.mhus.lib.core.io.LibreOfficeConnector;
import de.mhus.lib.core.parser.StringPropertyReplacerMap;

public class LibreOfficeShit implements ShitIfc {

	@Override
	public void printUsage() {
		System.out.println("version");
		System.out.println("convert <file> <format> [<output directory>]");
		System.out.println("replace <from> <to> [key=value]*");
	}

	@Override
	public Object doExecute(String cmd, String[] parameters) throws Exception {
		if (cmd.equals("version")) {
			LibreOfficeConnector tool = new LibreOfficeConnector();
			System.out.println("Binary : " + tool.getBinary());
			System.out.println("Valid  : " + tool.isValid());
			System.out.println("Version: " + tool.getVersion());
			return null;
		}
		if (cmd.equals("convert")) {
			LibreOfficeConnector tool = new LibreOfficeConnector();
			String res = tool.convertTo(parameters[1], parameters[0], parameters.length > 2 ? parameters[2] : null);
			return res;
		}
		if (cmd.equals("pdf")) {
			LibreOfficeConnector tool = new LibreOfficeConnector();
			String res = tool.convertToPdf(parameters[0], parameters.length > 1 ? parameters[1] : null);
			return res;
		}
		if (cmd.equals("replace")) {
			StringPropertyReplacerMap replacer = new StringPropertyReplacerMap();
			for (int i = 2; i < parameters.length; i++) {
				String key = MString.beforeIndex(parameters[i], '=');
				String value = MString.afterIndex(parameters[i], '=');
				replacer.put(key, value);
			}
				
			LibreOfficeConnector.replace(new File(parameters[0]), new File(parameters[1]), replacer);
		}
		return null;
	}

}
