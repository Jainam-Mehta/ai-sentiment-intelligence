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
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet("/getUserInfo")
public class GetUserInfoServlet extends HttpServlet 
{
    
    private UserDAO userDAO = new UserDAO();
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        HttpSession session = request.getSession(false);
        Integer userId = null;
        
        try 
        {
            if (session != null && session.getAttribute("userId") != null) 
            {
                userId = (Integer) session.getAttribute("userId");
                System.out.println("GetUserInfo: Found user in session: " + userId);
            }
            if (userId == null) 
            {
                Cookie[] cookies = request.getCookies();
                if (cookies != null) 
                {
                    for (Cookie cookie : cookies) 
                    {
                        if ("sessionToken".equals(cookie.getName())) {
                            System.out.println("GetUserInfo: Found sessionToken cookie: " + cookie.getValue());
                            userId = userDAO.validateSession(cookie.getValue());
                            
                            if (userId != null) 
                            {
                                session = request.getSession(true);
                                session.setAttribute("userId", userId);
                                session.setMaxInactiveInterval(30 * 60); 
                                System.out.println("GetUserInfo: Session restored for user ID: " + userId);
                                
                                cookie.setMaxAge(7 * 24 * 60 * 60); 
                                cookie.setPath("/");
                                response.addCookie(cookie);
                            }
                            break;
                        }
                    }
                }
            }
            if (userId != null) 
            {
                User user = userDAO.getUserById(userId);
                if (user != null) 
                {
                    String json = String.format(
                        "{\"loggedIn\":true,\"username\":\"%s\",\"userId\":%d}",
                        user.getUsername(), userId );
                    response.getWriter().write(json);
                    System.out.println("GetUserInfo: User " + user.getUsername() + " is logged in :)");
                } 
                else 
                {
                    System.out.println("GetUserInfo: User ID " + userId + " not found in database");
                    response.getWriter().write("{\"loggedIn\":false}");
                }
            } 
            else 
            {
                System.out.println("GetUserInfo: No user logged in");
                response.getWriter().write("{\"loggedIn\":false}");
            }
            
        } catch (Exception e) {
            System.err.println("GetUserInfo: Error processing request: " + e.getMessage());
            e.printStackTrace();
            response.getWriter().write("{\"loggedIn\":false,\"error\":\"" + e.getMessage() + "\"}");
        }
    }
}