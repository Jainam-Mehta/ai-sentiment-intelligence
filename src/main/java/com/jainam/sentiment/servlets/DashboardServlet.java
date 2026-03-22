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
import java.util.List;

@WebServlet("/dashboard")
public class DashboardServlet extends HttpServlet 
{
    
    private UserDAO userDAO = new UserDAO();
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        Integer userId = null;
        
        if (session != null && session.getAttribute("userId") != null) 
        {
            userId = (Integer) session.getAttribute("userId");
        } 
        else 
        {
            Cookie[] cookies = request.getCookies();
            if (cookies != null) 
            {
                for (Cookie cookie : cookies) 
                {
                    if ("sessionToken".equals(cookie.getName())) 
                    {
                        userId = userDAO.validateSession(cookie.getValue());
                        if (userId != null) 
                        {
                            session = request.getSession(true);
                            session.setAttribute("userId", userId);
                        }
                        break;
                    }
                }
            }
        }
        
        if (userId == null) 
        {
            response.sendRedirect("login.html");
            return;
        }
        User user = userDAO.getUserById(userId);
        List<String> keywords = userDAO.getUserKeywords(userId);
        
        response.sendRedirect("index.html");
    }
}