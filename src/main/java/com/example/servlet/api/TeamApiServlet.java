package com.example.servlet.api;

import com.example.dao.TeamDAO;
import com.example.model.Team;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@WebServlet("/api/teams/*")
public class TeamApiServlet extends BaseApiServlet {

    private final TeamDAO teamDAO = new TeamDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        Integer userId = requireAuth(req, resp);
        if (userId == null) return;

        String path = req.getPathInfo();

        // GET /api/teams
        if (path == null || "/".equals(path)) {
            handleGetAllTeams(req, resp);
            return;
        }

        // GET /api/teams/{id}
        if (path != null && path.matches("/\\d+")) {
            int teamId = Integer.parseInt(path.substring(1));
            handleGetTeam(req, resp, teamId);
            return;
        }

        // GET /api/teams/{id}/members
        if (path != null && path.matches("/\\d+/members")) {
            int teamId = Integer.parseInt(path.substring(1, path.indexOf("/members")));
            handleGetTeamMembers(req, resp, teamId);
            return;
        }

        // GET /api/teams/me
        if ("/me".equals(path)) {
            handleGetMyTeam(req, resp, userId);
            return;
        }

        sendError(resp, "Эндпоинт не найден", HttpServletResponse.SC_NOT_FOUND);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        Integer userId = requireAuth(req, resp);
        if (userId == null) return;

        String path = req.getPathInfo();

        // POST /api/teams
        if ("/".equals(path) || path == null) {
            handleCreateTeam(req, resp, userId);
            return;
        }

        // POST /api/teams/{id}/join
        if (path != null && path.matches("/\\d+/join")) {
            int teamId = Integer.parseInt(path.substring(1, path.indexOf("/join")));
            handleJoinTeam(req, resp, teamId, userId);
            return;
        }

        // POST /api/teams/{id}/leave
        if (path != null && path.matches("/\\d+/leave")) {
            int teamId = Integer.parseInt(path.substring(1, path.indexOf("/leave")));
            handleLeaveTeam(req, resp, teamId, userId);
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

        // PUT /api/teams/{id}
        if (path != null && path.matches("/\\d+")) {
            int teamId = Integer.parseInt(path.substring(1));
            handleUpdateTeam(req, resp, teamId, userId);
            return;
        }

        // PUT /api/teams/{id}/leadership
        if (path != null && path.matches("/\\d+/leadership")) {
            int teamId = Integer.parseInt(path.substring(1, path.indexOf("/leadership")));
            handleTransferLeadership(req, resp, teamId, userId);
            return;
        }

        sendError(resp, "Эндпоинт не найден", HttpServletResponse.SC_NOT_FOUND);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        Integer userId = requireAuth(req, resp);
        if (userId == null) return;

        String path = req.getPathInfo();

        // DELETE /api/teams/{id}
        if (path != null && path.matches("/\\d+")) {
            int teamId = Integer.parseInt(path.substring(1));
            handleDeleteTeam(req, resp, teamId, userId);
            return;
        }

        sendError(resp, "Эндпоинт не найден", HttpServletResponse.SC_NOT_FOUND);
    }

    // ========== ОБРАБОТЧИКИ ==========

    private void handleGetAllTeams(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        List<Map<String, Object>> teams = teamDAO.getAllTeams();
        sendSuccess(resp, teams);
    }

