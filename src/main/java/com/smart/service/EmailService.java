package com.smart.service;

import com.smart.util.Constants;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;
@Service
public class EmailService {
    public boolean sendEmail(String subject, String message, String to) {
        boolean isSend = false;
        String host = "smtp.gmail.com";
        //Getting System Properties
        Properties properties = System.getProperties();
        // Setting Some Important Information
        properties.put("mail.smtp.host",host);
        properties.put("mail.smtp.port","465");
        properties.put("mail.smtp.ssl.enable","true");
        properties.put("mail.smtp.auth","true");

        // Getting Session Object
        Session session = Session.getInstance(properties, new Authenticator(){
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(Constants.USER_EMAIL,Constants.PASSWORD);
            }
        });

        session.setDebug(true);

        // Compose the Message
        MimeMessage mimeMessage = new MimeMessage(session);

        try {
            mimeMessage.setFrom(Constants.USER_EMAIL);
            mimeMessage.setRecipients(Message.RecipientType.TO, new InternetAddress[]{new InternetAddress(to)});
            mimeMessage.setSubject(subject);
            mimeMessage.setText(message);
            Transport.send(mimeMessage);
            isSend = true;
        } catch(Exception e) {
            e.printStackTrace();
        }

        return isSend;
    }
}
