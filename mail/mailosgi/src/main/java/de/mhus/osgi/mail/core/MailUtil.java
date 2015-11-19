package de.mhus.osgi.mail.core;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

public class MailUtil {

	public static SendQueueManager getSendQueueManager() {
		BundleContext context = FrameworkUtil.getBundle(MailUtil.class).getBundleContext();
		ServiceReference<SendQueueManager> ref = context.getServiceReference(SendQueueManager.class);
		if (ref == null) return null;
		return context.getService(ref);
	}
	
	public static void addSmtpIfNotExists(String queueName, Properties properties) {
		SendQueueManager manager = getSendQueueManager();
		if (manager.getQueue(queueName) != null) return;
		manager.registerQueue(new SmtpSendQueue(queueName, properties));
	}
	
	public static void sendEmailWithAttachments(String queueName, String from, String toAddress,
            String subject, String message, File ... attachFiles)
            throws Exception {

		SendQueue queue = getSendQueueManager().getQueue(queueName);
        // creates a new e-mail message
        Message msg = new MimeMessage(queue.getSession());
 
        msg.setFrom(new InternetAddress(from));
//        InternetAddress[] toAddresses = { new InternetAddress(toAddress) };
        // msg.setRecipients(Message.RecipientType.TO, toAddresses);
        msg.setSubject(subject);
        msg.setSentDate(new Date());
 
        // creates message part
        MimeBodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setHeader("Content-Type", "text/plain; charset=\"utf-8\"");
        messageBodyPart.setHeader("Content-Transfer-Encoding", "quoted-printable");
        messageBodyPart.setContent(message, "text/html; charset=utf-8");
 
        // creates multi-part
        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(messageBodyPart);
 
        // adds attachments
        if (attachFiles != null && attachFiles.length > 0) {
            for (File filePath : attachFiles) {
            	if (filePath.exists() && filePath.isFile()) {
	                MimeBodyPart attachPart = new MimeBodyPart();
	 
	                try {
	                    attachPart.attachFile(filePath);
	                } catch (IOException ex) {
	                    ex.printStackTrace();
	                }
	 
	                multipart.addBodyPart(attachPart);
            	}
            }
        }
 
        // sets the multi-part as e-mail's content
        msg.setContent(multipart);

        queue.sendMessage(msg, new Address[] { new InternetAddress(toAddress) } );
	}
	
	public static void sendEmailWithAttachments(String queueName, String from, String[] toAddress,
            String subject, String message, File ... attachFiles)
            throws Exception {

		SendQueue queue = getSendQueueManager().getQueue(queueName);
        // creates a new e-mail message
        Message msg = new MimeMessage(queue.getSession());
 
        msg.setFrom(new InternetAddress(from));
//        InternetAddress[] toAddresses = { new InternetAddress(toAddress) };
        // msg.setRecipients(Message.RecipientType.TO, toAddresses);
        msg.setSubject(subject);
        msg.setSentDate(new Date());
 
        // creates message part
        MimeBodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setHeader("Content-Type", "text/plain; charset=\"utf-8\"");
        messageBodyPart.setHeader("Content-Transfer-Encoding", "quoted-printable");
        messageBodyPart.setContent(message, "text/html; charset=utf-8");
 
        // creates multi-part
        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(messageBodyPart);
 
        // adds attachments
        if (attachFiles != null && attachFiles.length > 0) {
            for (File filePath : attachFiles) {
            	if (filePath.exists() && filePath.isFile()) {
	                MimeBodyPart attachPart = new MimeBodyPart();
	 
	                try {
	                    attachPart.attachFile(filePath);
	                } catch (IOException ex) {
	                    ex.printStackTrace();
	                }
	 
	                multipart.addBodyPart(attachPart);
            	}
            }
        }
 
        // sets the multi-part as e-mail's content
        msg.setContent(multipart);

        Address[] addr = new Address[toAddress.length];
        for (int i = 0; i < addr.length; i++)
        	addr[i] = new InternetAddress(toAddress[i]);
        queue.sendMessage(msg, addr );
	}
	
}
