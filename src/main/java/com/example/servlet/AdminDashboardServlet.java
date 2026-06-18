package com.example.servlet;

import com.example.dao.ContestDAO;
import com.example.dao.TaskDAO;
import com.example.dao.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/admin")
public class AdminDashboardServlet extends HttpServlet {

    private UserDAO userDAO = new UserDAO();
    private ContestDAO contestDAO = new ContestDAO();
    private TaskDAO taskDAO = new TaskDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        // Проверка прав администратора
        Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");
        if (isAdmin == null || !isAdmin) {
            resp.sendRedirect(req.getContextPath() + "/contests");
            return;
        }

        // Получаем статистику
        int totalUsers = userDAO.getTotalUsersCount();
        int totalContests = contestDAO.getAllContestsForAdmin().size();
        int totalTasks = taskDAO.getAllTasks().size();
        int totalSubmissions = taskDAO.getTotalSubmissionsCount();

        req.setAttribute("totalUsers", totalUsers);
        req.setAttribute("totalContests", totalContests);
        req.setAttribute("totalTasks", totalTasks);
        req.setAttribute("totalSubmissions", totalSubmissions);

        req.getRequestDispatcher("/admin/dashboard.jsp").forward(req, resp);
    }
}