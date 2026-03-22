/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.jainam.sentiment.servlets;

import com.jainam.sentiment.dao.PostDAO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.*;
import java.util.List;

@WebServlet("/export")
public class ExportServlet extends HttpServlet 
{
    private PostDAO postDAO = new PostDAO();
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        
        String keyword = request.getParameter("keyword");
        if (keyword == null || keyword.trim().isEmpty()) 
        {
            keyword = "mustang"; 
        }
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment;filename=\"" + keyword + "_data.csv\"");
        PrintWriter out = response.getWriter();
        out.println("ID,Keyword,Content,Sentiment,Score,Timestamp");
        
        List<String[]> allPosts = postDAO.getPost();
        for (String[] post : allPosts) 
        {
            if (post[1].equals(keyword)) 
            {
                String content = post[2].replace("\"", "\"\"");
                out.println(
                    post[0] + "," +
                    post[1] + "," +
                    "\"" + content + "\"," +
                    post[3] + "," +
                    post[4] + "," +
                    post[5]
                );
            }
        }
    }
}