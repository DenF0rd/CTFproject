package com.example.servlet;

import com.example.dao.ContestDAO;
import com.example.model.Contest;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Timestamp;

@WebServlet("/admin/edit-contest")
public class AdminEditContestServlet extends HttpServlet {

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
        if (idParam != null && !idParam.isEmpty()) {
            int contestId = Integer.parseInt(idParam);
            Contest contest = contestDAO.getContestById(contestId, (int) session.getAttribute("userId"));
            req.setAttribute("contest", contest);
        }

        req.getRequestDispatcher("/admin/edit-contest.jsp").forward(req, resp);
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
            String title = req.getParameter("title");
            String description = req.getParameter("description");
            String reward = req.getParameter("reward");
            String startTimeStr = req.getParameter("startTime");
            String endTimeStr = req.getParameter("endTime");

            Timestamp startTime = convertToTimestamp(startTimeStr);
            Timestamp endTime = convertToTimestamp(endTimeStr);

            if (startTime != null && endTime != null) {
                contestDAO.createContest(title, description, reward, startTime, endTime);
                session.setAttribute("success", "Соревнование создано!");
            } else {
                session.setAttribute("error", "Ошибка при создании соревнования");
            }

        } else if ("update".equals(action)) {
            int contestId = Integer.parseInt(req.getParameter("contestId"));
            String title = req.getParameter("title");
            String description = req.getParameter("description");
            String reward = req.getParameter("reward");
            String startTimeStr = req.getParameter("startTime");
            String endTimeStr = req.getParameter("endTime");

            Timestamp startTime = convertToTimestamp(startTimeStr);
            Timestamp endTime = convertToTimestamp(endTimeStr);

            if (startTime != null && endTime != null) {
                contestDAO.updateContest(contestId, title, description, reward, startTime, endTime);
                session.setAttribute("success", "Соревнование обновлено!");
            } else {
                session.setAttribute("error", "Ошибка при обновлении соревнования");
            }
        }

        resp.sendRedirect(req.getContextPath() + "/admin/contests");
    }

    private Timestamp convertToTimestamp(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) {
            return null;
        }
        try {
            String formatted = dateTimeStr.replace('T', ' ') + ":00";
            return Timestamp.valueOf(formatted);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return null;
        }
    }
}