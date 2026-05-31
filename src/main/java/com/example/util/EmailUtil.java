package com.example.util;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

public class EmailUtil {

    // Настройки SMTP
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";
    private static final String FROM_EMAIL = "ctfvvsu@gmail.com";
    private static final String FROM_PASSWORD = "mibg lnyw xeqn ldah";

    public static void sendVerificationEmail(String to, String verificationCode) {
        Properties props = new Properties();
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(FROM_EMAIL, FROM_PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject("Подтверждение регистрации");

            String verificationLink = "http://localhost:8080/CTFproject/verify?email=" + to + "&code=" + verificationCode;

            String content = "<html>" +
                    "<body>" +
                    "<h2>Добро пожаловать!</h2>" +
                    "<p>Для завершения регистрации перейдите по ссылке:</p>" +
                    "<a href='" + verificationLink + "'>" + verificationLink + "</a>" +
                    "<p>Ссылка действительна в течение 24 часов.</p>" +
                    "<p>Если вы не регистрировались, проигнорируйте это письмо.</p>" +
                    "</body>" +
                    "</html>";

            message.setContent(content, "text/html; charset=utf-8");
            Transport.send(message);

        } catch (MessagingException e) {
            e.printStackTrace();
            throw new RuntimeException("Ошибка отправки email", e);
        }
    }
}