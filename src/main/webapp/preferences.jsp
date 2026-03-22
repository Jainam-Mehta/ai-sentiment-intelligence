<%-- 
    Document   : preferences
    Created on : Mar 16, 2026, 5:41:12 PM
    Author     : Jainam Mehta
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="com.jainam.sentiment.model.User" %>
<%@page import="com.jainam.sentiment.dao.UserDAO" %>
<%
    if (session == null || session.getAttribute("userId") == null) {
        response.sendRedirect("login.html");
        return;
    }
    int userId = (int) session.getAttribute("userId");
    UserDAO userDAO = new UserDAO();
    User user = userDAO.getUserById(userId);
    
    if (user == null) {
        response.sendRedirect("login.html");
        return;
    }
%>
<!DOCTYPE html>
<html>
<head>
    <title>Email Preferences - AI Sentiment</title>
    <style>
        body { font-family: Arial; background: #0a0f1e; color: white; padding: 20px; }
        .container { max-width: 600px; margin: 0 auto; background: #1e293b; padding: 30px; border-radius: 10px; }
        .form-group { margin-bottom: 20px; }
        label { display: block; margin-bottom: 5px; color: #94a3b8; }
        input, select { width: 100%; padding: 10px; border-radius: 5px; border: none; }
        input[type="checkbox"] { width: auto; margin-right: 10px; }
        .checkbox-group { display: flex; align-items: center; }
        .time-picker { display: flex; gap: 10px; align-items: center; }
        .time-picker select { flex: 1; }
        button { background: #3b82f6; color: white; padding: 12px 30px; border: none; border-radius: 5px; cursor: pointer; }
        button:hover { background: #2563eb; }
        .nav-links { margin-top: 20px; padding-top: 20px; border-top: 1px solid #334155; }
        .nav-links a { color: #94a3b8; text-decoration: none; margin-right: 15px; }
        .nav-links a:hover { color: white; }
        .hint { color: #94a3b8; font-size: 12px; margin-top: 5px; }
    </style>
</head>
<body>
    <div class="container">
        <h2>Email Preferences</h2>
        <%
    String success = request.getParameter("success");
    String error = request.getParameter("error");
%>

<% if ("true".equals(success)) { %>
    <div style="background: #14532d; color: #86efac; padding: 12px; border-radius: 5px; margin-bottom: 20px; text-align: center;">
        Preferences saved successfully :)
    </div>
<% } %>

<% if ("true".equals(error)) { %>
    <div style="background: #7f1d1d; color: #fecaca; padding: 12px; border-radius: 5px; margin-bottom: 20px; text-align: center;">
        Failed to save preferences. Please try again :(
    </div>
<% } %>
        <form action="updatePreferences" method="post">
            <div class="form-group">
                <label>Email Address:</label>
                <input type="email" name="email" value="<%= user.getEmail() != null ? user.getEmail() : "" %>" required>
            </div>
            
            <div class="form-group checkbox-group">
                <input type="checkbox" name="receiveReports" id="receiveReports" <%= user.isReceiveReports() ? "checked" : "" %>>
                <label for="receiveReports" style="display: inline;">Receive email reports</label>
            </div>
            
            <div class="form-group">
                <label>Report Frequency:</label>
                <select name="reportFrequency">  
                    <option value="daily" <%= "daily".equals(user.getReportFrequency()) ? "selected" : "" %>>Daily</option>
                    <option value="weekly" <%= "weekly".equals(user.getReportFrequency()) ? "selected" : "" %>>Weekly</option>
                    <option value="monthly" <%= "monthly".equals(user.getReportFrequency()) ? "selected" : "" %>>Monthly</option>
                </select>
            </div>
            
            <div class="form-group">
                <label>Report Format:</label>
                <select name="reportFormat">
                    <option value="html" <%= "html".equals(user.getReportFormat()) ? "selected" : "" %>>HTML</option>
                    <option value="text" <%= "text".equals(user.getReportFormat()) ? "selected" : "" %>>Plain Text</option>
                </select>
            </div>
            
<div class="form-group">
    <label>Preferred Report Time:</label>
    <div class="time-picker">
        <select name="reportHour">
            <% for(int h = 0; h < 24; h++) { 
                String hourStr = String.format("%02d", h);
                String selected = (user.getReportHour() == h) ? "selected" : ""; 
            %>
            <option value="<%= h %>" <%= selected %>><%= hourStr %></option>
            <% } %>
        </select>
        <span style="color: white;">:</span>
        <select name="reportMinute">
            <% for(int m = 0; m < 60; m+=15) { 
                String minStr = String.format("%02d", m);
                String selected = (user.getReportMinute() == m) ? "selected" : "";  
            %>
            <option value="<%= m %>" <%= selected %>><%= minStr %></option>
            <% } %>
        </select>
    </div>
    <div class="hint">Reports will be sent at your selected time</div>
</div>
            
            <div class="form-group">
                <label>Your Timezone:</label>
                <select name="reportTimezone">
                    <option value="Asia/Kolkata" <%= "Asia/Kolkata".equals(user.getReportTimezone()) ? "selected" : "" %>>IST (India)</option>
                    <option value="Asia/Dubai" <%= "Asia/Dubai".equals(user.getReportTimezone()) ? "selected" : "" %>>GST (Dubai)</option>
                    <option value="Asia/Singapore" <%= "Asia/Singapore".equals(user.getReportTimezone()) ? "selected" : "" %>>SGT (Singapore)</option>
                    <option value="Australia/Sydney" <%= "Australia/Sydney".equals(user.getReportTimezone()) ? "selected" : "" %>>AEDT (Sydney)</option>
                    <option value="America/New_York" <%= "America/New_York".equals(user.getReportTimezone()) ? "selected" : "" %>>EST (New York)</option>
                    <option value="America/Los_Angeles" <%= "America/Los_Angeles".equals(user.getReportTimezone()) ? "selected" : "" %>>PST (Los Angeles)</option>
                    <option value="Europe/London" <%= "Europe/London".equals(user.getReportTimezone()) ? "selected" : "" %>>GMT (London)</option>
                </select>
                <div class="hint">Reports will be sent according to your timezone</div>
            </div>
            
            <button type="submit">Save Preferences</button>
        </form>
        
        <div class="nav-links">
            <a href="profile.jsp">👤 Profile</a>
            <a href="index.html">🏠 Dashboard</a>
            <a href="upload.html">📤 Upload CSV</a>
            <a href="logout">🚪 Logout</a>
        </div>
    </div>
                
    <script>
    document.querySelector('form').addEventListener('submit', function(e) {
        console.log('Form submitted!');
        console.log('Email:', document.querySelector('input[name="email"]').value);
        console.log('Receive Reports:', document.querySelector('input[name="receiveReports"]').checked);
        console.log('Frequency:', document.querySelector('select[name="reportFrequency"]').value);
        console.log('Format:', document.querySelector('select[name="reportFormat"]').value);
        console.log('Hour:', document.querySelector('select[name="reportHour"]').value);
        console.log('Minute:', document.querySelector('select[name="reportMinute"]').value);
        console.log('Timezone:', document.querySelector('select[name="reportTimezone"]').value);
    });
</script>
</body>
</html>