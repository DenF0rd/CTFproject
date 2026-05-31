package com.example.servlet;

import com.example.dao.UserDAO;
import com.example.model.User;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

@WebServlet("/admin/users")
public class AdminUsersServlet extends HttpServlet {

    private UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");
        if (isAdmin == null || !isAdmin) {
            resp.sendRedirect(req.getContextPath() + "/contests");
            return;
        }

        List<User> users = userDAO.getAllUsersWithDetails();
        req.setAttribute("users", users);
        req.getRequestDispatcher("/admin/users.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");
        if (isAdmin == null || !isAdmin) {
            resp.sendRedirect(req.getContextPath() + "/contests");
            return;
        }

        int currentAdminId = (int) session.getAttribute("userId");
        String action = req.getParameter("action");

        System.out.println("Admin action: " + action);

        String userIdParam = req.getParameter("userId");
        if (userIdParam == null) {
            resp.sendRedirect(req.getContextPath() + "/admin/users");
            return;
        }

        int userId = Integer.parseInt(userIdParam);

        // Запрещаем действия над самим собой
        if (userId == currentAdminId) {
            System.out.println("Cannot perform action on self");
            resp.sendRedirect(req.getContextPath() + "/admin/users");
            return;
        }

        // Получаем информацию о целевом пользователе
        User targetUser = userDAO.findById(userId);
        if (targetUser == null) {
            System.out.println("Target user not found");
            resp.sendRedirect(req.getContextPath() + "/admin/users");
            return;
        }

        // Запрещаем действия над другими администраторами
        if (targetUser.isAdmin()) {
            System.out.println("Cannot perform action on another admin");
            resp.sendRedirect(req.getContextPath() + "/admin/users");
            return;
        }

        if ("toggleActive".equals(action)) {
            System.out.println("Toggling active status for user: " + userId);
            boolean result = userDAO.toggleUserActive(userId);
            System.out.println("Toggle result: " + result);
        }

        resp.sendRedirect(req.getContextPath() + "/admin/users");
    }
}