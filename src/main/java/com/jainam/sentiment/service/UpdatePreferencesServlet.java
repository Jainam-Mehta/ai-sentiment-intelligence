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
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;

@WebServlet("/updatePreferences")
public class UpdatePreferencesServlet extends HttpServlet 
{
    
    private UserDAO userDAO = new UserDAO();
    
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
        String email = request.getParameter("email");
        boolean receiveReports = "on".equals(request.getParameter("receiveReports"));
        String frequency = request.getParameter("reportFrequency");
        String format = request.getParameter("reportFormat");
        
        int reportHour = 8;
        int reportMinute = 0;
        String timezone = "Asia/Kolkata";
        try 
        {
            reportHour = Integer.parseInt(request.getParameter("reportHour"));
            reportMinute = Integer.parseInt(request.getParameter("reportMinute"));
            timezone = request.getParameter("reportTimezone");
        } catch (Exception e) {}
        
        boolean updated = userDAO.updateEmailPreferences(userId, email, receiveReports, frequency, format, reportHour, reportMinute, timezone);
        if (updated) 
        {
            response.sendRedirect("preferences.jsp?success=true");
        } 
        else 
        {
            response.sendRedirect("preferences.jsp?error=true");
        }
    }
}