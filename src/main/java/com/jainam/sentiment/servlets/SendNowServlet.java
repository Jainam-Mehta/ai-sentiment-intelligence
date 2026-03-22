/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.jainam.sentiment.servlets;

/**
 *
 * @author Jainam Mehta
 */

import com.jainam.sentiment.dao.PostDAO;
import com.jainam.sentiment.dao.UserDAO;
import com.jainam.sentiment.model.User;
import com.jainam.sentiment.utils.EmailSender;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet("/sendNow")
public class SendNowServlet extends HttpServlet 
{
    
    private UserDAO userDAO = new UserDAO();
    private PostDAO postDAO = new PostDAO();
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        
        out.println("<html><body style='background:#0a0f1e;color:white;font-family:Arial;padding:20px;'>");
        
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) 
        {
            out.println("<h2>Please login first</h2>");
            out.println("</body></html>");
            return;
        }
        
        int userId = (int) session.getAttribute("userId");
        out.println("<h2>Sending Report Now</h2>");
        
        try 
        {
            User user = userDAO.getUserById(userId);
            out.println("<p>User: " + user.getUsername() + "</p>");
            out.println("<p>Email: " + user.getEmail() + "</p>");
            
            List<String[]> posts = postDAO.getUserPostsInRange(userId, 24);
            out.println("<p>Posts found: " + posts.size() + "</p>");
            
            if (posts.isEmpty()) 
            {
                out.println("<p style='color:orange'> No posts in last 24 hours!</p>");
            } 
            else 
            {
                int positive = 0, neutral = 0, negative = 0;
                for (String[] p : posts) 
                {
                    switch (p[1]) 
                    {
                        case "positive": positive++; break;
                        case "neutral": neutral++; break;
                        case "negative": negative++; break;
                    }
                }
                
                StringBuilder keywords = new StringBuilder();
                String lastKeyword = "";
                for (String[] p : posts) 
                {
                    if (!p[3].equals(lastKeyword)) 
                    {
                        if (keywords.length() > 0) keywords.append(", ");
                        keywords.append(p[3]);
                        lastKeyword = p[3];
                    }
                }
                
                String html = generateReportHtml(user, posts.size(), positive, neutral, negative, keywords.toString());
                
                EmailSender.sendEmail(user.getEmail(), "MANUAL TEST : Your Sentiment Report", html);
                out.println("<p style='color:#86efac;'>Email sent successfully :)</p>");
            }
            
        } catch (Exception e) {
            out.println("<p style='color:#fca5a5;'>Error: " + e.getMessage() + "</p>");
            e.printStackTrace(out);
        }
        
        out.println("<p><a href='index.html' style='color:#3b82f6;'>Back</a></p>");
        out.println("</body></html>");
    }
    
    private String generateReportHtml(User user, int totalPosts, int positive, int neutral, int negative, String keywords) 
    {
        double posPercent = totalPosts > 0 ? (positive * 100.0 / totalPosts) : 0;
        double neuPercent = totalPosts > 0 ? (neutral * 100.0 / totalPosts) : 0;
        double negPercent = totalPosts > 0 ? (negative * 100.0 / totalPosts) : 0;
        
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial; background: #f5f5f5; padding: 20px; }
                    .container { max-width: 600px; margin: 0 auto; background: white; border-radius: 10px; padding: 30px; }
                    h1 { color: #333; }
                    .stats { display: flex; justify-content: space-between; margin: 30px 0; }
                    .stat-box { flex: 1; text-align: center; padding: 20px; border-radius: 8px; color: white; margin: 0 5px; }
                    .positive { background: linear-gradient(135deg, #22c55e, #16a34a); }
                    .neutral { background: linear-gradient(135deg, #f59e0b, #d97706); }
                    .negative { background: linear-gradient(135deg, #ef4444, #dc2626); }
                    .stat-number { font-size: 32px; font-weight: bold; }
                </style>
            </head>
            <body>
                <div class='container'>
                    <h1>MANUAL TEST : Your Sentiment Report</h1>
                    <p>Hello <strong>%s</strong>,</p>
                    <p>Total Posts: %d</p>
                    <p>Keywords: %s</p>
                    <div class='stats'>
                        <div class='stat-box positive'>
                            <div class='stat-number'>%d</div><div>Positive (%.1f%%)</div>
                        </div>
                        <div class='stat-box neutral'>
                            <div class='stat-number'>%d</div><div>Neutral (%.1f%%)</div>
                        </div>
                        <div class='stat-box negative'>
                            <div class='stat-number'>%d</div><div>Negative (%.1f%%)</div>
                        </div>
                    </div>
                </div>
            </body>
            </html>
            """, user.getUsername(), totalPosts, keywords, positive, posPercent, neutral, neuPercent, negative, negPercent);
    }
}
