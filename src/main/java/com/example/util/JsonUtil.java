package com.example.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class JsonUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    /**
     * Преобразовать объект в JSON-строку
     */
    public static String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "{}";
        }
    }

    /**
     * Преобразовать JSON-строку в объект
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Отправить JSON-ответ клиенту
     */
    public static void sendJsonResponse(HttpServletResponse resp, Object data) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();
        out.print(toJson(data));
        out.flush();
    }

    /**
     * Отправить JSON-ответ с указанным статусом
     */
    public static void sendJsonResponse(HttpServletResponse resp, Object data, int statusCode) throws IOException {
        resp.setStatus(statusCode);
        sendJsonResponse(resp, data);
    }

    /**
     * Отправить ошибку в формате JSON
     */
    public static void sendError(HttpServletResponse resp, String message, int statusCode) throws IOException {
        ErrorResponse error = new ErrorResponse(statusCode, message);
        sendJsonResponse(resp, error, statusCode);
    }

    /**
     * Класс для стандартного ответа об ошибке
     */
    public static class ErrorResponse {
        private final int status;
        private final String error;
        private final String message;
        private final long timestamp;

        public ErrorResponse(int status, String message) {
            this.status = status;
            this.error = HttpServletResponse.SC_OK == status ? "OK" : "Error";
            this.message = message;
            this.timestamp = System.currentTimeMillis();
        }

        public int getStatus() { return status; }
        public String getError() { return error; }
        public String getMessage() { return message; }
        public long getTimestamp() { return timestamp; }
    }

    /**
     * Класс для стандартного успешного ответа
     */
    public static class SuccessResponse {
        private final boolean success;
        private final String message;
        private final Object data;
        private final long timestamp;

        public SuccessResponse(String message, Object data) {
            this.success = true;
            this.message = message;
            this.data = data;
            this.timestamp = System.currentTimeMillis();
        }

        public SuccessResponse(Object data) {
            this("OK", data);
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public Object getData() { return data; }
        public long getTimestamp() { return timestamp; }
    }
}