package com.example.servlet;

import com.example.dao.UserDAO;
import com.example.model.User;
import com.example.util.PasswordUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private UserDAO userDAO = new UserDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String email = req.getParameter("email");
        String password = req.getParameter("password");

        System.out.println("=== LOGIN ATTEMPT ===");
        System.out.println("Email: " + email);

        if (email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            req.setAttribute("error", "Заполните все поля");
            req.getRequestDispatcher("/login.jsp").forward(req, resp);
            return;
        }

        User user = userDAO.authenticateUserWithStatus(email, password);

        if (user == null) {
            System.out.println("Login failed: User not found or wrong password");
            req.setAttribute("error", "Неверный email или пароль");
            req.getRequestDispatcher("/login.jsp").forward(req, resp);
            return;
        }

        System.out.println("User found: " + user.getUsername());
        System.out.println("is_active: " + user.isActive());
        System.out.println("is_verified: " + user.isVerified());

        // ПРОВЕРКА НА БЛОКИРОВКУ
        if (!user.isActive()) {
            System.out.println("Login failed: User is BLOCKED - " + email);
            req.setAttribute("error", "❌ Ваш аккаунт заблокирован. Обратитесь к администратору.");
            req.getRequestDispatcher("/login.jsp").forward(req, resp);
            return;
        }

        // Проверка на подтверждение email
        if (!user.isVerified()) {
            System.out.println("Login failed: Email not verified - " + email);
            req.setAttribute("error", "Аккаунт не подтверждён. Проверьте почту для подтверждения.");
            req.getRequestDispatcher("/login.jsp").forward(req, resp);
            return;
        }

        // Успешный вход
        HttpSession session = req.getSession(true);
        session.setAttribute("userId", user.getId());
        session.setAttribute("username", user.getUsername());
        session.setAttribute("email", user.getEmail());
        session.setAttribute("authenticated", true);
        session.setAttribute("isAdmin", user.isAdmin());
        session.setMaxInactiveInterval(30 * 60);

        userDAO.updateLastLogin(user.getId());

        System.out.println("Login successful! User: " + user.getUsername());
        resp.sendRedirect(req.getContextPath() + "/contests");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute("userId") != null) {
            resp.sendRedirect(req.getContextPath() + "/contests");
        } else {
            req.getRequestDispatcher("/login.jsp").forward(req, resp);
        }
    }
}