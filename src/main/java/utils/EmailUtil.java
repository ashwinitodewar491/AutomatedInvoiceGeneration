package utils;

import jakarta.mail.*;
import jakarta.mail.internet.*;

import java.io.File;
import java.util.Properties;

public class EmailUtil {

    public static void sendEmailWithAttachment(
            File attachment,
            String toEmail,
            String subject,
            String projectName,   // kept for logging / future use
            String mailContent
    ) {

        final String username = System.getProperty("SMTP_USER",EnvConfig.get("SMTP_USER"));
        final String password = System.getProperty("SMTP_PASS",EnvConfig.get("SMTP_PASS"));

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.ethereal.email");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props,
                new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));

            message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(toEmail)
            );

            // ✅ USE SUBJECT AS PASSED
            message.setSubject(subject);

            // ✅ MAIL BODY AS PASSED
            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setText(mailContent);

            // ✅ ATTACHMENT
            MimeBodyPart attachmentPart = new MimeBodyPart();
            attachmentPart.attachFile(attachment);

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(textPart);
            multipart.addBodyPart(attachmentPart);

            message.setContent(multipart);

            Transport.send(message);
            System.out.println(
                    "✅ Email sent for project: " + projectName
            );

        } catch (Exception e) {
            throw new RuntimeException("❌ Email sending failed", e);
        }
    }
}
