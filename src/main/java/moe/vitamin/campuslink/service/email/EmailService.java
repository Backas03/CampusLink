package moe.vitamin.campuslink.service.email;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import moe.vitamin.campuslink.CampusLink;
import moe.vitamin.campuslink.config.impl.EmailConfig;

import java.util.Properties;

public class EmailService {

    public static void sendEmail(String toEmail, String subject, String plainMessage) throws MessagingException {
        Message message = setupMessage(toEmail);

        message.setSubject(subject);
        message.setText(plainMessage);

        Transport.send(message);
    }

    public static void sendEmailAsHTML(String toEmail, String subject, String plainHTMLMessage) throws MessagingException {
        Message message = setupMessage(toEmail);

        message.setSubject(subject);
        message.setContent(plainHTMLMessage, "text/html; charset=utf-8");

        Transport.send(message);
    }

    private static Message setupMessage(String toEmail) throws MessagingException {
        EmailConfig config = CampusLink.getInstance()
                .getConfigManager()
                .getEmailConfig();

        Properties properties = new Properties();
        properties.put("mail.smtp.host", config.getHost());
        properties.put("mail.smtp.port", config.getPort());
        properties.put("mail.smtp.auth", config.isAuth());

        Boolean startTLSEnabled = config.getStartTLSEnabled();
        if (startTLSEnabled != null) {
            properties.put("mail.smtp.starttls.enable", startTLSEnabled);
        }

        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(config.getCredentialEmail(), config.getAppPassword());
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(config.getCredentialEmail()));
        message.setRecipients(
                Message.RecipientType.TO,
                InternetAddress.parse(toEmail)
        );
        return message;
    }


}
