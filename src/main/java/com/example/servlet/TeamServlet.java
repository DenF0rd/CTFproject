package com.example.servlet;

import com.example.dao.TeamDAO;
import com.example.model.Team;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet("/manage-team")
public class TeamServlet extends HttpServlet {

    private TeamDAO teamDAO = new TeamDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        int userId = (int) session.getAttribute("userId");
        int teamId = Integer.parseInt(req.getParameter("teamId"));
        int contestId = Integer.parseInt(req.getParameter("contestId"));

        Team team = teamDAO.getTeamById(teamId);
        if (team == null || team.getCaptainId() != userId) {
            resp.sendRedirect(req.getContextPath() + "/contest?id=" + contestId);
            return;
        }

        // getTeamMembers теперь принимает 1 аргумент (teamId)
        List<Map<String, Object>> members = teamDAO.getTeamMembers(teamId);
        List<Map<String, Object>> requests = teamDAO.getJoinRequests(teamId);

        req.setAttribute("team", team);
        req.setAttribute("members", members);
        req.setAttribute("requests", requests);
        req.setAttribute("contestId", contestId);
        req.getRequestDispatcher("/manage-team.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        int teamId = Integer.parseInt(req.getParameter("teamId"));
        int contestId = Integer.parseInt(req.getParameter("contestId"));
        String action = req.getParameter("action");

        if ("remove".equals(action)) {
            int memberId = Integer.parseInt(req.getParameter("memberId"));
            teamDAO.removeMember(teamId, memberId);
        } else if ("approve".equals(action)) {
            int requestId = Integer.parseInt(req.getParameter("requestId"));
            teamDAO.respondToRequest(requestId, "approved");
        } else if ("decline".equals(action)) {
            int requestId = Integer.parseInt(req.getParameter("requestId"));
            teamDAO.respondToRequest(requestId, "declined");
        } else if ("delete".equals(action)) {
            teamDAO.deleteTeam(teamId);
            resp.sendRedirect(req.getContextPath() + "/contest?id=" + contestId);
            return;
        }

        resp.sendRedirect(req.getContextPath() + "/manage-team?teamId=" + teamId + "&contestId=" + contestId);
    }
}