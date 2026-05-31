package com.example.servlet;

import com.example.dao.ContestDAO;
import com.example.dao.TaskDAO;
import com.example.dao.UserDAO;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
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
        int totalContests = contestDAO.getAllContests((int) session.getAttribute("userId")).size();
        int totalTasks = 0; // нужно добавить метод
        int totalSubmissions = 0; // нужно добавить метод

        req.setAttribute("totalUsers", totalUsers);
        req.setAttribute("totalContests", totalContests);
        req.setAttribute("totalTasks", totalTasks);
        req.setAttribute("totalSubmissions", totalSubmissions);

        req.getRequestDispatcher("/admin/dashboard.jsp").forward(req, resp);
    }
}