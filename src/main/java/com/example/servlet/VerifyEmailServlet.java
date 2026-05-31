package com.example.servlet;

import com.example.dao.UserDAO;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/verify")
public class VerifyEmailServlet extends HttpServlet {

    private UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String email = req.getParameter("email");
        String code = req.getParameter("code");

        if (email == null || code == null) {
            req.setAttribute("error", "Invalid verification link");
            req.getRequestDispatcher("/verify.jsp").forward(req, resp);
            return;
        }

        // verifyEmail теперь принимает 2 аргумента (email, code)
        if (userDAO.verifyEmail(email, code)) {
            req.setAttribute("message", "Email verified successfully! You can now login.");
            req.getRequestDispatcher("/login.jsp").forward(req, resp);
        } else {
            req.setAttribute("error", "Verification failed. Code may be expired or invalid.");
            req.getRequestDispatcher("/verify.jsp").forward(req, resp);
        }
    }
}