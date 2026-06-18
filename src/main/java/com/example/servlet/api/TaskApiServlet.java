package com.example.servlet.api;

import com.example.dao.ContestDAO;
import com.example.dao.TaskDAO;
import com.example.model.Contest;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/api/tasks/*")
public class TaskApiServlet extends BaseApiServlet {

    private final TaskDAO taskDAO = new TaskDAO();
    private final ContestDAO contestDAO = new ContestDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        Integer userId = requireAuth(req, resp);
        if (userId == null) return;

        String path = req.getPathInfo();

        // GET /api/tasks/{id}
        if (path != null && path.matches("/\\d+")) {
            int taskId = Integer.parseInt(path.substring(1));
            handleGetTask(req, resp, taskId, userId);
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

        // POST /api/tasks/{id}/submit
        if (path != null && path.matches("/\\d+/submit")) {
            int taskId = Integer.parseInt(path.substring(1, path.indexOf("/submit")));
            handleSubmitFlag(req, resp, taskId, userId);
            return;
        }

        sendError(resp, "Эндпоинт не найден", HttpServletResponse.SC_NOT_FOUND);
    }

    // ========== ОБРАБОТЧИКИ ==========

    private void handleGetTask(HttpServletRequest req, HttpServletResponse resp, int taskId, int userId) throws IOException {
        Map<String, Object> task = taskDAO.getTaskById(taskId, userId);
        if (task == null || task.isEmpty()) {
            sendError(resp, "Задача не найдена", HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // Проверяем, участвует ли пользователь в соревновании
        int contestId = (int) task.get("contestId");
        if (!contestDAO.isUserJoined(contestId, userId)) {
            sendError(resp, "Вы не участвуете в этом соревновании", HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        // Проверяем, активно ли соревнование
        Contest contest = contestDAO.getContestById(contestId, userId);
        if (contest == null) {
            sendError(resp, "Соревнование не найдено", HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        boolean isFinished = contest.getEndTime() != null &&
                contest.getEndTime().before(new java.util.Date());
        if (isFinished) {
            sendError(resp, "Соревнование завершено", HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        sendSuccess(resp, task);
    }

    private void handleSubmitFlag(HttpServletRequest req, HttpServletResponse resp, int taskId, int userId) throws IOException {
        Map<String, String> data = readJson(req, Map.class);
        String flag = data.get("flag");

        if (flag == null || flag.trim().isEmpty()) {
            sendError(resp, "Флаг не указан", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // Проверяем, не решена ли уже задача
        if (taskDAO.isTaskSolved(userId, taskId)) {
            sendError(resp, "Вы уже решили эту задачу", HttpServletResponse.SC_CONFLICT);
            return;
        }

        // Получаем задачу, чтобы проверить contestId
        Map<String, Object> task = taskDAO.getTaskById(taskId, userId);
        if (task == null || task.isEmpty()) {
            sendError(resp, "Задача не найдена", HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        int contestId = (int) task.get("contestId");

        // Проверяем участие в соревновании
        if (!contestDAO.isUserJoined(contestId, userId)) {
            sendError(resp, "Вы не участвуете в этом соревновании", HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        // Проверяем, активно ли соревнование
        Contest contest = contestDAO.getContestById(contestId, userId);
        if (contest == null) {
            sendError(resp, "Соревнование не найдено", HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        boolean isFinished = contest.getEndTime() != null &&
                contest.getEndTime().before(new java.util.Date());
        if (isFinished) {
            sendError(resp, "Соревнование завершено", HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String ipAddress = req.getRemoteAddr();
        String userAgent = req.getHeader("User-Agent");

        Object[] result = taskDAO.checkFlag(taskId, flag.trim(), userId, ipAddress, userAgent);
        boolean isCorrect = (boolean) result[0];
        int pointsAwarded = (int) result[1];
        String message = (String) result[2];

        Map<String, Object> response = new HashMap<>();
        response.put("isCorrect", isCorrect);
        response.put("pointsAwarded", pointsAwarded);
        response.put("message", message);

        if (isCorrect) {
            contestDAO.updateUserContestPoints(userId, contestId, pointsAwarded);
            // Пересчёт стоимости задачи
            taskDAO.recalculateTaskPoints(taskId);
            sendSuccess(resp, response);
        } else {
            sendError(resp, message, HttpServletResponse.SC_BAD_REQUEST);
        }
    }
}