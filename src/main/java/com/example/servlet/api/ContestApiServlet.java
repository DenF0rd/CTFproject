package com.example.servlet.api;

import com.example.dao.ContestDAO;
import com.example.model.Contest;
import com.example.util.JsonUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/api/contests/*")
public class ContestApiServlet extends BaseApiServlet {

    private final ContestDAO contestDAO = new ContestDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        Integer userId = requireAuth(req, resp);
        if (userId == null) return;

        String path = req.getPathInfo();

        // GET /api/contests
        if (path == null || "/".equals(path)) {
            handleGetAllContests(req, resp, userId);
            return;
        }

        // GET /api/contests/{id}
        if (path.matches("/\\d+")) {
            int contestId = Integer.parseInt(path.substring(1));
            handleGetContest(req, resp, contestId, userId);
            return;
        }

        // GET /api/contests/{id}/tasks
        if (path.matches("/\\d+/tasks")) {
            int contestId = Integer.parseInt(path.substring(1, path.indexOf("/tasks")));
            handleGetContestTasks(req, resp, contestId, userId);
            return;
        }

        // GET /api/contests/{id}/leaderboard
        if (path.matches("/\\d+/leaderboard")) {
            int contestId = Integer.parseInt(path.substring(1, path.indexOf("/leaderboard")));
            handleGetLeaderboard(req, resp, contestId);
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

        // POST /api/contests/{id}/join
        if (path != null && path.matches("/\\d+/join")) {
            int contestId = Integer.parseInt(path.substring(1, path.indexOf("/join")));
            handleJoinContest(req, resp, contestId, userId);
            return;
        }

        // POST /api/contests/{id}/leave
        if (path != null && path.matches("/\\d+/leave")) {
            int contestId = Integer.parseInt(path.substring(1, path.indexOf("/leave")));
            handleLeaveContest(req, resp, contestId, userId);
            return;
        }

        // POST /api/contests (создание — только для админа)
        if ("/".equals(path) || path == null) {
            if (!requireAdmin(req, resp)) return;
            handleCreateContest(req, resp);
            return;
        }

        sendError(resp, "Эндпоинт не найден", HttpServletResponse.SC_NOT_FOUND);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        if (!requireAdmin(req, resp)) return;

        String path = req.getPathInfo();

        // PUT /api/contests/{id}
        if (path != null && path.matches("/\\d+")) {
            int contestId = Integer.parseInt(path.substring(1));
            handleUpdateContest(req, resp, contestId);
            return;
        }

        sendError(resp, "Эндпоинт не найден", HttpServletResponse.SC_NOT_FOUND);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        if (!requireAdmin(req, resp)) return;

        String path = req.getPathInfo();

        // DELETE /api/contests/{id}
        if (path != null && path.matches("/\\d+")) {
            int contestId = Integer.parseInt(path.substring(1));
            handleDeleteContest(req, resp, contestId);
            return;
        }

        sendError(resp, "Эндпоинт не найден", HttpServletResponse.SC_NOT_FOUND);
    }

    // ========== ОБРАБОТЧИКИ ==========

    private void handleGetAllContests(HttpServletRequest req, HttpServletResponse resp, int userId) throws IOException {
        List<Contest> contests = contestDAO.getAllContests(userId);
        sendSuccess(resp, contests);
    }

    private void handleGetContest(HttpServletRequest req, HttpServletResponse resp, int contestId, int userId) throws IOException {
        Contest contest = contestDAO.getContestById(contestId, userId);
        if (contest == null) {
            sendError(resp, "Соревнование не найдено", HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        sendSuccess(resp, contest);
    }

    private void handleGetContestTasks(HttpServletRequest req, HttpServletResponse resp, int contestId, int userId) throws IOException {
        List<Map<String, Object>> tasks = contestDAO.getContestTasks(contestId, userId);
        sendSuccess(resp, tasks);
    }

    private void handleGetLeaderboard(HttpServletRequest req, HttpServletResponse resp, int contestId) throws IOException {
        List<Map<String, Object>> leaderboard = contestDAO.getContestLeaderboard(contestId);
        sendSuccess(resp, leaderboard);
    }

    private void handleJoinContest(HttpServletRequest req, HttpServletResponse resp, int contestId, int userId) throws IOException {
        if (contestDAO.isUserJoined(contestId, userId)) {
            sendError(resp, "Вы уже присоединились к этому соревнованию", HttpServletResponse.SC_CONFLICT);
            return;
        }

        if (contestDAO.joinContest(contestId, userId)) {
            sendSuccess(resp, "Вы присоединились к соревнованию", null);
        } else {
            sendError(resp, "Ошибка при присоединении", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void handleLeaveContest(HttpServletRequest req, HttpServletResponse resp, int contestId, int userId) throws IOException {
        if (!contestDAO.isUserJoined(contestId, userId)) {
            sendError(resp, "Вы не участвуете в этом соревновании", HttpServletResponse.SC_CONFLICT);
            return;
        }

        if (contestDAO.leaveContest(contestId, userId)) {
            sendSuccess(resp, "Вы покинули соревнование", null);
        } else {
            sendError(resp, "Ошибка при выходе", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void handleCreateContest(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Map<String, Object> data = readJson(req, Map.class);

        String title = (String) data.get("title");
        String description = (String) data.get("description");
        String reward = (String) data.get("reward");
        String startTimeStr = (String) data.get("startTime");
        String endTimeStr = (String) data.get("endTime");

        if (title == null || title.trim().isEmpty()) {
            sendError(resp, "Название обязательно", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            Timestamp startTime = Timestamp.valueOf(startTimeStr.replace("T", " "));
            Timestamp endTime = Timestamp.valueOf(endTimeStr.replace("T", " "));

            boolean created = contestDAO.createContest(title, description, reward, startTime, endTime);
            if (created) {
                sendSuccess(resp, "Соревнование создано", null);
            } else {
                sendError(resp, "Ошибка при создании", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e) {
            sendError(resp, "Неверный формат даты. Используйте ISO 8601 (YYYY-MM-DDTHH:mm:ss)",
                    HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    private void handleUpdateContest(HttpServletRequest req, HttpServletResponse resp, int contestId) throws IOException {
        Map<String, Object> data = readJson(req, Map.class);

        String title = (String) data.get("title");
        String description = (String) data.get("description");
        String reward = (String) data.get("reward");
        String startTimeStr = (String) data.get("startTime");
        String endTimeStr = (String) data.get("endTime");

        try {
            Timestamp startTime = Timestamp.valueOf(startTimeStr.replace("T", " "));
            Timestamp endTime = Timestamp.valueOf(endTimeStr.replace("T", " "));

            boolean updated = contestDAO.updateContest(contestId, title, description, reward, startTime, endTime);
            if (updated) {
                sendSuccess(resp, "Соревнование обновлено", null);
            } else {
                sendError(resp, "Ошибка при обновлении", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e) {
            sendError(resp, "Неверный формат даты", HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    private void handleDeleteContest(HttpServletRequest req, HttpServletResponse resp, int contestId) throws IOException {
        if (contestDAO.deleteContest(contestId)) {
            sendSuccess(resp, "Соревнование удалено", null);
        } else {
            sendError(resp, "Ошибка при удалении", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}