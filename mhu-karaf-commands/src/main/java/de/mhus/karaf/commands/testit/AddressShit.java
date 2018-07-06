package de.mhus.karaf.commands.testit;

import java.util.Locale;

import de.mhus.lib.core.MProperties;
import de.mhus.lib.core.MXml;
import de.mhus.lib.core.config.XmlConfig;
import de.mhus.lib.core.util.Address;

public class AddressShit implements ShitIfc {

	@Override
	public void printUsage() {
		System.out.println("reload - reload definitions file\n"
				+ "definition - print definition dump\n"
				+ "parse <string> - parse the string and find the salutation\n"
				+ "tostring <string> [locale] - parse salutation and convert to string\n"
				+ "name [key=value]* - create address and print full name (lacale=)\n"
				+ "letter [key=value]* - create address and letter salutation (lacale=)"
				
				);
	}

	@Override
	public Object doExecute(String cmd, String[] parameters) throws Exception {
		if (cmd.equals("reload")) {
			Address.reloadDefinition();
			return "OK";
		}
		if (cmd.equals("definition")) {
			System.out.println( MXml.dump(((XmlConfig)Address.getDefinition()).getXmlElement()) );
			return null;
		}
		if (cmd.equals("parse")) {
			return Address.toSalutation(parameters[0]).name();
		}
		if (cmd.equals("tostring")) {
			Locale l = parameters.length > 1 ? Locale.forLanguageTag(parameters[1]) : null;
			return Address.toSalutationString(Address.toSalutation(parameters[0]), l);
		}
		if (cmd.equals("name")) {
			MProperties attr = MProperties.explodeToMProperties(parameters);
			Address addr = new Address(attr);
			Locale l = attr.isProperty("locale") ? Locale.forLanguageTag(attr.getString("locale")) : null;
			return addr.getFullName(l);
		}
		if (cmd.equals("letter")) {
			MProperties attr = MProperties.explodeToMProperties(parameters);
			Address addr = new Address(attr);
			Locale l = attr.isProperty("locale") ? Locale.forLanguageTag(attr.getString("locale")) : null;
			return addr.getLetterSalutation(l);
		}
		System.out.println("Command not found");
		return null;
	}

}
