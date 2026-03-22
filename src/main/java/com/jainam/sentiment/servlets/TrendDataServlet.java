/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.jainam.sentiment.servlets;

import com.jainam.sentiment.utils.DBConnection;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.*;
import java.sql.*;
import java.util.*;

@WebServlet("/trendData")
public class TrendDataServlet extends HttpServlet 
{

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        
        String keyword = request.getParameter("keyword");
        if (keyword == null || keyword.trim().isEmpty()) 
        {
            keyword = "mustang";
        }

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        String sql = "SELECT " +
                     "DATE_FORMAT(created_at, '%H:%i') as minute, " +
                     "SUM(CASE WHEN sentiment = 'positive' THEN 1 ELSE 0 END) as pos_count, " +
                     "SUM(CASE WHEN sentiment = 'neutral' THEN 1 ELSE 0 END) as neu_count, " +
                     "SUM(CASE WHEN sentiment = 'negative' THEN 1 ELSE 0 END) as neg_count, " +
                     "COUNT(*) as total_count " +
                     "FROM posts " +
                     "WHERE keyword = ? AND created_at >= NOW() - INTERVAL 1 HOUR " +
                     "GROUP BY minute " +
                     "ORDER BY minute";

        List<Map<String, Object>> data = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) 
        {

            stmt.setString(1, keyword);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) 
            {
                Map<String, Object> point = new HashMap<>();
                point.put("minute", rs.getString("minute"));
                point.put("pos", rs.getInt("pos_count"));
                point.put("neu", rs.getInt("neu_count"));
                point.put("neg", rs.getInt("neg_count"));
                point.put("total", rs.getInt("total_count"));
                data.add(point);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\":\"" + e.getMessage() + "\"}");
            return;
        }

        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < data.size(); i++) 
        {
            Map<String, Object> point = data.get(i);
            json.append("{")
                .append("\"minute\":\"").append(point.get("minute")).append("\",")
                .append("\"pos\":").append(point.get("pos")).append(",")
                .append("\"neu\":").append(point.get("neu")).append(",")
                .append("\"neg\":").append(point.get("neg")).append(",")
                .append("\"total\":").append(point.get("total"))
                .append("}");
            if (i < data.size() - 1) json.append(",");
        }
        json.append("]");
        out.print(json.toString());
    }
}