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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/teams")
public class TeamListServlet extends HttpServlet {

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
        String searchQuery = req.getParameter("search");

        // Получаем команды (с поиском или без)
        List<Map<String, Object>> allTeams;
        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            allTeams = teamDAO.searchTeams(searchQuery.trim(), userId);
            req.setAttribute("searchQuery", searchQuery);
        } else {
            allTeams = teamDAO.getAllTeams();
        }

        // Получаем текущую команду пользователя
        Team userTeamObj = teamDAO.getCurrentUserTeam(userId);
        Map<String, Object> userTeam = null;
        boolean isCaptain = false;

        if (userTeamObj != null) {
            userTeam = new HashMap<>();
            userTeam.put("id", userTeamObj.getId());
            userTeam.put("name", userTeamObj.getName());
            userTeam.put("description", userTeamObj.getDescription());
            userTeam.put("captain_id", userTeamObj.getCaptainId());
            userTeam.put("members_count", userTeamObj.getMembersCount());
            userTeam.put("total_points", userTeamObj.getTotalPoints());
            isCaptain = (userTeamObj.getCaptainId() == userId);
        }

        final boolean finalIsCaptain = isCaptain;
        final Map<String, Object> finalUserTeam = userTeam;

        for (Map<String, Object> team : allTeams) {
            if (finalUserTeam != null && team.get("id").equals(finalUserTeam.get("id"))) {
                team.put("isCaptain", finalIsCaptain);
                team.put("isUserTeam", true);
            } else {
                team.put("isCaptain", false);
                team.put("isUserTeam", false);
            }
        }

        req.setAttribute("allTeams", allTeams);
        req.setAttribute("userTeam", userTeam);
        req.setAttribute("userId", userId);

        req.getRequestDispatcher("/teams.jsp").forward(req, resp);
    }
}