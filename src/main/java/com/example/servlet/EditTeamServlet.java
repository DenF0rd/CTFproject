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

@WebServlet("/edit-team")
public class EditTeamServlet extends HttpServlet {

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
        String name = req.getParameter("name");
        String description = req.getParameter("description");

        System.out.println("=== EDIT TEAM ===");
        System.out.println("TeamId: " + teamId);
        System.out.println("Name: " + name);
        System.out.println("Description: " + description);
        System.out.println("UserId: " + userId);

        // Проверяем, является ли пользователь капитаном
        Team team = teamDAO.getTeamById(teamId);
        if (team == null) {
            System.out.println("Team not found!");
            session.setAttribute("error", "Команда не найдена");
            resp.sendRedirect(req.getContextPath() + "/teams");
            return;
        }

        if (team.getCaptainId() != userId) {
            System.out.println("User is not captain! CaptainId: " + team.getCaptainId() + ", UserId: " + userId);
            session.setAttribute("error", "Только капитан может редактировать команду");
            resp.sendRedirect(req.getContextPath() + "/teams");
            return;
        }

        // Проверяем, что название не пустое
        if (name == null || name.trim().isEmpty()) {
            session.setAttribute("error", "Название команды не может быть пустым");
            resp.sendRedirect(req.getContextPath() + "/teams");
            return;
        }

        boolean updated = teamDAO.updateTeam(teamId, name.trim(), description);

        if (updated) {
            System.out.println("Team updated successfully!");
            session.setAttribute("success", "Информация о команде обновлена!");
        } else {
            System.out.println("Team update failed!");
            session.setAttribute("error", "Ошибка при обновлении команды");
        }

        resp.sendRedirect(req.getContextPath() + "/teams");
    }
}