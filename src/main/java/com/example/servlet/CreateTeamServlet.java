package com.example.servlet;

import com.example.dao.TeamDAO;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.UUID;

@WebServlet("/create-team")
public class CreateTeamServlet extends HttpServlet {

    private TeamDAO teamDAO = new TeamDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        int contestId = Integer.parseInt(req.getParameter("contestId"));
        req.setAttribute("contestId", contestId);
        req.getRequestDispatcher("/create-team.jsp").forward(req, resp);
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

        // Проверяем, не состоит ли пользователь уже в команде
        if (teamDAO.isUserInAnyTeam(userId)) {
            session.setAttribute("error", "Вы уже состоите в команде. Нельзя создать новую команду.");
            resp.sendRedirect(req.getContextPath() + "/teams");
            return;
        }

        String name = req.getParameter("name");
        String description = req.getParameter("description");
        int contestId = Integer.parseInt(req.getParameter("contestId"));
        String inviteCode = UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        boolean created = teamDAO.createTeam(name, description, userId, inviteCode);

        if (created) {
            session.setAttribute("success", "Команда успешно создана!");
            resp.sendRedirect(req.getContextPath() + "/contest?id=" + contestId + "&tab=team");
        } else {
            req.setAttribute("error", "Ошибка при создании команды. Возможно, имя уже существует.");
            req.setAttribute("contestId", contestId);
            req.getRequestDispatcher("/create-team.jsp").forward(req, resp);
        }
    }
}