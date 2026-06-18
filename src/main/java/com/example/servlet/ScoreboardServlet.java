package com.example.servlet;

import com.example.dao.UserDAO;
import com.example.model.User;
import com.example.util.RedisCache;
import com.fasterxml.jackson.core.type.TypeReference;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

@WebServlet("/scoreboard")
public class ScoreboardServlet extends HttpServlet {

    private UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        int currentUserId = (int) session.getAttribute("userId");

        // Получаем список пользователей из кэша или БД
        String cacheKey = "scoreboard_users";
        List<User> users = RedisCache.get(cacheKey, new TypeReference<List<User>>() {});

        if (users == null) {
            System.out.println("Redis MISS: " + cacheKey + " - loading from DB");
            users = userDAO.getAllUsers();
            RedisCache.put(cacheKey, users, 30);
        } else {
            System.out.println("Redis HIT: " + cacheKey);
        }

        User currentUser = userDAO.findById(currentUserId);

        int currentRank = 1;
        for (User user : users) {
            if (user.getId() == currentUserId) {
                break;
            }
            currentRank++;
        }

        req.setAttribute("users", users);
        req.setAttribute("currentUser", currentUser);
        req.setAttribute("currentRank", currentRank);
        req.setAttribute("currentUserId", currentUserId);

        req.getRequestDispatcher("/scoreboard.jsp").forward(req, resp);
    }
}