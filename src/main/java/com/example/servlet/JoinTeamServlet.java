package com.example.servlet;

import com.example.dao.TeamDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/join-team")
public class JoinTeamServlet extends HttpServlet {

    private TeamDAO teamDAO = new TeamDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        int userId = (int) session.getAttribute("userId");
        int teamId = Integer.parseInt(req.getParameter("teamId"));
        int contestId = Integer.parseInt(req.getParameter("contestId"));
        String message = req.getParameter("message");

        // Добавляем пользователя напрямую (без заявки)
        boolean added = teamDAO.addMember(teamId, userId);

        if (added) {
            resp.sendRedirect(req.getContextPath() + "/contest?id=" + contestId + "&tab=team");
        } else {
            req.setAttribute("error", "Не удалось присоединиться к команде");
            req.getRequestDispatcher("/contest?id=" + contestId).forward(req, resp);
        }
    }
}