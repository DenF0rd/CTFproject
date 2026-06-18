package com.example.util;

import com.sendgrid.SendGrid;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.Method;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Content;

import java.io.IOException;
import java.util.Random;

public class EmailUtil {

    private static final String SENDGRID_API_KEY = "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";

    /**
     * Генерирует 6-значный код подтверждения
     */
    public static String generateVerificationCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }

    public static void sendVerificationEmail(String to, String verificationCode) {
        // Создаём отправителя и получателя
        Email from = new Email("ctfvvsu@gmail.com");
        from.setName("CTF Platform");

        Email toEmail = new Email(to);

        // Содержимое письма с КОДОМ вместо ссылки
        String content = "<html>" +
                "<body style='font-family: Arial, sans-serif;'>" +
                "<h2 style='color: #8b5cf6;'>Добро пожаловать на CTF Platform!</h2>" +
                "<p>Ваш код подтверждения регистрации:</p>" +
                "<div style='background: #f0f0f0; padding: 20px; border-radius: 10px; text-align: center; font-size: 2rem; font-weight: bold; letter-spacing: 5px; color: #333;'>" +
                verificationCode +
                "</div>" +
                "<p style='margin-top: 20px;'>Введите этот код на странице подтверждения.</p>" +
                "<p style='color: #888; font-size: 0.8rem;'>Код действителен в течение 24 часов.</p>" +
                "<p style='color: #888; font-size: 0.8rem;'>Если вы не регистрировались, проигнорируйте это письмо.</p>" +
                "</body>" +
                "</html>";

        Content emailContent = new Content("text/html", content);

        // Создаём письмо
        Mail mail = new Mail(from, "Код подтверждения регистрации", toEmail, emailContent);

        // Отправляем через SendGrid
        SendGrid sg = new SendGrid(SENDGRID_API_KEY);
        Request request = new Request();

        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);

            System.out.println("=========================================");
            System.out.println("📧 Код подтверждения отправлен");
            System.out.println("Кому: " + to);
            System.out.println("Код: " + verificationCode);
            System.out.println("Статус: " + response.getStatusCode());
            System.out.println("=========================================");

            if (response.getStatusCode() == 202) {
                System.out.println("✅ Письмо принято SendGrid для доставки");
            } else {
                System.err.println("❌ Ошибка при отправке: " + response.getStatusCode());
                System.err.println("Ответ: " + response.getBody());
            }

        } catch (IOException ex) {
            System.err.println("❌ Ошибка при отправке письма через SendGrid");
            ex.printStackTrace();
        }
    }
}