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

import java.io.InputStreamReader;
import java.nio.charset.Charset;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.lib.core.MApi;
import de.mhus.lib.core.MFile;
import de.mhus.lib.core.mail.MSendMail;
import de.mhus.lib.core.mail.MailAttachment;

@Command(scope = "mhus", name = "mail", description = "Send a mail via MSendMail")
@Service
public class CmdMail implements Action {

	@Argument(index=0, name="address", required=true, description="Recipients", multiValued=false)
    String to;
	
	@Argument(index=1, name="subject", required=true, description="Subject", multiValued=false)
    String subject;
	
	@Argument(index=2, name="message", required=true, description="Message or * for stdin", multiValued=false)
    String message;
	
	@Argument(index=3, name="attachment", required=false, description="Attachment", multiValued=true)
    String[] attachments;

	@Option(name="-f", aliases="--from", description="Sender address",required=false)
	String from;

	@Option(name="-c", aliases="--cc", description="Copy to",required=false)
	String cc;
	
	@Option(name="-b", aliases="--bcc", description="Blind copy to",required=false)
	String bcc;

	@Option(name="-p", aliases="--plain", description="Send plain text mail - currently no attachments",required=false)
	boolean plain;
	
	@Override
	public Object execute() throws Exception {
		
		MSendMail sendMail = MApi.lookup(MSendMail.class);
		
		MailAttachment[] attachFiles = null;
		if (attachments != null) {
			attachFiles = new MailAttachment[ attachments == null ? 0 : attachments.length ];
			for (int i = 0; i < attachFiles.length; i++)
				attachFiles[i] = new MailAttachment(attachments[i], false);
		}
		
		if (message.equals("*")) {
			InputStreamReader isr = new InputStreamReader(System.in, Charset.forName("UTF-8"));
			message = MFile.readFile(isr);
		}
		
		if (plain)
			sendMail.sendPlainMail(from, to.split(";"), cc.split(";"), bcc.split(";"), subject, message);
		else
			sendMail.sendHtmlMail(from, to.split(";"), cc == null ? null : cc.split(";"), bcc == null ? null : bcc.split(";"), subject, message, attachFiles);
		
		System.out.println("Send");
		
		return null;
	}
	
}
