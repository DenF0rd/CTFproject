package com.example.servlet;

import com.example.dao.UserDAO;
import com.example.model.User;
import com.example.util.PasswordUtil;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/quick-login")
public class QuickLoginServlet extends HttpServlet {

    private UserDAO userDAO = new UserDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String role = req.getParameter("role");
        User user = null;

        switch (role) {
            case "admin":
                user = getOrCreateAdmin();
                break;
            case "user":
                user = getOrCreateUser();
                break;
            case "test":
                user = getOrCreateTestUser();
                break;
            default:
                resp.sendRedirect(req.getContextPath() + "/login");
                return;
        }

        if (user != null) {
            HttpSession session = req.getSession(true);
            session.setAttribute("userId", user.getId());
            session.setAttribute("username", user.getUsername());
            session.setAttribute("email", user.getEmail());
            session.setAttribute("authenticated", true);
            session.setAttribute("isAdmin", user.isAdmin());
            session.setMaxInactiveInterval(30 * 60);

            System.out.println("Quick login: " + role + " -> " + user.getUsername() + ", isAdmin=" + user.isAdmin());
            resp.sendRedirect(req.getContextPath() + "/contests");
        } else {
            req.setAttribute("error", "Ошибка при быстром входе");
            req.getRequestDispatcher("/login.jsp").forward(req, resp);
        }
    }

    private User getOrCreateAdmin() {
        String email = "admin@ctf.local";
        User user = userDAO.findByEmail(email);
        if (user == null) {
            // Хэшируем пароль перед сохранением
            String hashedPassword = PasswordUtil.hashPassword("admin123");
            userDAO.registerUser("Administrator", email, hashedPassword);
            user = userDAO.findByEmail(email);
            if (user != null) {
                userDAO.verifyEmailSimple(email);
                userDAO.updateUserRating(user.getId(), 1000);
                userDAO.makeAdmin(user.getId());
            }
        }
        // Обновляем объект user, чтобы получить актуальные данные
        if (user != null) {
            user = userDAO.findById(user.getId());
        }
        return user;
    }

    private User getOrCreateUser() {
        String email = "player@ctf.local";
        User user = userDAO.findByEmail(email);
        if (user == null) {
            // Хэшируем пароль перед сохранением
            String hashedPassword = PasswordUtil.hashPassword("player123");
            userDAO.registerUser("Player", email, hashedPassword);
            user = userDAO.findByEmail(email);
            if (user != null) {
                userDAO.verifyEmailSimple(email);
                userDAO.updateUserRating(user.getId(), 100);
            }
        }
        if (user != null) {
            user = userDAO.findById(user.getId());
        }
        return user;
    }

    private User getOrCreateTestUser() {
        String email = "test@ctf.local";
        User user = userDAO.findByEmail(email);
        if (user == null) {
            // Хэшируем пароль перед сохранением
            String hashedPassword = PasswordUtil.hashPassword("test123");
            userDAO.registerUser("Tester", email, hashedPassword);
            user = userDAO.findByEmail(email);
            if (user != null) {
                userDAO.verifyEmailSimple(email);
            }
        }
        if (user != null) {
            user = userDAO.findById(user.getId());
        }
        return user;
    }
}