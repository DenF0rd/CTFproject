package com.example.servlet;

import com.example.dao.UserDAO;
import com.example.util.EmailUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/resend-code")
public class ResendCodeServlet extends HttpServlet {

    private UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String email = req.getParameter("email");

        if (email == null || email.trim().isEmpty()) {
            req.setAttribute("error", "Email не указан");
            req.getRequestDispatcher("/verify-code.jsp").forward(req, resp);
            return;
        }

        // Проверяем, существует ли пользователь и не подтверждён ли он
        if (userDAO.emailExists(email) && !userDAO.isEmailVerified(email)) {
            String newCode = EmailUtil.generateVerificationCode();
            userDAO.saveVerificationCode(email, newCode);

            try {
                EmailUtil.sendVerificationEmail(email, newCode);
                req.setAttribute("message", "Новый код отправлен на вашу почту");
                req.setAttribute("email", email);
            } catch (Exception e) {
                req.setAttribute("error", "Ошибка отправки кода: " + e.getMessage());
            }
        } else {
            req.setAttribute("error", "Пользователь с таким email не найден или уже подтверждён");
        }

        req.getRequestDispatcher("/verify-code.jsp").forward(req, resp);
    }
}