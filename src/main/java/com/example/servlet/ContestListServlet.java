package com.example.servlet;

import com.example.dao.ContestDAO;
import com.example.model.Contest;
import com.example.util.RedisCache;
import com.fasterxml.jackson.core.type.TypeReference;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

@WebServlet("/contests")
public class ContestListServlet extends HttpServlet {

    private ContestDAO contestDAO = new ContestDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        int userId = (int) session.getAttribute("userId");

        // Используем кэширование через ContestDAO
        List<Contest> activeContests = contestDAO.getActiveContests(userId);
        List<Contest> upcomingContests = contestDAO.getUpcomingContests(userId);
        List<Contest> pastContests = contestDAO.getPastContests(userId);

        req.setAttribute("activeContests", activeContests);
        req.setAttribute("upcomingContests", upcomingContests);
        req.setAttribute("pastContests", pastContests);
        req.setAttribute("username", session.getAttribute("username"));

        req.getRequestDispatcher("/contests.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        int userId = (int) session.getAttribute("userId");
        String action = req.getParameter("action");
        int contestId = Integer.parseInt(req.getParameter("contestId"));

        if ("join".equals(action)) {
            contestDAO.joinContest(contestId, userId);
        } else if ("leave".equals(action)) {
            contestDAO.leaveContest(contestId, userId);
        }

        resp.sendRedirect(req.getContextPath() + "/contests");
    }
}