/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.jainam.sentiment.listener;

/**
 *
 * @author Jainam Mehta
 */

import com.jainam.sentiment.servlets.DataStreamServlet;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class StreamRestoreListener implements ServletContextListener 
{
    @Override
    public void contextInitialized(ServletContextEvent sce) 
    {
        System.out.println("APPLICATION STARTING UP");
        System.out.println("Active streams at startup: " + DataStreamServlet.activeStreams.size());
    }
    
    @Override
    public void contextDestroyed(ServletContextEvent sce)
    {
        System.out.println("APPLICATION SHUTTING DOWN");
        int streamCount = DataStreamServlet.activeStreams.size();
        System.out.println("Cleaning up " + streamCount + " active streams");
        for (String keyword : DataStreamServlet.activeStreams.keySet()) 
        {
            DataStreamServlet.StreamInfo info = DataStreamServlet.activeStreams.get(keyword);
            if (info != null) 
            {
                try 
                {
                    System.out.println("Stopping stream for: " + keyword);
                    info.source.stop();
                    info.consumer.stop();
                    info.sourceThread.interrupt();
                    info.consumerThread.interrupt();
                    System.out.println("Stopped stream for: " + keyword);
                } catch (Exception e) {
                    System.err.println("Error stopping stream for " + keyword + ": " + e.getMessage());
                }
            }
        }
        
        DataStreamServlet.activeStreams.clear();
        System.out.println("All streams cleaned up");
    }
}