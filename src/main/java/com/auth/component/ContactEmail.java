package com.auth.component;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Properties;

@Component
public class ContactEmail {
    public void sendEmail(List<String> emailAdminList, String subject, String message, 
                          String senderEmail, String senderName, String phoneNumber) {
        try {
            // Set up properties for Gmail SMTP
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "587");

            // Create a session with authentication
            Session session = Session.getInstance(props, new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication("suyashkhare9698@gmail.com", "pugchqxeyiuzdagi");
                }
            });

            // Create a new email message
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress("suyashkhare9698@gmail.com", false));

            // Convert the list of email addresses to an array of InternetAddress
            InternetAddress[] recipientAddresses = new InternetAddress[emailAdminList.size()];
            for (int i = 0; i < emailAdminList.size(); i++) {
                recipientAddresses[i] = new InternetAddress(emailAdminList.get(i));
            }

            // Set recipients
            msg.setRecipients(Message.RecipientType.TO, recipientAddresses);
            msg.setSubject(subject);
            msg.setSentDate(new Date());

            // Construct the email body with a signature
            String emailContent = "<h3>" + subject + "</h3>" +
                                  "<p>" + message + "</p>" +
                                  "<br><br>" +
                                  "<hr>" +  // Adds a horizontal line for separation
                                  "<p style='font-style:italic; color:#555;'>Thanks and Regards,</p>" +
                                  "<p><strong>" + senderName + "</strong></p>" +
                                  "<p>Email: <a href='mailto:" + senderEmail + "'>" + senderEmail + "</a></p>" +
                                  "<p>Phone: " + phoneNumber + "</p>";

            MimeBodyPart messageBody = new MimeBodyPart();
            messageBody.setContent(emailContent, "text/html");

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBody);

            // Attach multipart content to the message
            msg.setContent(multipart);

            // Send the email
            Transport.send(msg);

            System.out.println("Email sent successfully!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
