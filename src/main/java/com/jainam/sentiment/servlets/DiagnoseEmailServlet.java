/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.jainam.sentiment.servlets;

/**
 *
 * @author Jainam Mehta
 */

import jakarta.mail.*;
import jakarta.mail.internet.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;

@WebServlet("/diagnoseEmail")
public class DiagnoseEmailServlet extends HttpServlet 
{
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        
        out.println("<!DOCTYPE html>");
        out.println("<html><head><title>Email Diagnosis</title>");
        out.println("<style>");
        out.println("body { font-family: Arial; background: #0a0f1e; color: white; padding: 20px; }");
        out.println(".container { max-width: 800px; margin: 0 auto; background: #1e293b; padding: 30px; border-radius: 10px; }");
        out.println(".success { background: #14532d; color: #86efac; padding: 15px; border-radius: 5px; }");
        out.println(".error { background: #7f1d1d; color: #fecaca; padding: 15px; border-radius: 5px; }");
        out.println(".info { background: #1e3a8a; color: #93c5fd; padding: 10px; border-radius: 5px; }");
        out.println("pre { background: #0f172a; padding: 10px; border-radius: 5px; overflow-x: auto; }");
        out.println("</style>");
        out.println("</head><body>");
        out.println("<div class='container'>");
        out.println("<h1>Email System Diagnosis</h1>");
        
        out.println("<h2>Test 1: Authentication Check</h2>");
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) 
        {
            out.println("<div class='error'>Not logged in, Please login first :(</div>");
            out.println("</div></body></html>");
            return;
        }
        
        int userId = (int) session.getAttribute("userId");
        out.println("<div class='success'>Logged in as user ID: " + userId + " :)</div>");
        
        out.println("<h2>Test 2: Database Email Check</h2>");
        try 
        {
            com.jainam.sentiment.dao.UserDAO userDAO = new com.jainam.sentiment.dao.UserDAO();
            com.jainam.sentiment.model.User user = userDAO.getUserById(userId);
            
            if (user == null) 
            {
                out.println("<div class='error'>User not found in database :(</div>");
            } 
            else 
            {
                out.println("<div class='success'>User found: " + user.getUsername() + " :)</div>");
                out.println("<div>Email: " + user.getEmail() + "</div>");
                out.println("<div>Receive Reports: " + user.isReceiveReports() + "</div>");
                out.println("<div>Frequency: " + user.getReportFrequency() + "</div>");
                out.println("<div>Preferred Time: " + user.getReportHour() + ":" + String.format("%02d", user.getReportMinute()) + "</div>");
                
                if (user.getEmail() == null || user.getEmail().trim().isEmpty()) 
                {
                    out.println("<div class='error'>No email address set :(</div>");
                } 
                else 
                {
                    out.println("<div class='success'>Email configured: " + user.getEmail() + " :)</div>");
                }
            }
        } catch (Exception e) {
            out.println("<div class='error'>Database error: " + e.getMessage() + " :(</div>");
        }
        
        out.println("<h2>Test 3: Posts Check</h2>");
        try 
        {
            com.jainam.sentiment.dao.PostDAO postDAO = new com.jainam.sentiment.dao.PostDAO();
            java.util.List<String[]> posts = postDAO.getUserPostsInRange(userId, 24);
            
            if (posts == null || posts.isEmpty()) 
            {
                out.println("<div class='error'>No posts found in last 24 hours :(</div>");
                out.println("<div class='info'>Generate some posts in Private Mode first</div>");
            } 
            else 
            {
                out.println("<div class='success'>Found " + posts.size() + " posts in last 24 hours :)</div>");
            }
        } catch (Exception e) {
            out.println("<div class='error'>Error checking posts: " + e.getMessage() + " :(</div>");
        }
        
        out.println("<h2>Test 4: Direct SMTP Test</h2>");
        
        final String username = System.getenv("EMAIL_USERNAME");
        final String password = System.getenv("EMAIL_PASSWORD");
        
        out.println("<div>Testing with: " + username + "</div>");
        out.println("<div>Password length: " + password.length() + " characters</div>");
        
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");
        
        Session mailSession = Session.getInstance(props, new Authenticator() 
        {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() 
            {
                return new PasswordAuthentication(username, password);
            }
        });
        mailSession.setDebug(true);
        
        try 
        {
            Message message = new MimeMessage(mailSession);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(username));
            message.setSubject("Diagnostic Test Email : " + new java.util.Date());
            message.setText("If you receive this, email is working ;)\n\nSent from DiagnoseEmailServlet");
            
            Transport.send(message);
            out.println("<div class='success'>EMAIL SENT SUCCESSFULLY :)</div>");
            out.println("<div class='info'>Check your Gmail inbox and spam folder</div>");
            
        } catch (AuthenticationFailedException e) {
            out.println("<div class='error'>Authentication Failed: " + e.getMessage() + " :(</div>");
            out.println("<div class='info'>Possible causes:</div>");
            out.println("<ul>");
            out.println("<li>Wrong email or app password</li>");
            out.println("<li>2Factor Authentication not enabled</li>");
            out.println("<li>App password has spaces</li>");
            out.println("</ul>");
        } catch (SendFailedException e) {
            out.println("<div class='error'>Send Failed: " + e.getMessage() + " :(</div>");
        } catch (MessagingException e) {
            out.println("<div class='error'>Messaging Error: " + e.getMessage() + " :(</div>");
            out.println("<pre>");
            e.printStackTrace(out);
            out.println("</pre>");
        } catch (Exception e) {
            out.println("<div class='error'>Unexpected Error: " + e.getMessage() + " :(</div>");
            e.printStackTrace(out);
        }
        
        out.println("<p><a href='index.html' style='color:#3b82f6;'>Back to Dashboard</a></p>");
        out.println("</div>");
        out.println("</body></html>");
    }
}