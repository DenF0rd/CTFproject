package com.example.servlet;

import com.example.dao.TeamDAO;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/delete-team")
public class DeleteTeamServlet extends HttpServlet {

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

        System.out.println("=== DELETE TEAM ===");
        System.out.println("TeamId: " + teamId);
        System.out.println("UserId: " + userId);

        boolean deleted = teamDAO.deleteTeam(teamId, userId);

        if (deleted) {
            System.out.println("Team deleted successfully!");
            session.setAttribute("success", "Команда успешно удалена!");
        } else {
            System.out.println("Team deletion failed!");
            session.setAttribute("error", "Ошибка при удалении команды. Только капитан может удалить команду.");
        }

        resp.sendRedirect(req.getContextPath() + "/teams");
    }
}