package com.example.servlet;

import com.example.dao.TeamDAO;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/transfer-leadership")
public class TransferLeadershipServlet extends HttpServlet {

    private TeamDAO teamDAO = new TeamDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        int currentUserId = (int) session.getAttribute("userId");
        int teamId = Integer.parseInt(req.getParameter("teamId"));
        int newCaptainId = Integer.parseInt(req.getParameter("newCaptainId"));

        boolean transferred = teamDAO.transferLeadership(teamId, currentUserId, newCaptainId);

        if (transferred) {
            session.setAttribute("success", "Лидерство успешно передано!");
        } else {
            session.setAttribute("error", "Не удалось передать лидерство. Только капитан может передать лидерство.");
        }

        resp.sendRedirect(req.getContextPath() + "/team-members?teamId=" + teamId);
    }
}