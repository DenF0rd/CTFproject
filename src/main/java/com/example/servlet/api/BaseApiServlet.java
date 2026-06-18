package com.example.servlet.api;

import com.example.util.JsonUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;

public abstract class BaseApiServlet extends HttpServlet {

    protected static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule())
            .disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    /**
     * Проверить авторизацию пользователя
     */
    protected int getUserId(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) {
            return -1;
        }
        Integer userId = (Integer) session.getAttribute("userId");
        return userId != null ? userId : -1;
    }

    /**
     * Проверить, является ли пользователь администратором
     */
    protected boolean isAdmin(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) {
            return false;
        }
        Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");
        return isAdmin != null && isAdmin;
    }

    /**
     * Прочитать JSON из тела запроса
     */
    protected <T> T readJson(HttpServletRequest req, Class<T> clazz) throws IOException {
        StringBuilder sb = new StringBuilder();
        String line;
        try (BufferedReader reader = req.getReader()) {
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return JsonUtil.fromJson(sb.toString(), clazz);
    }

    /**
     * Отправить успешный ответ
     */
    protected void sendSuccess(HttpServletResponse resp, Object data) throws IOException {
        JsonUtil.sendJsonResponse(resp, new JsonUtil.SuccessResponse(data));
    }

    /**
     * Отправить успешный ответ с сообщением
     */
    protected void sendSuccess(HttpServletResponse resp, String message, Object data) throws IOException {
        JsonUtil.sendJsonResponse(resp, new JsonUtil.SuccessResponse(message, data));
    }

    /**
     * Отправить ошибку
     */
    protected void sendError(HttpServletResponse resp, String message, int statusCode) throws IOException {
        JsonUtil.sendError(resp, message, statusCode);
    }

    /**
     * Проверить авторизацию и вернуть userId или null
     */
    protected Integer requireAuth(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        int userId = getUserId(req);
        if (userId == -1) {
            sendError(resp, "Требуется авторизация", HttpServletResponse.SC_UNAUTHORIZED);
            return null;
        }
        return userId;
    }

    /**
     * Проверить права администратора
     */
    protected boolean requireAdmin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!isAdmin(req)) {
            sendError(resp, "Недостаточно прав", HttpServletResponse.SC_FORBIDDEN);
            return false;
        }
        return true;
    }
}