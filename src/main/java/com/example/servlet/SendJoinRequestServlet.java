package com.example.servlet;

import com.example.dao.TeamDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/send-join-request")
public class SendJoinRequestServlet extends HttpServlet {

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

        if (teamIdParam == null || teamIdParam.isEmpty()) {
            session.setAttribute("error", "Не указана команда");
            resp.sendRedirect(req.getContextPath() + "/teams");
            return;
        }

        int teamId = Integer.parseInt(teamIdParam);

        // Проверяем, не состоит ли пользователь уже в какой-либо команде
        if (teamDAO.isUserInAnyTeam(userId)) {
            session.setAttribute("error", "Вы уже состоите в команде. Нельзя вступить в другую команду.");
            resp.sendRedirect(req.getContextPath() + "/teams");
            return;
        }

        // Проверяем, есть ли место в команде
        if (!teamDAO.hasTeamSpace(teamId)) {
            session.setAttribute("error", "Команда заполнена (максимум 5 участников)");
            resp.sendRedirect(req.getContextPath() + "/teams");
            return;
        }

        // Добавляем пользователя в команду
        boolean added = teamDAO.addMember(teamId, userId);

        if (added) {
            session.setAttribute("success", "Вы успешно вступили в команду!");
        } else {
            session.setAttribute("error", "Не удалось вступить в команду");
        }

        resp.sendRedirect(req.getContextPath() + "/teams");
    }
}