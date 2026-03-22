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
import java.io.IOException;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet 
{
    
    private UserDAO userDAO = new UserDAO();
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException 
    {
        request.getRequestDispatcher("/register.html").forward(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");
        String email = request.getParameter("email");
        String fullName = request.getParameter("fullName");
        if (username == null || username.trim().isEmpty() || password == null || password.isEmpty() || email == null || email.trim().isEmpty()) 
        {
            response.sendRedirect("register.html?error=missing_fields");
            return;
        }
        if (!password.equals(confirmPassword)) 
        {
            response.sendRedirect("register.html?error=password_mismatch");
            return;
        }
        User existingUser = userDAO.getUserByUsername(username);
        if (existingUser != null) 
        {
            response.sendRedirect("register.html?error=username_taken");
            return;
        }
        User newUser = new User(username, password, email, fullName);
        
        if (userDAO.register(newUser, password)) 
        {
            response.sendRedirect("login.html?registered=true");
        } 
        else 
        {
            response.sendRedirect("register.html?error=registration_failed");
        }
    }
}