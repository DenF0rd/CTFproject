package com.example.servlet;

import com.example.dao.ContestDAO;
import com.example.dao.TaskDAO;
import com.example.model.Contest;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@WebServlet("/task")
public class TaskServlet extends HttpServlet {

    private TaskDAO taskDAO = new TaskDAO();
    private ContestDAO contestDAO = new ContestDAO();

    private static final ConcurrentMap<String, Long> processedRequests = new ConcurrentHashMap<>();
    private static final long REQUEST_TIMEOUT = 30000;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        int userId = (int) session.getAttribute("userId");
        String taskIdParam = req.getParameter("id");
        String contestIdParam = req.getParameter("contestId");

        if (taskIdParam == null || contestIdParam == null) {
            resp.sendRedirect(req.getContextPath() + "/contests");
            return;
        }

        int taskId = Integer.parseInt(taskIdParam);
        int contestId = Integer.parseInt(contestIdParam);

        // Проверяем, активно ли соревнование и участвует ли пользователь
        Contest contest = contestDAO.getContestById(contestId, userId);
        if (contest == null) {
            resp.sendRedirect(req.getContextPath() + "/contests");
            return;
        }

        // ===== ПРОВЕРКА: соревнование ещё не началось =====
        Date now = new Date();
        boolean isUpcoming = contest.getStartTime() != null && now.before(contest.getStartTime());
        if (isUpcoming) {
            session.setAttribute("taskError", "❌ Это соревнование ещё не началось. Старт: " + contest.getStartTime());
            resp.sendRedirect(req.getContextPath() + "/contest?id=" + contestId);
            return;
        }
        // ==================================================

        // Проверяем, участвует ли пользователь в соревновании
        boolean isJoined = contestDAO.isUserJoined(contestId, userId);
        if (!isJoined) {
            session.setAttribute("taskError", "❌ Вы не участвуете в этом соревновании. Присоединитесь, чтобы решать задачи.");
            resp.sendRedirect(req.getContextPath() + "/contest?id=" + contestId);
            return;
        }

        // Проверяем, активно ли соревнование (не завершено)
        boolean isFinished = contest.getEndTime() != null &&
                contest.getEndTime().before(now);
        if (isFinished) {
            session.setAttribute("taskError", "❌ Это соревнование завершено. Нельзя решать задачи.");
            resp.sendRedirect(req.getContextPath() + "/contest?id=" + contestId);
            return;
        }

        Map<String, Object> task = taskDAO.getTaskById(taskId, userId);
        if (task == null) {
            resp.sendRedirect(req.getContextPath() + "/contest?id=" + contestId);
            return;
        }

        // Получаем очки, которые пользователь реально получил за эту задачу
        int userEarnedPoints = 0;
        boolean isSolved = task != null && (boolean) task.get("is_solved");
        if (isSolved) {
            userEarnedPoints = taskDAO.getPointsEarnedForTask(userId, taskId);
        }
        req.setAttribute("userEarnedPoints", userEarnedPoints);

        List<Map<String, Object>> submissionHistory = taskDAO.getSubmissionHistory(taskId, userId);

        String message = (String) session.getAttribute("taskMessage");
        String error = (String) session.getAttribute("taskError");
        session.removeAttribute("taskMessage");
        session.removeAttribute("taskError");

        req.setAttribute("task", task);
        req.setAttribute("contest", contest);
        req.setAttribute("submissionHistory", submissionHistory);
        req.setAttribute("contestId", contestId);
        req.setAttribute("message", message);
        req.setAttribute("error", error);

        req.getRequestDispatcher("/task.jsp").forward(req, resp);
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
        int taskId = Integer.parseInt(req.getParameter("taskId"));
        int contestId = Integer.parseInt(req.getParameter("contestId"));
        String flag = req.getParameter("flag");

        // Проверяем, активно ли соревнование
        Contest contest = contestDAO.getContestById(contestId, userId);
        if (contest == null) {
            resp.sendRedirect(req.getContextPath() + "/contests");
            return;
        }

        // ===== ПРОВЕРКА: соревнование ещё не началось =====
        Date now = new Date();
        boolean isUpcoming = contest.getStartTime() != null && now.before(contest.getStartTime());
        if (isUpcoming) {
            session.setAttribute("taskError", "❌ Это соревнование ещё не началось. Старт: " + contest.getStartTime());
            resp.sendRedirect(req.getContextPath() + "/contest?id=" + contestId);
            return;
        }
        // ==================================================

        boolean isFinished = contest.getEndTime() != null &&
                contest.getEndTime().before(now);
        if (isFinished) {
            session.setAttribute("taskError", "❌ Это соревнование завершено. Нельзя решать задачи.");
            resp.sendRedirect(req.getContextPath() + "/contest?id=" + contestId);
            return;
        }

        // Проверяем, участвует ли пользователь в соревновании
        boolean isJoined = contestDAO.isUserJoined(contestId, userId);
        if (!isJoined) {
            session.setAttribute("taskError", "❌ Вы не участвуете в этом соревновании. Присоединитесь, чтобы решать задачи.");
            resp.sendRedirect(req.getContextPath() + "/contest?id=" + contestId);
            return;
        }

        // Уникальный ключ для этого запроса (защита от двойных кликов)
        String requestKey = userId + "_" + taskId + "_" + flag.hashCode();
        Long lastProcessed = processedRequests.get(requestKey);
        long nowTime = System.currentTimeMillis();

        if (lastProcessed != null && (nowTime - lastProcessed) < REQUEST_TIMEOUT) {
            resp.sendRedirect(req.getContextPath() + "/task?id=" + taskId + "&contestId=" + contestId);
            return;
        }

        processedRequests.put(requestKey, nowTime);

        if (processedRequests.size() > 1000) {
            processedRequests.entrySet().removeIf(entry -> (nowTime - entry.getValue()) > REQUEST_TIMEOUT);
        }

        try {
            // Проверяем, не решена ли уже задача
            if (taskDAO.isTaskSolved(userId, taskId)) {
                session.setAttribute("taskError", "❌ Вы уже решили эту задачу!");
                resp.sendRedirect(req.getContextPath() + "/task?id=" + taskId + "&contestId=" + contestId);
                return;
            }

            String ipAddress = req.getRemoteAddr();
            String userAgent = req.getHeader("User-Agent");

            Object[] result = taskDAO.checkFlag(taskId, flag, userId, ipAddress, userAgent);
            boolean isCorrect = (boolean) result[0];
            int pointsAwarded = (int) result[1];
            String message = (String) result[2];

            if (isCorrect) {
                //contestDAO.updateUserContestPoints(userId, contestId, pointsAwarded);
                // Пересчёт стоимости задачи
                taskDAO.recalculateTaskPoints(taskId);
                session.setAttribute("taskMessage", "✅ " + message + " +" + pointsAwarded + " очков!");
            } else {
                session.setAttribute("taskError", "❌ " + message);
            }

        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("taskError", "❌ Ошибка при проверке флага");
        }

        resp.sendRedirect(req.getContextPath() + "/task?id=" + taskId + "&contestId=" + contestId);
    }
}