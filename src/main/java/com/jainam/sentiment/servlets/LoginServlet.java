/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.jainam.sentiment.servlets;

/**
 *
 * @author Jainam Mehta
 */

import com.jainam.sentiment.dao.UserDAO;
import com.jainam.sentiment.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet 
{
    
    private UserDAO userDAO = new UserDAO();
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("userId") != null) 
        {
            response.sendRedirect("dashboard");
            return;
        }
        
        request.getRequestDispatcher("/login.html").forward(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String rememberMe = request.getParameter("rememberMe");
        
        if (username == null || password == null) 
        {
            response.sendRedirect("login.html?error=missing_credentials");
            return;
        }
        User user = userDAO.authenticate(username, password);
        if (user != null) 
        {
            userDAO.updateLastLogin(user.getId());
            
            HttpSession session = request.getSession();
            session.setAttribute("userId", user.getId());
            session.setAttribute("username", user.getUsername());
            session.setAttribute("userRole", user.getRole());
            session.setMaxInactiveInterval(30 * 60);
            
            if ("on".equals(rememberMe)) 
            {
                String ipAddress = request.getRemoteAddr();
                String userAgent = request.getHeader("User-Agent");
                String sessionToken = userDAO.createSession(user.getId(), ipAddress, userAgent);
                
                Cookie sessionCookie = new Cookie("sessionToken", sessionToken);
                sessionCookie.setMaxAge(7 * 24 * 60 * 60); 
                sessionCookie.setHttpOnly(true);
                sessionCookie.setPath("/");
                response.addCookie(sessionCookie);
            }
            response.sendRedirect("dashboard");
        } 
        else 
        {
            response.sendRedirect("login.html?error=invalid_credentials");
        }
    }
}