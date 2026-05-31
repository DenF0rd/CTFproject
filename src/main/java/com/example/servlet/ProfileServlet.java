package com.example.servlet;

import com.example.dao.UserDAO;
import com.example.dao.TeamDAO;
import com.example.model.User;
import com.example.model.Team;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet("/profile")
public class ProfileServlet extends HttpServlet {

    private UserDAO userDAO = new UserDAO();
    private TeamDAO teamDAO = new TeamDAO();
    private static final String AVATAR_DIR = "uploads/avatars/";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        int currentUserId = (int) session.getAttribute("userId");
        String idParam = req.getParameter("id");
        int profileId = (idParam != null && !idParam.isEmpty()) ? Integer.parseInt(idParam) : currentUserId;
        boolean isOwnProfile = (profileId == currentUserId);

        User user = userDAO.findById(profileId);
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/contests");
            return;
        }

        int totalScore = userDAO.getUserScore(profileId);
        int solvedCount = userDAO.getUserSolvedCount(profileId);
        int rank = userDAO.getUserRank(profileId);

        // Получаем текущую команду пользователя (только одну)
        Team userTeam = teamDAO.getCurrentUserTeam(profileId);
        boolean isCaptain = (userTeam != null && userTeam.getCaptainId() == profileId);

        req.setAttribute("profileUser", user);
        req.setAttribute("isOwnProfile", isOwnProfile);
        req.setAttribute("totalScore", totalScore);
        req.setAttribute("solvedCount", solvedCount);
        req.setAttribute("rank", rank);
        req.setAttribute("userTeam", userTeam);
        req.setAttribute("isCaptain", isCaptain);
        req.setAttribute("currentUserId", currentUserId);

        req.getRequestDispatcher("/profile.jsp").forward(req, resp);
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
        String action = req.getParameter("action");

        if ("update".equals(action)) {
            String bio = req.getParameter("bio");
            String city = req.getParameter("city");
            String ageParam = req.getParameter("age");
            int age = ageParam != null && !ageParam.isEmpty() ? Integer.parseInt(ageParam) : 0;
            userDAO.updateProfile(userId, bio, city, age);

        } else if ("updateName".equals(action)) {
            String newUsername = req.getParameter("username");
            if (newUsername != null && newUsername.length() >= 3) {
                userDAO.updateUsername(userId, newUsername);
                session.setAttribute("username", newUsername);
            }

        } else if ("updateAvatar".equals(action)) {
            try {
                String avatarPath = uploadAvatar(req, userId);
                if (avatarPath != null) {
                    userDAO.updateAvatar(userId, avatarPath);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if ("deleteAvatar".equals(action)) {
            userDAO.updateAvatar(userId, null);
        }

        resp.sendRedirect(req.getContextPath() + "/profile?id=" + userId);
    }

    private String uploadAvatar(HttpServletRequest req, int userId) throws Exception {
        if (!ServletFileUpload.isMultipartContent(req)) {
            return null;
        }

        DiskFileItemFactory factory = new DiskFileItemFactory();
        ServletFileUpload upload = new ServletFileUpload(factory);

        String uploadPath = getServletContext().getRealPath("") + File.separator + AVATAR_DIR;
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        List<FileItem> items = upload.parseRequest(req);
        for (FileItem item : items) {
            if (!item.isFormField() && item.getSize() > 0) {
                String fileName = System.currentTimeMillis() + "_" + userId + ".jpg";
                String filePath = uploadPath + File.separator + fileName;
                item.write(new File(filePath));
                return AVATAR_DIR + fileName;
            }
        }
        return null;
    }
}