    private void handleGetTeam(HttpServletRequest req, HttpServletResponse resp, int teamId) throws IOException {
        Team team = teamDAO.getTeamById(teamId);
        if (team == null) {
            sendError(resp, "Команда не найдена", HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        sendSuccess(resp, team);
    }

    private void handleGetTeamMembers(HttpServletRequest req, HttpServletResponse resp, int teamId) throws IOException {
        List<Map<String, Object>> members = teamDAO.getTeamMembersWithDetails(teamId);
        sendSuccess(resp, members);
    }

    private void handleGetMyTeam(HttpServletRequest req, HttpServletResponse resp, int userId) throws IOException {
        Team team = teamDAO.getCurrentUserTeam(userId);
        if (team == null) {
            sendSuccess(resp, null);
            return;
        }
        sendSuccess(resp, team);
    }

    private void handleCreateTeam(HttpServletRequest req, HttpServletResponse resp, int userId) throws IOException {
        Map<String, String> data = readJson(req, Map.class);

        String name = data.get("name");
        String description = data.get("description");

        if (name == null || name.trim().isEmpty()) {
            sendError(resp, "Название команды обязательно", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        if (teamDAO.isUserInAnyTeam(userId)) {
            sendError(resp, "Вы уже состоите в команде", HttpServletResponse.SC_CONFLICT);
            return;
        }

        String inviteCode = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        boolean created = teamDAO.createTeam(name.trim(), description, userId, inviteCode);

        if (created) {
            sendSuccess(resp, "Команда создана", null);
        } else {
            sendError(resp, "Ошибка при создании команды", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void handleJoinTeam(HttpServletRequest req, HttpServletResponse resp, int teamId, int userId) throws IOException {
        if (teamDAO.isUserInAnyTeam(userId)) {
            sendError(resp, "Вы уже состоите в команде", HttpServletResponse.SC_CONFLICT);
            return;
        }

        if (!teamDAO.hasTeamSpace(teamId)) {
            sendError(resp, "Команда заполнена (максимум 5 участников)", HttpServletResponse.SC_CONFLICT);
            return;
        }

        boolean added = teamDAO.addMember(teamId, userId);
        if (added) {
            sendSuccess(resp, "Вы вступили в команду", null);
        } else {
            sendError(resp, "Ошибка при вступлении", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void handleLeaveTeam(HttpServletRequest req, HttpServletResponse resp, int teamId, int userId) throws IOException {
        Team team = teamDAO.getTeamById(teamId);
        if (team == null) {
            sendError(resp, "Команда не найдена", HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        if (team.getCaptainId() == userId) {
            sendError(resp, "Капитан не может покинуть команду. Сначала передайте лидерство.",
                    HttpServletResponse.SC_CONFLICT);
            return;
        }

        teamDAO.removeMember(teamId, userId);
        sendSuccess(resp, "Вы покинули команду", null);
    }

    private void handleUpdateTeam(HttpServletRequest req, HttpServletResponse resp, int teamId, int userId) throws IOException {
        Team team = teamDAO.getTeamById(teamId);
        if (team == null) {
            sendError(resp, "Команда не найдена", HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        if (team.getCaptainId() != userId) {
            sendError(resp, "Только капитан может редактировать команду", HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        Map<String, String> data = readJson(req, Map.class);
        String name = data.get("name");
        String description = data.get("description");

        if (name == null || name.trim().isEmpty()) {
            sendError(resp, "Название команды обязательно", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        boolean updated = teamDAO.updateTeam(teamId, name.trim(), description);
        if (updated) {
            sendSuccess(resp, "Команда обновлена", null);
        } else {
            sendError(resp, "Ошибка при обновлении", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void handleTransferLeadership(HttpServletRequest req, HttpServletResponse resp, int teamId, int userId) throws IOException {
        Team team = teamDAO.getTeamById(teamId);
        if (team == null) {
            sendError(resp, "Команда не найдена", HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        if (team.getCaptainId() != userId) {
            sendError(resp, "Только капитан может передать лидерство", HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        Map<String, Integer> data = readJson(req, Map.class);
        Integer newCaptainId = data.get("newCaptainId");

        if (newCaptainId == null) {
            sendError(resp, "Укажите нового капитана", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        if (!teamDAO.isUserInTeam(teamId, newCaptainId)) {
            sendError(resp, "Пользователь не состоит в команде", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        boolean transferred = teamDAO.transferLeadership(teamId, userId, newCaptainId);
        if (transferred) {
            sendSuccess(resp, "Лидерство передано", null);
        } else {
            sendError(resp, "Ошибка при передаче лидерства", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void handleDeleteTeam(HttpServletRequest req, HttpServletResponse resp, int teamId, int userId) throws IOException {
        boolean deleted = teamDAO.deleteTeam(teamId, userId);
        if (deleted) {
            sendSuccess(resp, "Команда удалена", null);
        } else {
            sendError(resp, "Только капитан может удалить команду", HttpServletResponse.SC_FORBIDDEN);
        }
    }
}