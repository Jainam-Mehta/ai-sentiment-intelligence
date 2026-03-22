/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.jainam.sentiment.model;

import java.sql.Timestamp;

public class User 
{
    private int id;
    private String username;
    private String password;
    private String email;
    private String fullName;
    private String role;
    private Timestamp createdAt;
    private Timestamp lastLogin;
    private boolean isActive;
    
    private boolean receiveReports;
    private String reportFrequency;
    private String reportFormat;
    private Timestamp lastReportSent;
    
    private int reportHour;
    private int reportMinute;
    private String reportTimezone;
    public int getReportHour() 
    { 
        return reportHour; 
    }
    public void setReportHour(int reportHour) 
    { 
        this.reportHour = reportHour; 
    }

    public int getReportMinute() 
    { 
        return reportMinute; 
    }
    public void setReportMinute(int reportMinute) 
    { 
        this.reportMinute = reportMinute; 
    }

    public String getReportTimezone() 
    { 
        return reportTimezone; 
    }
    public void setReportTimezone(String reportTimezone) 
    { 
        this.reportTimezone = reportTimezone; 
    }

    public User() {}
    
    public User(String username, String password, String email, String fullName) 
    {
        this.username = username;
        this.password = password;
        this.email = email;
        this.fullName = fullName;
        this.role = "user";
        this.isActive = true;
        this.receiveReports = true;
        this.reportFrequency = "daily";
        this.reportFormat = "html";
        this.reportHour = 8;
        this.reportMinute = 0;
        this.reportTimezone = "Asia/Kolkata";
    }
    
    public int getId() 
    { 
        return id; 
    }
    public void setId(int id) 
    { 
        this.id = id; 
    }
    
    public String getUsername() 
    { 
        return username; 
    }
    public void setUsername(String username) 
    { 
        this.username = username; 
    }
    
    public String getPassword() 
    { 
        return password; 
    }
    public void setPassword(String password) 
    { 
        this.password = password; 
    }
    
    public String getEmail() 
    { 
        return email; 
    }
    public void setEmail(String email) 
    { 
        this.email = email; 
    }
    
    public String getFullName() 
    { 
        return fullName; 
    }
    public void setFullName(String fullName) 
    { 
        this.fullName = fullName; 
    }
    
    public String getRole() 
    { 
        return role; 
    }
    public void setRole(String role) 
    { 
        this.role = role; 
    }
    
    public Timestamp getCreatedAt() 
    { 
        return createdAt; 
    }
    public void setCreatedAt(Timestamp createdAt) 
    { 
        this.createdAt = createdAt; 
    }
    
    public Timestamp getLastLogin() 
    { 
        return lastLogin; 
    }
    public void setLastLogin(Timestamp lastLogin) 
    { 
        this.lastLogin = lastLogin; 
    }
    
    public boolean isActive() 
    { 
        return isActive; 
    }
    public void setActive(boolean active) 
    { 
        isActive = active; 
    }
    
    public boolean isReceiveReports() 
    { 
        return receiveReports; 
    }
    public void setReceiveReports(boolean receiveReports) 
    { 
        this.receiveReports = receiveReports; 
    }

    public String getReportFrequency() 
    { 
        return reportFrequency; 
    }
    public void setReportFrequency(String reportFrequency) 
    { 
        this.reportFrequency = reportFrequency; 
    }

    public String getReportFormat() 
    { 
        return reportFormat; 
    }
    public void setReportFormat(String reportFormat) 
    { 
        this.reportFormat = reportFormat; 
    }

    public Timestamp getLastReportSent() 
    { 
        return lastReportSent; 
    }
    public void setLastReportSent(Timestamp lastReportSent) 
    { 
        this.lastReportSent = lastReportSent; 
    }
}