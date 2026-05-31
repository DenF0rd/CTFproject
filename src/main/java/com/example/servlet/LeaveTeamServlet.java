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

@WebServlet("/leave-team")
public class LeaveTeamServlet extends HttpServlet {

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
        String teamIdParam = req.getParameter("teamId");

        if (teamIdParam == null) {
            resp.sendRedirect(req.getContextPath() + "/teams");
            return;
        }

        int teamId = Integer.parseInt(teamIdParam);

        // Проверяем, является ли пользователь капитаном
        Team team = teamDAO.getTeamById(teamId);
        if (team != null && team.getCaptainId() == userId) {
            session.setAttribute("error", "Капитан не может покинуть команду. Сначала передайте лидерство другому участнику или удалите команду.");
        } else {
            teamDAO.removeMember(teamId, userId);
            session.setAttribute("success", "Вы покинули команду");
        }

        resp.sendRedirect(req.getContextPath() + "/teams");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // Просто перенаправляем на doGet для обработки
        doGet(req, resp);
    }
}