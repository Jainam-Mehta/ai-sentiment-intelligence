/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.jainam.sentiment.service;

/**
 *
 * @author Jainam Mehta
 */

import com.jainam.sentiment.dao.PostDAO;
import com.jainam.sentiment.dao.UserDAO;
import com.jainam.sentiment.model.User;
import com.jainam.sentiment.utils.EmailSender;
import java.util.List;

public class ReportGenerator 
{
    
    private UserDAO userDAO = new UserDAO();
    private PostDAO postDAO = new PostDAO();
    
    public void generateAndSendDailyReports() 
    {
        generateReports("daily", 24);
    }
    
    public void generateAndSendWeeklyReports() 
    {
        generateReports("weekly", 168);
    }
    
    public void generateAndSendMonthlyReports() 
    {
        generateReports("monthly", 720);
    }
    
    public void generateAndSendDailyReportForUser(int userId) 
    {
        generateReportForUser(userId, "daily", 24);
    }
    
    public void generateAndSendWeeklyReportForUser(int userId) 
    {
        generateReportForUser(userId, "weekly", 168);
    }
    
    public void generateAndSendMonthlyReportForUser(int userId) 
    {
        generateReportForUser(userId, "monthly", 720);
    }
    
    private void generateReportForUser(int userId, String frequency, int hours) 
    {
        User user = userDAO.getUserById(userId);
        if (user == null || user.getEmail() == null || !user.isReceiveReports()) 
        {
            return;
        }
        
        try 
        {
            List<String[]> posts = postDAO.getUserPostsInRange(userId, hours);
            if (posts.isEmpty()) 
            {
                System.out.println("No posts for user " + user.getUsername() + " in last " + hours + " hours");
                return;
            }
            
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
            String html = generateHtmlReport(user, posts.size(), positive, neutral, negative, keywords.toString(), frequency);
            EmailSender.sendEmail(user.getEmail(), "Your " + frequency + " Sentiment Report", html);
            System.out.println(frequency + " report sent to " + user.getEmail());
            
        } catch (Exception e) {
            System.err.println("Failed to send report to " + user.getEmail() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void generateReports(String frequency, int hours) 
    {
        System.out.println("Starting " + frequency + " report generation");  
        List<User> users = userDAO.getAllUsers();
        System.out.println("Found " + users.size() + " users with emails");
        int sentCount = 0;
        for (User user : users) 
        {
            if (user.getEmail() == null || user.getEmail().trim().isEmpty() || !user.isReceiveReports()) 
            {
                continue;
            }
            
            if (!user.getReportFrequency().equals(frequency)) 
            {
                continue;
            }
            try 
            {
                List<String[]> posts = postDAO.getUserPostsInRange(user.getId(), hours);
                if (posts.isEmpty()) 
                {
                    System.out.println("No posts for user " + user.getUsername() + " in last " + hours + " hours");
                    continue;
                }
                
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
                
                String html;
                if ("text".equals(user.getReportFormat())) 
                {
                    html = generateTextReport(user, posts.size(), positive, neutral, negative, keywords.toString(), frequency);
                } 
                else 
                {
                    html = generateHtmlReport(user, posts.size(), positive, neutral, negative, keywords.toString(), frequency);
                }
                String subject = "Your " + frequency.substring(0,1).toUpperCase() + frequency.substring(1) + " Sentiment Report";
                EmailSender.sendEmail(user.getEmail(), subject, html);
                System.out.println(frequency + " report sent to " + user.getEmail());
                sentCount++;
                Thread.sleep(1000);
            } catch (Exception e) {
                System.err.println("Failed to send report to " + user.getEmail() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
        System.out.println(frequency + " report generation complete. Sent " + sentCount + " reports");
    }
    
    private String generateHtmlReport(User user, int totalPosts, int positive, int neutral, int negative, String keywords, String frequency) 
    {
        double posPercent = totalPosts > 0 ? (positive * 100.0 / totalPosts) : 0;
        double neuPercent = totalPosts > 0 ? (neutral * 100.0 / totalPosts) : 0;
        double negPercent = totalPosts > 0 ? (negative * 100.0 / totalPosts) : 0;
        String period = frequency.equals("daily") ? "last 24 hours" : frequency.equals("weekly") ? "last 7 days" : "last 30 days";
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; background: #f5f5f5; padding: 20px; }
                    .container { max-width: 600px; margin: 0 auto; background: white; border-radius: 10px; padding: 30px; box-shadow: 0 5px 15px rgba(0,0,0,0.1); }
                    h1 { color: #333; margin-bottom: 20px; }
                    .stats { display: flex; justify-content: space-between; margin: 30px 0; }
                    .stat-box { flex: 1; text-align: center; padding: 20px; border-radius: 8px; color: white; margin: 0 5px; }
                    .positive { background: linear-gradient(135deg, #22c55e, #16a34a); }
                    .neutral { background: linear-gradient(135deg, #f59e0b, #d97706); }
                    .negative { background: linear-gradient(135deg, #ef4444, #dc2626); }
                    .stat-number { font-size: 32px; font-weight: bold; }
                    .stat-label { font-size: 14px; opacity: 0.9; }
                    .summary { background: #f8fafc; padding: 20px; border-radius: 8px; margin: 20px 0; }
                    .button { display: inline-block; background: #3b82f6; color: white; padding: 12px 30px; text-decoration: none; border-radius: 6px; margin-top: 20px; }
                    .footer { margin-top: 30px; color: #64748b; font-size: 12px; text-align: center; }
                </style>
            </head>
            <body>
                <div class='container'>
                    <h1>Your %s Sentiment Report</h1>
                    <p>Hello <strong>%s</strong>, here's your activity summary for the %s:</p>
                    
                    <div class='summary'>
                        <p><strong>Total Posts Analyzed:</strong> %d</p>
                        <p><strong>Keywords Tracked:</strong> %s</p>
                    </div>
                    
                    <div class='stats'>
                        <div class='stat-box positive'>
                            <div class='stat-number'>%d</div>
                            <div class='stat-label'>Positive</div>
                            <div>(%.1f%%)</div>
                        </div>
                        <div class='stat-box neutral'>
                            <div class='stat-number'>%d</div>
                            <div class='stat-label'>Neutral</div>
                            <div>(%.1f%%)</div>
                        </div>
                        <div class='stat-box negative'>
                            <div class='stat-number'>%d</div>
                            <div class='stat-label'>Negative</div>
                            <div>(%.1f%%)</div>
                        </div>
                    </div>
                    
                    <div style='text-align: center;'>
                        <a href='http://localhost:8080/Social_Sentiment_Java_Project' class='button'>View Full Dashboard</a>
                    </div>
                    
                    <div class='footer'>
                        <p>You're receiving this because you have an account at AI Sentiment Intelligence</p>
                        <p>To change your email preferences, visit your profile settings</p>
                    </div>
                </div>
            </body>
            </html>
            """, 
            frequency.substring(0,1).toUpperCase() + frequency.substring(1),
            user.getUsername(), 
            period,
            totalPosts, 
            keywords,
            positive, posPercent, 
            neutral, neuPercent, 
            negative, negPercent);
    }
    
    private String generateTextReport(User user, int totalPosts, int positive, int neutral, int negative, String keywords, String frequency) 
    {
        double posPercent = totalPosts > 0 ? (positive * 100.0 / totalPosts) : 0;
        double neuPercent = totalPosts > 0 ? (neutral * 100.0 / totalPosts) : 0;
        double negPercent = totalPosts > 0 ? (negative * 100.0 / totalPosts) : 0;
        
        String period = frequency.equals("daily") ? "last 24 hours" : frequency.equals("weekly") ? "last 7 days" : "last 30 days";
        
        return String.format("""
            Your %s Sentiment Report
                             
            
            Hello %s,
            
            Here's your activity summary for the %s:
            
            Total Posts Analyzed: %d
            Keywords Tracked: %s
            
            Sentiment Breakdown:
            Positive: %d (%.1f%%)
            Neutral:  %d (%.1f%%)
            Negative: %d (%.1f%%)
            
            Visit the dashboard to see detailed trends:
            http://localhost:8080/Social_Sentiment_Java_Project
            
                             
                             
            You're receiving this because you have an account at AI Sentiment Intelligence
            To change your email preferences, visit your profile settings
            """,
            frequency.substring(0,1).toUpperCase() + frequency.substring(1),
            user.getUsername(),
            period,
            totalPosts,
            keywords,
            positive, posPercent,
            neutral, neuPercent,
            negative, negPercent);
    }
}