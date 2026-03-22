package com.jainam.sentiment.servlets;

import com.jainam.sentiment.dao.PostDAO;
import com.jainam.sentiment.dao.UserDAO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.*;
import java.util.*;
import jakarta.servlet.ServletException;

@WebServlet("/liveData")
public class LiveDataServlet extends HttpServlet 
{
    
    private PostDAO postDAO = new PostDAO();
    private UserDAO userDAO = new UserDAO();
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String keyword = request.getParameter("keyword");
        String mode = request.getParameter("mode");
        
        if (keyword == null || keyword.trim().isEmpty()) 
        {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Keyword required");
            return;
        }
        if (mode == null || mode.trim().isEmpty()) 
        {
            mode = "public";
        }
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        PrintWriter out = response.getWriter();
        
        try 
        {
            List<String[]> posts;
            int[] counts;
            
            if ("public".equals(mode)) 
            {
                System.out.println("LiveData: Public mode for keyword: " + keyword);
                posts = postDAO.getAllPostsByKeyword(keyword);
                counts = postDAO.getSentimentCounts(keyword);
                
            } 
            else if ("guest".equals(mode)) 
            {
                System.out.println("LiveData: Guest mode for keyword: " + keyword);
                posts = postDAO.getPublicPostsByKeyword(keyword);
                counts = postDAO.getPublicSentimentCounts(keyword);
                
            } 
            else 
            {
                System.out.println("LiveData: Private mode for keyword: " + keyword);
                HttpSession session = request.getSession(false);
                Integer userId = null;
                
                if (session != null && session.getAttribute("userId") != null) 
                {
                    userId = (Integer) session.getAttribute("userId");
                } 
                else 
                {
                    Cookie[] cookies = request.getCookies();
                    if (cookies != null) 
                    {
                        for (Cookie cookie : cookies) 
                        {
                            if ("sessionToken".equals(cookie.getName())) 
                            {
                                userId = userDAO.validateSession(cookie.getValue());
                                if (userId != null) 
                                {
                                    session = request.getSession(true);
                                    session.setAttribute("userId", userId);
                                }
                                break;
                            }
                        }
                    }
                }
                
                if (userId == null) 
                {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    out.write("{\"error\":\"Please login for private mode\"}");
                    return;
                }
                posts = postDAO.getUserPostsByKeyword(userId, keyword);
                counts = postDAO.getUserSentimentCounts(userId, keyword);
            }
            StringBuilder json = new StringBuilder();
            json.append("{");
            json.append("\"keyword\":\"").append(escapeJson(keyword)).append("\",");
            json.append("\"mode\":\"").append(mode).append("\",");
            json.append("\"stats\":{");
            json.append("\"positive\":").append(counts[0]).append(",");
            json.append("\"neutral\":").append(counts[1]).append(",");
            json.append("\"negative\":").append(counts[2]);
            json.append("},");
            json.append("\"posts\":[");
            
            for (int i = 0; i < posts.size(); i++) 
            {
                String[] post = posts.get(i);
                json.append("{");
                json.append("\"content\":\"").append(escapeJson(post[0])).append("\",");
                json.append("\"sentiment\":\"").append(escapeJson(post[1])).append("\",");
                json.append("\"time\":\"").append(escapeJson(post[2])).append("\"");
                json.append("}");
                if (i < posts.size() - 1) json.append(",");
            }
            
            json.append("]");
            json.append("}");
            
            out.print(json.toString());
            
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
        }
    }
    
    private String escapeJson(String s) 
    {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}