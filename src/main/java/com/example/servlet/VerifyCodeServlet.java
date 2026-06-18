package com.example.servlet;

import com.example.dao.UserDAO;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/verify-code")
public class VerifyCodeServlet extends HttpServlet {

    private UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String email = req.getParameter("email");
        if (email != null && !email.isEmpty()) {
            req.setAttribute("email", email);
        }
        req.getRequestDispatcher("/verify-code.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String email = req.getParameter("email");
        String code = req.getParameter("code");

        if (email == null || email.trim().isEmpty() || code == null || code.trim().isEmpty()) {
            req.setAttribute("error", "Email и код обязательны");
            req.getRequestDispatcher("/verify-code.jsp").forward(req, resp);
            return;
        }

        // Проверяем код
        boolean verified = userDAO.verifyEmailByCode(email, code.trim());

        if (verified) {
            // Успешно подтверждён
            req.setAttribute("message", "Email успешно подтверждён! Теперь вы можете войти.");
            req.getRequestDispatcher("/login.jsp").forward(req, resp);
        } else {
            req.setAttribute("error", "Неверный код или срок его действия истёк. Попробуйте снова.");
            req.setAttribute("email", email);
            req.getRequestDispatcher("/verify-code.jsp").forward(req, resp);
        }
    }
}