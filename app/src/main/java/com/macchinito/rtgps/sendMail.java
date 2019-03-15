package com.macchinito.rtgps;

// javamail
import javax.mail.*;
import javax.mail.internet.*;

import java.util.Properties;

import android.util.Log;

class mailInfo {
    static String To;
    static String To2;
    static String Subject;
    static String txtString;
    static String htmlString;
    static String logString;
    static String ryutoMessage;
    static String locInfo;
    static boolean html = false;
    static boolean busy = false;
    static long counter;
    static String reason;
}

public class sendMail {

    //final static String SENDER_MAIL = "whereisryuto@gmail.com";
    //final static String SENDER_PASSWD = "kako101303614";
    //final static String SMTP_HOST = "smtp.gmail.com";

    final static String SENDER_MAIL = "whereisryuto@macchinito.com";
    final static String SENDER_PASSWD = "kako101303614";
    final static String SMTP_HOST = "macchinito.com";

    final static String SMTP_PORT = "587";
    //final static String SMTP_PORT = "465";
    final static String SMTP_AUTH = "true";
    //final static String SMTP_STARTTLS_ENABLE = "true";

    private Properties properties;
         
    public sendMail(){
	properties = System.getProperties();
    }
         
    public boolean send(){

	boolean result = true;

	properties.put("mail.smtp.host", SMTP_HOST);
	properties.put("mail.smtp.port", SMTP_PORT);
	properties.put("mail.smtp.auth", SMTP_AUTH);
	//properties.put("mail.smtp.starttls.enable", SMTP_STARTTLS_ENABLE);
	properties.put("mail.smtp.debug", "true");

	properties.put("mail.smtp.connectiontimeout", "60000");
	properties.put("mail.smtp.timeout", "60000");
	/* works only for gmail
	properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");  // for ssl
	properties.put("mail.smtp.socketFactory.fallback", "false");  // for ssl
	properties.put("mail.smtp.socketFactory.port", SMTP_PORT);      // for ssl
	*/
	Session session = Session.getDefaultInstance(properties, new javax.mail.Authenticator(){
		protected PasswordAuthentication getPasswordAuthentication(){
		    return new PasswordAuthentication(SENDER_MAIL, SENDER_PASSWD);
		}
	    });
	MimeMessage message = new MimeMessage(session);
        
	try {
	    message.setSubject(mailInfo.Subject);
	    message.setFrom(new InternetAddress(SENDER_MAIL));
	    message.setSender(new InternetAddress(SENDER_MAIL));
	    if (mailInfo.To2 == null)
		message.setRecipient(Message.RecipientType.TO, new InternetAddress(mailInfo.To));
	    else
		message.setRecipients(Message.RecipientType.TO, new InternetAddress[]{new InternetAddress(mailInfo.To), new InternetAddress(mailInfo.To2)});

	    if (mailInfo.html) {
	    
		Multipart alternativePart = new MimeMultipart("alternative");

		// text mail
		MimeBodyPart textBodyPart = new MimeBodyPart();
		textBodyPart.setText(mailInfo.txtString, "utf-8", "plain");
		textBodyPart.setHeader("Content-Transfer-Encoding", "base64");
		alternativePart.addBodyPart(textBodyPart);	// alter
		
		// html mail
		MimeBodyPart htmlBodyPart = new MimeBodyPart();
		//	    htmlBodyPart.setText(htmlString, "shift-jis", "html");
		htmlBodyPart.setText(mailInfo.htmlString, "utf-8", "html");
		htmlBodyPart.setHeader("Content-Transfer-Encoding", "base64");
		alternativePart.addBodyPart(htmlBodyPart);

		// set alternative
		message.setContent(alternativePart);
	    
	    } else {
		message.setText(mailInfo.txtString);
	    }

	    Transport.send(message);

	} catch (AddressException e) {
	    e.printStackTrace();
	    Log.v("RTGPS", "*ADDRESSEXCEPTION");
	    result = false;
	} catch (MessagingException e) {
	    e.printStackTrace();
	    Log.v("RTGPS", "*MESSAGINGEXCEPTION");
	    result = false;
	}
	return result;
    }
}
