package com.example.servlet;

import com.example.dao.TeamDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.UUID;

@WebServlet("/create-team-global")
public class CreateTeamGlobalServlet extends HttpServlet {

    private TeamDAO teamDAO = new TeamDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        req.getRequestDispatcher("/create-team-global.jsp").forward(req, resp);
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
        String name = req.getParameter("name");
        String description = req.getParameter("description");

        // Генерируем уникальный код приглашения
        String inviteCode = UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // Передаём 4 аргумента: name, description, captainId, inviteCode
        boolean created = teamDAO.createTeam(name, description, userId, inviteCode);

        if (created) {
            System.out.println("Team created successfully! Invite code: " + inviteCode);
            resp.sendRedirect(req.getContextPath() + "/profile?id=" + userId);
        } else {
            req.setAttribute("error", "Ошибка при создании команды");
            req.getRequestDispatcher("/create-team-global.jsp").forward(req, resp);
        }
    }
}