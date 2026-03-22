/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.jainam.sentiment.listener;

/**
 *
 * @author Jainam Mehta
 */

import com.jainam.sentiment.utils.DBConnection;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class DBCleanupListener implements ServletContextListener 
{
    @Override
    public void contextInitialized(ServletContextEvent sce) 
    {
        System.out.println("DBCleanupListener initialized");
    }
    
    @Override
    public void contextDestroyed(ServletContextEvent sce) 
    {
        System.out.println("SHUTTING DOWN APPLICATION");
        System.out.println("Cleaning up MySQL resources");
        DBConnection.deregisterDriver();
        System.out.println("Shutdown complete");
    }
}