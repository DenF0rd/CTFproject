package com.example.servlet;

import com.example.dao.TaskDAO;
import com.example.dao.ContestDAO;
import com.example.model.Task;
import com.example.model.Contest;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

@WebServlet("/admin/tasks")
public class AdminTasksServlet extends HttpServlet {

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

        String contestIdParam = req.getParameter("contestId");
        List<Task> tasks;

        if (contestIdParam != null && !contestIdParam.isEmpty()) {
            tasks = taskDAO.getTasksByContestId(Integer.parseInt(contestIdParam));
            req.setAttribute("filterContestId", Integer.parseInt(contestIdParam));
        } else {
            tasks = taskDAO.getAllTasks();
        }

        List<Contest> contests = contestDAO.getAllContestsForAdmin();

        req.setAttribute("tasks", tasks);
        req.setAttribute("contests", contests);
        req.getRequestDispatcher("/admin/tasks.jsp").forward(req, resp);
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

        if ("delete".equals(action)) {
            int taskId = Integer.parseInt(req.getParameter("taskId"));
            taskDAO.deleteTask(taskId);
            session.setAttribute("success", "Задача удалена!");
        }

        resp.sendRedirect(req.getContextPath() + "/admin/tasks");
    }
}