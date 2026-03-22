/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.jainam.sentiment.servlets;

/**
 *
 * @author Jainam Mehta
 */

import com.jainam.sentiment.stream.PostSource;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/getActiveStreams")
public class GetActiveStreamsServlet extends HttpServlet 
{
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        List<String> activeKeywords = new ArrayList<>();
        for (String keyword : DataStreamServlet.activeStreams.keySet()) 
        {
            activeKeywords.add(keyword);
        }
        
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"active\":[");
        
        for (int i = 0; i < activeKeywords.size(); i++) 
        {
            json.append("\"").append(activeKeywords.get(i)).append("\"");
            if (i < activeKeywords.size() - 1) 
            {
                json.append(",");
            }
        }
        
        json.append("]}");
        out.print(json.toString());
    }
}