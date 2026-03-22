/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.jainam.sentiment.servlets;

/**
 *
 * @author Jainam Mehta
 */

import com.jainam.sentiment.stream.streamConsumer;
import com.jainam.sentiment.stream.streamGenerator;
import com.jainam.sentiment.stream.RedditStreamGenerator;
import com.jainam.sentiment.stream.PostSource;
import com.jainam.sentiment.dao.UserDAO;
import java.util.concurrent.ConcurrentHashMap;
import java.io.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import jakarta.servlet.ServletException;

@WebServlet("/startStream")
public class DataStreamServlet extends HttpServlet 
{
    
    public static ConcurrentHashMap<String, StreamInfo> activeStreams = new ConcurrentHashMap<>();
    private UserDAO userDAO = new UserDAO();
    
    public static class StreamInfo 
    {
        public PostSource source;
        public streamConsumer consumer;
        public Thread sourceThread;
        public Thread consumerThread;
        public String keyword;
        public String sourceType;
        public Integer userId;
        public String mode;
        
        public StreamInfo(PostSource s, streamConsumer c, Thread st, Thread ct, String kw, String srcType, Integer uid, String md) 
        {
            this.source = s;
            this.consumer = c;
            this.sourceThread = st;
            this.consumerThread = ct;
            this.keyword = kw;
            this.sourceType = srcType;
            this.userId = uid;
            this.mode = md;
        }
    }
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String mode = request.getParameter("mode");
        String keyword = request.getParameter("keyword");
        String action = request.getParameter("action");
        String source = request.getParameter("source");
        
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        
        if (keyword == null || keyword.trim().isEmpty()) 
        {
            out.write("{\"status\":\"error\",\"message\":\"Keyword required\"}");
            return;
        }
        
        keyword = keyword.trim();
        
        try 
        {
            if ("start".equals(action)) 
            {
                
                if (activeStreams.containsKey(keyword)) 
                {
                    out.write("{\"status\":\"error\",\"message\":\"Stream already running\"}");
                    return;
                }
                
                PostSource postSource = null;
                Thread sourceThread = null;
                String sourceType = "sim";
                
                if ("reddit".equals(source)) 
                {
                    RedditStreamGenerator generator = new RedditStreamGenerator(keyword);
                    postSource = generator;
                    sourceThread = new Thread(generator, "Reddit-" + keyword);
                    sourceType = "reddit";
                    System.out.println("Created REDDIT source for: " + keyword);
                }
                else 
                {
                    streamGenerator generator = new streamGenerator(keyword);
                    postSource = generator;
                    sourceThread = new Thread(generator, "Simulated-" + keyword);
                    sourceType = "sim";
                    System.out.println("Created SIMULATED source for: " + keyword);
                }
                
                Integer userId = null;
                if ("private".equals(mode)) 
                {
                    HttpSession session = request.getSession(false);
                    userId = (session != null) ? (Integer) session.getAttribute("userId") : null;
                    System.out.println("PRIVATE MODE : userId from session: " + userId);
                } 
                else 
                {
                    System.out.println("?PUBLIC/GUEST mode : no userId needed");
                }
                
                streamConsumer consumer = new streamConsumer(postSource, userId);
                Thread consumerThread = new Thread(consumer, "Consumer-" + keyword);
                
                sourceThread.start();
                consumerThread.start();
                
                activeStreams.put(keyword, new StreamInfo(postSource, consumer, sourceThread, consumerThread, keyword, sourceType, userId, mode));
                
                if ("private".equals(mode) && userId != null) 
                {
                    boolean saved = userDAO.addUserKeyword(userId, keyword);
                    if (saved) 
                    {
                        System.out.println("Keyword '" + keyword + "' saved for user ID: " + userId);
                    } 
                    else 
                    {
                        System.out.println("Keyword '" + keyword + "' already exists for user ID: " + userId);
                    }
                }
                
                System.out.println("THREADS STARTED - Source: " + sourceThread.getName());
                System.out.println("Consumer: " + consumerThread.getName());
                System.out.println("Active streams: " + activeStreams.size());
                
                out.write("{\"status\":\"success\",\"message\":\"Stream started for " + keyword + "\"}");
                
            } 
            else if ("stop".equals(action)) 
            {
                StreamInfo info = activeStreams.remove(keyword);
                if (info != null) 
                {
                    info.source.stop();
                    info.consumer.stop();
                    info.sourceThread.interrupt();
                    info.consumerThread.interrupt();
                    
                    out.write("{\"status\":\"success\",\"message\":\"Stream stopped for " + keyword + "\"}");
                    System.out.println("Stream stopped for: " + keyword);
                    System.out.println("Active streams remaining: " + activeStreams.size());
                } 
                else 
                {
                    out.write("{\"status\":\"error\",\"message\":\"No active stream for " + keyword + "\"}");
                }
            } 
            else 
            {
                out.write("{\"status\":\"error\",\"message\":\"Invalid action\"}");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            out.write("{\"status\":\"error\",\"message\":\"" + e.getMessage() + "\"}");
        }
    }
}