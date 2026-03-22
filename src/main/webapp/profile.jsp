<%-- 
    Document   : profile
    Created on : Mar 16, 2026, 9:17:23 PM
    Author     : Jainam Mehta
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>

<%@page import="com.jainam.sentiment.dao.UserDAO" %>
<%@page import="com.jainam.sentiment.model.User" %>
<%@page import="java.util.List" %>
<%
    if (session == null || session.getAttribute("userId") == null) {
        response.sendRedirect("login.html");
        return;
    }
    
    int userId = (int) session.getAttribute("userId");
    UserDAO userDAO = new UserDAO();
    User user = userDAO.getUserById(userId);
    List<String> keywords = userDAO.getUserKeywords(userId);
%>
<!DOCTYPE html>
<html>
<head>
    <title>Profile - AI Sentiment</title>
    <style>
        body { font-family: Arial; background: #0a0f1e; color: white; padding: 20px; }
        .container { max-width: 800px; margin: 0 auto; background: #1e293b; padding: 30px; border-radius: 10px; }
        h2 { color: white; margin-bottom: 20px; }
        .profile-section { background: #334155; padding: 20px; border-radius: 8px; margin-bottom: 20px; }
        .info-row { display: flex; margin-bottom: 10px; padding: 10px 0; border-bottom: 1px solid #475569; }
        .info-label { width: 150px; color: #94a3b8; }
        .info-value { color: white; }
        .keywords { display: flex; flex-wrap: wrap; gap: 10px; margin-top: 10px; }
        .keyword-tag { background: #3b82f6; padding: 5px 15px; border-radius: 20px; color: white; font-size: 14px; }
        .nav-links { margin-top: 20px; padding-top: 20px; border-top: 1px solid #334155; }
        .nav-links a { color: #94a3b8; text-decoration: none; margin-right: 15px; }
        .nav-links a:hover { color: white; }
        .btn { background: #3b82f6; color: white; padding: 10px 20px; border: none; border-radius: 5px; cursor: pointer; text-decoration: none; display: inline-block; }
        .btn:hover { background: #2563eb; }
    </style>
</head>
<body>
    <div class="container">
        <h2>User Profile</h2>
        
        <div class="profile-section">
            <h3>Account Information</h3>
            <div class="info-row">
                <span class="info-label">Username:</span>
                <span class="info-value"><%= user.getUsername() %></span>
            </div>
            <div class="info-row">
                <span class="info-label">Full Name:</span>
                <span class="info-value"><%= user.getFullName() != null ? user.getFullName() : "Not set" %></span>
            </div>
            <div class="info-row">
                <span class="info-label">Email:</span>
                <span class="info-value"><%= user.getEmail() %></span>
            </div>
            <div class="info-row">
                <span class="info-label">Role:</span>
                <span class="info-value"><%= user.getRole() %></span>
            </div>
            <div class="info-row">
                <span class="info-label">Member Since:</span>
                <span class="info-value"><%= user.getCreatedAt() %></span>
            </div>
            <div class="info-row">
                <span class="info-label">Last Login:</span>
                <span class="info-value"><%= user.getLastLogin() != null ? user.getLastLogin() : "First login" %></span>
            </div>
        </div>
        
        <div class="profile-section">
            <h3>Tracked Keywords</h3>
            <% if (keywords != null && !keywords.isEmpty()) { %>
                <div class="keywords">
                    <% for (String keyword : keywords) { %>
                        <span class="keyword-tag"><%= keyword %></span>
                    <% } %>
                </div>
            <% } else { %>
                <p style="color: #94a3b8;">No keywords tracked yet, Start monitoring to add keywords :)</p>
            <% } %>
        </div>
        
        <div style="display: flex; gap: 10px; margin-top: 20px;">
            <a href="preferences.jsp" class="btn">Email Preferences</a>
            <a href="index.html" class="btn">Dashboard</a>
            <a href="upload.html" class="btn">Upload CSV</a>
        </div>
        
        <div class="nav-links">
            <a href="index.html">Back to Dashboard</a>
            <a href="logout">Logout</a>
        </div>
    </div>
</body>
</html>
