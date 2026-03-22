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
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

@WebServlet("/profile")
public class ProfileServlet extends HttpServlet 
{
    
    private UserDAO userDAO = new UserDAO();
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException 
    {
        
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) 
        {
            response.sendRedirect("login.html");
            return;
        }
        
        int userId = (int) session.getAttribute("userId");
        User user = userDAO.getUserById(userId);
        List<String> keywords = userDAO.getUserKeywords(userId);
        
        request.setAttribute("user", user);
        request.setAttribute("keywords", keywords);
        request.getRequestDispatcher("/profile.jsp").forward(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException 
    {
        
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null)
        {
            response.sendRedirect("login.html");
            return;
        }
        
        int userId = (int) session.getAttribute("userId");
        String action = request.getParameter("action");
        
        if ("addKeyword".equals(action)) 
        {
            String keyword = request.getParameter("keyword");
            if (keyword != null && !keyword.trim().isEmpty()) 
            {
                userDAO.addUserKeyword(userId, keyword.trim());
            }
        } 
        else if ("removeKeyword".equals(action)) 
        {
            String keyword = request.getParameter("keyword");
            if (keyword != null) 
            {
                userDAO.removeUserKeyword(userId, keyword);
            }
        }
        
        response.sendRedirect("profile");
    }
}