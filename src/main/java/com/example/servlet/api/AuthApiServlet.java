package com.example.servlet.api;

import com.example.dao.UserDAO;
import com.example.model.User;
import com.example.util.JsonUtil;
import com.example.util.PasswordUtil;
import com.example.util.EmailUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@WebServlet("/api/auth/*")
public class AuthApiServlet extends BaseApiServlet {

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String path = req.getPathInfo();

        if ("/login".equals(path)) {
            handleLogin(req, resp);
        } else if ("/register".equals(path)) {
            handleRegister(req, resp);
        } else if ("/logout".equals(path)) {
            handleLogout(req, resp);
        } else {
            sendError(resp, "Эндпоинт не найден", HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String path = req.getPathInfo();
        if ("/me".equals(path)) {
            handleGetCurrentUser(req, resp);
        } else {
            sendError(resp, "Эндпоинт не найден", HttpServletResponse.SC_NOT_FOUND);
        }
    }

    // ========== ОБРАБОТЧИКИ ==========

    private void handleLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Map<String, String> credentials = readJson(req, Map.class);

        String email = credentials.get("email");
        String password = credentials.get("password");

        if (email == null || password == null) {
            sendError(resp, "Email и пароль обязательны", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        User user = userDAO.authenticateUserWithStatus(email, password);

        if (user == null) {
            sendError(resp, "Неверный email или пароль", HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        if (!user.isActive()) {
            sendError(resp, "Аккаунт заблокирован", HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        if (!user.isVerified()) {
            sendError(resp, "Email не подтверждён", HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        HttpSession session = req.getSession(true);
        session.setAttribute("userId", user.getId());
        session.setAttribute("username", user.getUsername());
        session.setAttribute("email", user.getEmail());
        session.setAttribute("authenticated", true);
        session.setAttribute("isAdmin", user.isAdmin());
        session.setMaxInactiveInterval(30 * 60);

        userDAO.updateLastLogin(user.getId());

        Map<String, Object> response = new HashMap<>();
        response.put("id", user.getId());
        response.put("username", user.getUsername());
        response.put("email", user.getEmail());
        response.put("isAdmin", user.isAdmin());
        response.put("rating", user.getScore());

        sendSuccess(resp, "Вход выполнен успешно", response);
    }

    private void handleRegister(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Map<String, String> data = readJson(req, Map.class);

        String email = data.get("email");
        String password = data.get("password");
        String confirmPassword = data.get("confirmPassword");
        String username = data.get("username");

        // Валидация
        if (email == null || !email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            sendError(resp, "Некорректный email", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        if (password == null || password.length() < 6) {
            sendError(resp, "Пароль должен быть не менее 6 символов", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        if (!password.equals(confirmPassword)) {
            sendError(resp, "Пароли не совпадают", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        if (userDAO.emailExists(email)) {
            sendError(resp, "Пользователь с таким email уже существует", HttpServletResponse.SC_CONFLICT);
            return;
        }

        if (username == null || username.trim().isEmpty() || username.length() < 3) {
            username = email.substring(0, email.indexOf('@'));
            username = username.replaceAll("[^a-zA-Z0-9_]", "");
            if (username.isEmpty()) {
                username = "user_" + System.currentTimeMillis();
            }
        }

        String hashedPassword = PasswordUtil.hashPassword(password);
        boolean registered = userDAO.registerUser(username, email, hashedPassword);

        if (registered) {
            String verificationCode = UUID.randomUUID().toString();
            userDAO.saveVerificationCode(email, verificationCode);

            try {
                EmailUtil.sendVerificationEmail(email, verificationCode);
            } catch (Exception e) {
                e.printStackTrace();
            }

            Map<String, String> response = new HashMap<>();
            response.put("message", "Регистрация успешна. Проверьте почту для подтверждения.");
            response.put("email", email);

            sendSuccess(resp, response);
        } else {
            sendError(resp, "Ошибка регистрации", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void handleLogout(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        sendSuccess(resp, "Выход выполнен успешно", null);
    }

    private void handleGetCurrentUser(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Integer userId = requireAuth(req, resp);
        if (userId == null) return;

        User user = userDAO.findById(userId);
        if (user == null) {
            sendError(resp, "Пользователь не найден", HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        Map<String, Object> response = new HashMap<>();
        response.put("id", user.getId());
        response.put("username", user.getUsername());
        response.put("email", user.getEmail());
        response.put("isAdmin", user.isAdmin());
        response.put("rating", user.getScore());
        response.put("isVerified", user.isVerified());
        response.put("bio", user.getBio());
        response.put("city", user.getCity());
        response.put("age", user.getAge());
        response.put("registrationDate", user.getRegistrationDate());
        response.put("lastLogin", user.getLastLogin());

        sendSuccess(resp, response);
    }
}