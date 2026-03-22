/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.jainam.sentiment.listener;

/**
 *
 * @author Jainam Mehta
 */

import com.jainam.sentiment.service.ReportGenerator;
import com.jainam.sentiment.dao.UserDAO;
import com.jainam.sentiment.model.User;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@WebListener
public class ReportSchedulerListener implements ServletContextListener 
{
    private ScheduledExecutorService scheduler;
    private ReportGenerator reportGenerator;
    private UserDAO userDAO;

    @Override
    public void contextInitialized(ServletContextEvent sce) 
    {
        reportGenerator = new ReportGenerator();
        userDAO = new UserDAO();
        scheduler = Executors.newScheduledThreadPool(10);
        System.out.println("📧 Enhanced Report Scheduler Starting");
        scheduleUserReports();
    }
    
    private void scheduleUserReports() 
    {
        List<User> users = userDAO.getAllUsers();
        int scheduledCount = 0;
        for (User user : users) 
        {
            if (!user.isReceiveReports() || user.getEmail() == null) continue;
            scheduleReportForUser(user);
            scheduledCount++;
        }
        
        System.out.println("Scheduled reports for " + scheduledCount + " users");
    }
    
    private void scheduleReportForUser(User user) 
    {
        int hour = user.getReportHour();
        int minute = user.getReportMinute();
        String timezone = user.getReportTimezone();
        String frequency = user.getReportFrequency();
        long initialDelay = calculateInitialDelay(hour, minute, timezone);
        long period = getPeriodInSeconds(frequency);
        Runnable reportTask = () -> 
        {
            try 
            {
                System.out.println("Sending " + frequency + " report to " + user.getUsername() + " at their preferred time " + hour + ":" + String.format("%02d", minute));  
                if ("daily".equals(frequency)) 
                {
                    reportGenerator.generateAndSendDailyReportForUser(user.getId());
                } 
                else if ("weekly".equals(frequency)) 
                {
                    reportGenerator.generateAndSendWeeklyReportForUser(user.getId());
                } 
                else if ("monthly".equals(frequency)) 
                {
                    reportGenerator.generateAndSendMonthlyReportForUser(user.getId());
                }
            } catch (Exception e) {
                System.err.println("Failed to send report to user " + user.getUsername() + ": " + e.getMessage());
                e.printStackTrace();
            }
        };
        scheduler.scheduleAtFixedRate(reportTask, initialDelay, period, TimeUnit.SECONDS);
        System.out.println("Scheduled " + frequency + " reports for " + user.getUsername() + " at " + String.format("%02d:%02d", hour, minute) + " " + timezone);
    }
    
    private long calculateInitialDelay(int targetHour, int targetMinute, String timezone) 
    {
        try 
        {
            ZoneId zoneId = ZoneId.of(timezone);
            ZonedDateTime now = ZonedDateTime.now(zoneId);
            ZonedDateTime nextRun = now.withHour(targetHour).withMinute(targetMinute).withSecond(0).withNano(0); 
            if (now.compareTo(nextRun) > 0) 
            {
                nextRun = nextRun.plusDays(1);
            }
            long seconds = java.time.Duration.between(now, nextRun).getSeconds();
            System.out.println("Next report for timezone " + timezone + " in " + seconds/3600 + " hours");
            return seconds;
        } catch (Exception e) {
            System.err.println("Error calculating delay for timezone " + timezone + ": " + e.getMessage());
            return 3600;
        }
    }
    
    private long getPeriodInSeconds(String frequency) 
    {
        switch (frequency) 
        {
            case "daily": return 24 * 60 * 60;
            case "weekly": return 7 * 24 * 60 * 60;
            case "monthly": return 30 * 24 * 60 * 60;
            default: return 24 * 60 * 60;
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) 
    {
        if (scheduler != null) 
        {
            scheduler.shutdown();
            try 
            {
                if (!scheduler.awaitTermination(30, TimeUnit.SECONDS)) 
                {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
            }
            System.out.println("Report scheduler stopped");
        }
    }
}