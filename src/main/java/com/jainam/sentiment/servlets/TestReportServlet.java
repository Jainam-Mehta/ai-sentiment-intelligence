/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.jainam.sentiment.servlets;

/**
 *
 * @author Jainam Mehta
 */

import com.jainam.sentiment.service.ReportGenerator;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/testReport")
public class TestReportServlet extends HttpServlet 
{
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        
        String type = request.getParameter("type");
        ReportGenerator generator = new ReportGenerator();
        
        out.println("<!DOCTYPE html>");
        out.println("<html><head><title>Test Reports</title>");
        out.println("<style>");
        out.println("body { font-family: Arial; background: #0a0f1e; color: white; padding: 20px; }");
        out.println(".container { max-width: 600px; margin: 0 auto; background: #1e293b; padding: 30px; border-radius: 10px; }");
        out.println("h1 { color: white; }");
        out.println(".success { background: #14532d; color: #86efac; padding: 15px; border-radius: 5px; }");
        out.println(".error { background: #7f1d1d; color: #fecaca; padding: 15px; border-radius: 5px; }");
        out.println("a { display: inline-block; background: #3b82f6; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px; margin: 10px 5px; }");
        out.println("a:hover { background: #2563eb; }");
        out.println("</style>");
        out.println("</head><body>");
        out.println("<div class='container'>");
        out.println("<h1>Test Email Reports</h1>");
        
        try 
        {
            if ("daily".equals(type)) 
            {
                out.println("<div class='success'>Triggering daily reports</div>");
                generator.generateAndSendDailyReports();
                out.println("<div class='success'>Daily reports triggered successfully :)</div>");
            } 
            else if ("weekly".equals(type)) 
            {
                out.println("<div class='success'>Triggering weekly reports</div>");
                generator.generateAndSendWeeklyReports();
                out.println("<div class='success'>Weekly reports triggered successfully :)</div>");
            } 
            else if ("monthly".equals(type)) 
            {
                out.println("<div class='success'>Triggering monthly reports</div>");
                generator.generateAndSendMonthlyReports();
                out.println("<div class='success'>Monthly reports triggered successfully :)</div>");
            } 
            else 
            {
                out.println("<h2>Choose a report type to test:</h2>");
                out.println("<a href='?type=daily'>Test Daily Reports</a>");
                out.println("<a href='?type=weekly'>Test Weekly Reports</a>");
                out.println("<a href='?type=monthly'>Test Monthly Reports</a>");
            }
        } catch (Exception e) {
            out.println("<div class='error'>Error: " + e.getMessage() + "</div>");
            e.printStackTrace();
        }
        
        out.println("<p><a href='index.html' style='background: #4b5563;'>Back to Dashboard</a></p>");
        out.println("</div>");
        out.println("</body></html>");
    }
}