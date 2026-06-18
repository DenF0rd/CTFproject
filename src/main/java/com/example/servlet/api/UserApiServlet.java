package com.example.servlet.api;

import com.example.dao.UserDAO;
import com.example.dao.TeamDAO;
import com.example.model.User;
import com.example.model.Team;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/api/users/*")
public class UserApiServlet extends BaseApiServlet {

    private final UserDAO userDAO = new UserDAO();
    private final TeamDAO teamDAO = new TeamDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        Integer userId = requireAuth(req, resp);
        if (userId == null) return;

        String path = req.getPathInfo();

        // GET /api/users/{id}
        if (path != null && path.matches("/\\d+")) {
            int profileId = Integer.parseInt(path.substring(1));
            handleGetUser(req, resp, profileId);
            return;
        }

        // GET /api/users/me/team
        if ("/me/team".equals(path)) {
            handleGetUserTeam(req, resp, userId);
            return;
        }

        sendError(resp, "Эндпоинт не найден", HttpServletResponse.SC_NOT_FOUND);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        Integer userId = requireAuth(req, resp);
        if (userId == null) return;

        String path = req.getPathInfo();

        // PUT /api/users/me
        if ("/me".equals(path)) {
            handleUpdateUser(req, resp, userId);
            return;
        }

        // PUT /api/users/me/username
        if ("/me/username".equals(path)) {
            handleUpdateUsername(req, resp, userId);
            return;
        }

        sendError(resp, "Эндпоинт не найден", HttpServletResponse.SC_NOT_FOUND);
    }

    // ========== ОБРАБОТЧИКИ ==========

    private void handleGetUser(HttpServletRequest req, HttpServletResponse resp, int profileId) throws IOException {
        User user = userDAO.findById(profileId);
        if (user == null) {
            sendError(resp, "Пользователь не найден", HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        int totalScore = userDAO.getUserScore(profileId);
        int solvedCount = userDAO.getUserSolvedCount(profileId);
        int rank = userDAO.getUserRank(profileId);

        Map<String, Object> response = new HashMap<>();
        response.put("id", user.getId());
        response.put("username", user.getUsername());
        response.put("email", user.getEmail());
        response.put("bio", user.getBio());
        response.put("city", user.getCity());
        response.put("age", user.getAge());
        response.put("rating", totalScore);
        response.put("solvedCount", solvedCount);
        response.put("rank", rank);
        response.put("isAdmin", user.isAdmin());
        response.put("registrationDate", user.getRegistrationDate());

        sendSuccess(resp, response);
    }

    private void handleGetUserTeam(HttpServletRequest req, HttpServletResponse resp, int userId) throws IOException {
        Team team = teamDAO.getCurrentUserTeam(userId);
        if (team == null) {
            sendSuccess(resp, null);
            return;
        }

        Map<String, Object> response = new HashMap<>();
        response.put("id", team.getId());
        response.put("name", team.getName());
        response.put("description", team.getDescription());
        response.put("membersCount", team.getMembersCount());
        response.put("totalPoints", team.getTotalPoints());
        response.put("isCaptain", team.isCaptain());

        sendSuccess(resp, response);
    }

    private void handleUpdateUser(HttpServletRequest req, HttpServletResponse resp, int userId) throws IOException {
        Map<String, Object> data = readJson(req, Map.class);

        String bio = (String) data.get("bio");
        String city = (String) data.get("city");
        Integer age = (Integer) data.get("age");

        userDAO.updateProfile(userId, bio, city, age != null ? age : 0);
        sendSuccess(resp, "Профиль обновлён", null);
    }

    private void handleUpdateUsername(HttpServletRequest req, HttpServletResponse resp, int userId) throws IOException {
        Map<String, String> data = readJson(req, Map.class);
        String newUsername = data.get("username");

        if (newUsername == null || newUsername.length() < 3) {
            sendError(resp, "Имя пользователя должно быть не менее 3 символов", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        userDAO.updateUsername(userId, newUsername);

        // Обновляем имя в сессии
        req.getSession().setAttribute("username", newUsername);

        sendSuccess(resp, "Имя пользователя обновлено", null);
    }
}