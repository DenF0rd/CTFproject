package com.example.servlet;

import com.example.dao.UserDAO;
import com.example.model.User;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
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

        // Получаем всех пользователей, отсортированных по рейтингу
        List<User> users = userDAO.getAllUsers();

        // Получаем текущего пользователя
        int currentUserId = (int) session.getAttribute("userId");
        User currentUser = userDAO.findById(currentUserId);

        // Находим место текущего пользователя
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