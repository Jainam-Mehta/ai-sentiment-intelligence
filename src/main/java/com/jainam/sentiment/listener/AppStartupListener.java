/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.jainam.sentiment.listener;

/**
 *
 * @author Jainam Mehta
 */

import com.jainam.sentiment.stream.UploadQueue;
import com.jainam.sentiment.stream.streamConsumer;
import com.jainam.sentiment.stream.PostSource;
import com.jainam.sentiment.model.Post;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@WebListener
public class AppStartupListener implements ServletContextListener 
{
    private Thread consumerThread;
    private volatile boolean running = true;
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("AppStartupListener: Initializing upload consumer");
        try 
        {
            if (UploadQueue.getQueue() == null) 
            {
                System.err.println("AppStartupListener: UploadQueue is null!");
                return;
            }
            
            PostSource uploadSource = new PostSource() 
            {
                @Override
                public BlockingQueue<Post> getPostQueue() 
                {
                    return UploadQueue.getQueue();
                }
                @Override
                public void stop() 
                {
                    System.out.println("Upload source stopping");
                }
            };

            streamConsumer consumer = new streamConsumer(uploadSource, null);
            consumerThread = new Thread(consumer, "Upload-Consumer");
            consumerThread.setDaemon(true);
            consumerThread.start();
            
            System.out.println("Upload consumer started successfully");
            ScheduledExecutorService monitor = Executors.newSingleThreadScheduledExecutor();
            monitor.scheduleAtFixedRate(() -> 
            {
                if (!consumerThread.isAlive()) 
                {
                    System.err.println("Upload consumer thread died! Restarting");
                    Thread newThread = new Thread(consumer, "Upload-Consumer");
                    newThread.setDaemon(true);
                    newThread.start();
                    consumerThread = newThread;
                }
            }, 10, 10, TimeUnit.SECONDS);
            
        } catch (Exception e) {
            System.err.println("AppStartupListener: Failed to start upload consumer!");
            e.printStackTrace();
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) 
    {
        System.out.println("AppStartupListener: Shutting down upload consumer");
        running = false;
        if (consumerThread != null) 
        {
            consumerThread.interrupt();
            try 
            {
                consumerThread.join(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        System.out.println("AppStartupListener: Shutdown complete");
    }
}