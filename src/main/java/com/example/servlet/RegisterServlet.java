package com.example.servlet;

import com.example.dao.UserDAO;
import com.example.util.EmailUtil;
import com.example.util.PasswordUtil;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    private UserDAO userDAO = new UserDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String email = req.getParameter("email");
        String password = req.getParameter("password");
        String confirmPassword = req.getParameter("confirmPassword");
        String username = req.getParameter("username");

        // Валидация email
        if (email == null || !email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            req.setAttribute("error", "Введите корректный email");
            req.getRequestDispatcher("/register.jsp").forward(req, resp);
            return;
        }

        // Валидация пароля
        if (password == null || password.length() < 6) {
            req.setAttribute("error", "Пароль должен быть не менее 6 символов");
            req.getRequestDispatcher("/register.jsp").forward(req, resp);
            return;
        }

        if (!password.equals(confirmPassword)) {
            req.setAttribute("error", "Пароли не совпадают");
            req.getRequestDispatcher("/register.jsp").forward(req, resp);
            return;
        }

        // Проверка существующего пользователя
        if (userDAO.emailExists(email)) {
            req.setAttribute("error", "Пользователь с таким email уже существует");
            req.getRequestDispatcher("/register.jsp").forward(req, resp);
            return;
        }

        // Генерация username из email, если поле не заполнено
        if (username == null || username.trim().isEmpty() || username.length() < 3) {
            username = email.substring(0, email.indexOf('@'));
            username = username.replaceAll("[^a-zA-Z0-9_]", "");
            if (username.isEmpty()) {
                username = "user_" + System.currentTimeMillis();
            }
        } else {
            username = username.trim().replaceAll("[^a-zA-Z0-9_]", "");
        }

        if (username.length() < 3) {
            req.setAttribute("error", "Имя пользователя должно быть не менее 3 символов");
            req.getRequestDispatcher("/register.jsp").forward(req, resp);
            return;
        }

        // ХЭШИРУЕМ ПАРОЛЬ перед сохранением
        String hashedPassword = PasswordUtil.hashPassword(password);
        boolean registered = userDAO.registerUser(username, email, hashedPassword);

        if (registered) {
            String verificationCode = UUID.randomUUID().toString();
            userDAO.saveVerificationCode(email, verificationCode);

            try {
                EmailUtil.sendVerificationEmail(email, verificationCode);
                req.setAttribute("message", "Регистрация успешна! Проверьте почту для подтверждения.");
                req.setAttribute("email", email);
                req.getRequestDispatcher("/register-success.jsp").forward(req, resp);
            } catch (Exception e) {
                e.printStackTrace();
                req.setAttribute("error", "Ошибка отправки письма: " + e.getMessage());
                req.getRequestDispatcher("/register.jsp").forward(req, resp);
            }
        } else {
            req.setAttribute("error", "Ошибка регистрации. Попробуйте позже.");
            req.getRequestDispatcher("/register.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.getRequestDispatcher("/register.jsp").forward(req, resp);
    }
}