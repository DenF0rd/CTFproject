package com.example.servlet;

import com.example.dao.ContestDAO;
import com.example.dao.TeamDAO;
import com.example.model.Contest;
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

@WebServlet("/contest")
public class ContestServlet extends HttpServlet {

    private ContestDAO contestDAO = new ContestDAO();
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
        String idParam = req.getParameter("id");

        if (idParam == null || idParam.isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/contests");
            return;
        }

        int contestId = Integer.parseInt(idParam);

        System.out.println("=== CONTEST SERVLET DEBUG ===");
        System.out.println("ContestId: " + contestId);
        System.out.println("UserId: " + userId);

        // Получаем информацию о соревновании
        Contest contest = contestDAO.getContestById(contestId, userId);
        if (contest == null) {
            System.out.println("Contest not found!");
            resp.sendRedirect(req.getContextPath() + "/contests");
            return;
        }

        System.out.println("Contest found: " + contest.getTitle());

        // Получаем задачи соревнования
        List<Map<String, Object>> tasks = contestDAO.getContestTasks(contestId, userId);
        System.out.println("Tasks count: " + (tasks != null ? tasks.size() : 0));

        // Получаем рейтинг
        List<Map<String, Object>> leaderboard = contestDAO.getContestLeaderboard(contestId);
        System.out.println("Leaderboard count: " + (leaderboard != null ? leaderboard.size() : 0));

        // Получаем команду пользователя
        Team userTeam = teamDAO.getTeamByUser(userId);
        System.out.println("User team: " + (userTeam != null ? userTeam.getName() : "null"));

        // Проверяем, участвует ли пользователь
        boolean isJoined = contestDAO.isUserJoined(contestId, userId);
        System.out.println("Is joined: " + isJoined);

        // Статусы прохождения
        boolean isCompletedForUser = contest.isUserCompleted();

        boolean isFinished = contest.getEndTime() != null &&
                contest.getEndTime().before(new java.util.Date());
        req.setAttribute("isFinished", isFinished);

        // Получаем рейтинг ТОЛЬКО для этого соревнования

// Получаем очки текущего пользователя в этом соревновании
        int userContestPoints = contestDAO.getUserContestPoints(userId, contestId);

        req.setAttribute("leaderboard", leaderboard);
        req.setAttribute("userContestPoints", userContestPoints);
        req.setAttribute("contest", contest);
        req.setAttribute("tasks", tasks != null ? tasks : new java.util.ArrayList<>());
        req.setAttribute("leaderboard", leaderboard != null ? leaderboard : new java.util.ArrayList<>());
        req.setAttribute("userTeam", userTeam);
        req.setAttribute("isJoined", isJoined);
        req.setAttribute("contestId", contestId);
        req.setAttribute("isCompletedForUser", isCompletedForUser);
        req.setAttribute("username", session.getAttribute("username"));

        req.getRequestDispatcher("/contest.jsp").forward(req, resp);
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
        String action = req.getParameter("action");
        int contestId = Integer.parseInt(req.getParameter("contestId"));

        if ("join".equals(action)) {
            contestDAO.joinContest(contestId, userId);
        } else if ("leave".equals(action)) {
            contestDAO.leaveContest(contestId, userId);
        } else if ("complete".equals(action)) {
            String updateSql = "UPDATE contests SET is_active = false WHERE id = ?";
            try (java.sql.Connection conn = com.example.util.DBConnection.getConnection();
                 java.sql.PreparedStatement stmt = conn.prepareStatement(updateSql)) {
                stmt.setInt(1, contestId);
                stmt.executeUpdate();
            } catch (java.sql.SQLException e) {
                e.printStackTrace();
            }
        } else if ("activate".equals(action)) {
            String updateSql = "UPDATE contests SET is_active = true WHERE id = ?";
            try (java.sql.Connection conn = com.example.util.DBConnection.getConnection();
                 java.sql.PreparedStatement stmt = conn.prepareStatement(updateSql)) {
                stmt.setInt(1, contestId);
                stmt.executeUpdate();
            } catch (java.sql.SQLException e) {
                e.printStackTrace();
            }
        }

        resp.sendRedirect(req.getContextPath() + "/contest?id=" + contestId);
    }


}