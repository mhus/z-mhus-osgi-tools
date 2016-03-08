package de.mhus.osgi.mail.karaf;

import java.io.File;
import java.io.FileInputStream;
import java.io.PrintStream;
import java.util.Properties;

import org.apache.felix.service.command.CommandSession;
import org.apache.karaf.shell.commands.Action;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import de.mhus.lib.core.MString;
import de.mhus.lib.core.console.ConsoleTable;
import de.mhus.osgi.mail.core.MailUtil;
import de.mhus.osgi.mail.core.SendQueue;
import de.mhus.osgi.mail.core.SendQueueManager;
import de.mhus.osgi.mail.core.SmtpSendQueue;

@Command(scope = "mail", name = "send", description = "Send a mail via send queue")
public class CmdSend implements Action {

	private SendQueueManager admin;

	@Argument(index=0, name="name", required=true, description="Queue Name", multiValued=false)
    String name;
	@Argument(index=1, name="from", required=true, description="Sender", multiValued=false)
    String from;
	@Argument(index=2, name="address", required=true, description="Recipient", multiValued=false)
    String address;
	@Argument(index=3, name="subject", required=true, description="Subject", multiValued=false)
    String subject;
	@Argument(index=4, name="message", required=true, description="Message", multiValued=false)
    String message;
	@Argument(index=5, name="attachment", required=false, description="Attachment", multiValued=true)
    String[] attachments;

	@Override
	public Object execute(CommandSession session) throws Exception {
		
		SendQueue q = admin.getQueue(name);
		if (q == null) {
			System.out.println("Queue not found");
			return null;
		}
		File[] attachFiles = new File[ attachments == null ? 0 : attachments.length ];
		for (int i = 0; i < attachFiles.length; i++)
			attachFiles[i] = new File(attachments[i]);
		
		MailUtil.sendEmailWithAttachments(name, from, address, subject, message, attachFiles);
		
		System.out.println("OK");
		
		return null;
	}
	
	public void setAdmin(SendQueueManager admin) {
		this.admin = admin;
	}

}
