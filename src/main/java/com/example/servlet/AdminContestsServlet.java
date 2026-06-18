package com.example.servlet;

import com.example.dao.ContestDAO;
import com.example.dao.TaskDAO;
import com.example.model.Contest;
import com.example.model.Task;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@WebServlet("/admin/contests")
public class AdminContestsServlet extends HttpServlet {

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

        Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");
        if (isAdmin == null || !isAdmin) {
            resp.sendRedirect(req.getContextPath() + "/contests");
            return;
        }

        int userId = (int) session.getAttribute("userId");
        String action = req.getParameter("action");
        String contestIdParam = req.getParameter("contestId");

        if ("edit".equals(action) && contestIdParam != null) {
            // Редактирование соревнования
            int contestId = Integer.parseInt(contestIdParam);
            Contest contest = contestDAO.getContestById(contestId, userId);
            List<com.example.model.Task> tasks = taskDAO.getTasksByContestId(contestId);

            req.setAttribute("contest", contest);
            req.setAttribute("tasks", tasks);
            req.setAttribute("isEdit", true);
            req.getRequestDispatcher("/admin/edit-contest.jsp").forward(req, resp);
            return;
        } else if ("create".equals(action)) {
            // Создание нового соревнования
            req.setAttribute("isEdit", false);
            req.getRequestDispatcher("/admin/edit-contest.jsp").forward(req, resp);
            return;
        }

        // Список всех соревнований
        List<Contest> contests = contestDAO.getAllContestsForAdmin();
        req.setAttribute("contests", contests);
        req.getRequestDispatcher("/admin/contests.jsp").forward(req, resp);
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
            int contestId = Integer.parseInt(req.getParameter("contestId"));
            contestDAO.deleteContest(contestId);
            session.setAttribute("success", "Соревнование удалено!");
        }

        resp.sendRedirect(req.getContextPath() + "/admin/contests");
    }
}