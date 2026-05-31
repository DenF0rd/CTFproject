package com.example.servlet;

import com.example.dao.TeamDAO;
import com.example.model.Team;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet("/team-members")
public class TeamMembersServlet extends HttpServlet {

    private TeamDAO teamDAO = new TeamDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        int currentUserId = (int) session.getAttribute("userId");
        String teamIdParam = req.getParameter("teamId");

        if (teamIdParam == null) {
            resp.sendRedirect(req.getContextPath() + "/teams");
            return;
        }

        int teamId = Integer.parseInt(teamIdParam);

        // Получаем информацию о команде
        Team team = teamDAO.getTeamById(teamId);
        if (team == null) {
            resp.sendRedirect(req.getContextPath() + "/teams");
            return;
        }

        // Получаем участников команды
        List<Map<String, Object>> members = teamDAO.getTeamMembersWithDetails(teamId);

        // Проверяем, является ли текущий пользователь капитаном
        boolean isCaptain = (team.getCaptainId() == currentUserId);

        req.setAttribute("team", team);
        req.setAttribute("members", members);
        req.setAttribute("isCaptain", isCaptain);
        req.setAttribute("currentUserId", currentUserId);

        req.getRequestDispatcher("/team-members.jsp").forward(req, resp);
    }
}