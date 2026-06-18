package com.example.servlet.api;

import com.example.dao.UserDAO;
import com.example.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/api/scoreboard")
public class ScoreboardApiServlet extends BaseApiServlet {

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        Integer userId = requireAuth(req, resp);
        if (userId == null) return;

        List<User> users = userDAO.getAllUsers();

        List<Map<String, Object>> result = new ArrayList<>();
        int rank = 1;
        int currentUserRank = -1;

        for (User user : users) {
            Map<String, Object> entry = new HashMap<>();
            entry.put("rank", rank);
            entry.put("id", user.getId());
            entry.put("username", user.getUsername());
            entry.put("score", user.getScore());
            entry.put("solvedCount", user.getSolvedCount());
            result.add(entry);

            if (user.getId() == userId) {
                currentUserRank = rank;
            }
            rank++;
        }

        Map<String, Object> response = new HashMap<>();
        response.put("users", result);
        response.put("currentUserRank", currentUserRank);

        sendSuccess(resp, response);
    }
}