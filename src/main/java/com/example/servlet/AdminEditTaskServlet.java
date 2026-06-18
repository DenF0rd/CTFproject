package com.example.servlet;

import com.example.dao.ContestDAO;
import com.example.dao.TaskDAO;
import com.example.model.Task;
import com.example.model.Contest;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

@WebServlet("/admin/edit-task")
public class AdminEditTaskServlet extends HttpServlet {

    private TaskDAO taskDAO = new TaskDAO();
    private ContestDAO contestDAO = new ContestDAO();

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

        String idParam = req.getParameter("id");
        String contestIdParam = req.getParameter("contestId");

        List<Contest> contests = contestDAO.getAllContestsForAdmin();
        req.setAttribute("contests", contests);

        if (idParam != null && !idParam.isEmpty()) {
            int taskId = Integer.parseInt(idParam);
            Task task = taskDAO.getTaskById(taskId);
            req.setAttribute("task", task);
        } else if (contestIdParam != null && !contestIdParam.isEmpty()) {
            req.setAttribute("preselectedContestId", Integer.parseInt(contestIdParam));
        }

        req.getRequestDispatcher("/admin/edit-task.jsp").forward(req, resp);
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

        String action = req.getParameter("action");

        if ("create".equals(action)) {
            int contestId = Integer.parseInt(req.getParameter("contestId"));
            String title = req.getParameter("title");
            String description = req.getParameter("description");
            int points = Integer.parseInt(req.getParameter("points"));
            String flag = req.getParameter("flag");
            String hint = req.getParameter("hint");

            // Новые поля для динамической стоимости
            int basePoints = Integer.parseInt(req.getParameter("basePoints"));
            int minPoints = Integer.parseInt(req.getParameter("minPoints"));

            taskDAO.createTask(contestId, title, description, points, flag, hint, basePoints, minPoints);
            session.setAttribute("success", "Задача создана!");

        } else if ("update".equals(action)) {
            int taskId = Integer.parseInt(req.getParameter("taskId"));
            int contestId = Integer.parseInt(req.getParameter("contestId"));
            String title = req.getParameter("title");
            String description = req.getParameter("description");
            int points = Integer.parseInt(req.getParameter("points"));
            String flag = req.getParameter("flag");
            String hint = req.getParameter("hint");
            boolean isActive = "true".equals(req.getParameter("isActive"));

            // Новые поля для динамической стоимости
            int basePoints = Integer.parseInt(req.getParameter("basePoints"));
            int minPoints = Integer.parseInt(req.getParameter("minPoints"));

            taskDAO.updateTask(taskId, contestId, title, description, points, flag, hint, isActive, basePoints, minPoints);
            session.setAttribute("success", "Задача обновлена!");
        }

        resp.sendRedirect(req.getContextPath() + "/admin/tasks");
    }
}