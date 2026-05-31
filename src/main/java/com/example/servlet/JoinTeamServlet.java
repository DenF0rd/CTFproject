package com.example.servlet;

import com.example.dao.TeamDAO;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
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