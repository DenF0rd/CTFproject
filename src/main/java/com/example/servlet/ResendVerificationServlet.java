package com.example.servlet;

import com.example.dao.UserDAO;
import com.example.model.User;
import com.example.util.EmailUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

@WebServlet("/resend-verification")
public class ResendVerificationServlet extends HttpServlet {

    private UserDAO userDAO = new UserDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String email = req.getParameter("email");

        if (email == null || email.trim().isEmpty()) {
            req.setAttribute("error", "Email не указан");
            req.getRequestDispatcher("/register.jsp").forward(req, resp);
            return;
        }

        // Ищем пользователя по email
        User user = userDAO.findByEmail(email);

        // Проверяем, существует ли пользователь и не подтверждён ли он
        if (user != null && !user.isVerified()) {
            // Генерируем новый код подтверждения
            String newCode = UUID.randomUUID().toString();

            try {
                EmailUtil.sendVerificationEmail(email, newCode);
                req.setAttribute("message", "Новое письмо с подтверждением отправлено! Проверьте почту.");
                req.setAttribute("email", email);
            } catch (Exception e) {
                req.setAttribute("error", "Ошибка отправки письма: " + e.getMessage());
                req.getRequestDispatcher("/register.jsp").forward(req, resp);
            }
        } else if (user != null && user.isVerified()) {
            req.setAttribute("error", "Этот email уже подтверждён. Вы можете войти в систему.");
            req.getRequestDispatcher("/login.jsp").forward(req, resp);
        } else {
            req.setAttribute("error", "Пользователь с таким email не найден");
            req.getRequestDispatcher("/register.jsp").forward(req, resp);
        }
    }
}