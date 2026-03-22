/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.jainam.sentiment.dao;

/**
 *
 * @author Jainam Mehta
 */

import com.jainam.sentiment.utils.DBConnection;
import java.sql.*;
import java.util.*;
import com.jainam.sentiment.model.User;

public class PostDAO 
{
    
    public boolean createPost(String keyword, String content, String sentiment, int score, Integer userId) 
    {
        String sql = "INSERT INTO posts (keyword, content, sentiment, sent_score, user_id) VALUES (?, ?, ?, ?, ?)";
        boolean rowInserted = false;
    
        try (Connection con = DBConnection.getConnection();PreparedStatement statement = con.prepareStatement(sql)) 
        {
            statement.setString(1, keyword);
            statement.setString(2, content);
            statement.setString(3, sentiment);
            statement.setInt(4, score);
            if (userId != null) 
            {
                statement.setInt(5, userId);
            }
            else 
            {
                statement.setNull(5, java.sql.Types.INTEGER);
            }
            rowInserted = statement.executeUpdate() > 0;
            if (rowInserted) 
            {
                System.out.println("New row added for user: " + userId);
            }
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
        return rowInserted;
    }
    
    public List<String[]> getPost() 
    {
        String sql="select * from posts";
        List<String[]> posts=new ArrayList<>();
        try (Connection con=DBConnection.getConnection();PreparedStatement statement=con.prepareStatement(sql)) 
        {
            ResultSet result= statement.executeQuery();
            while (result.next()) 
            {
                String[] postData = new String[6];
                postData[0]=String.valueOf(result.getInt("id"));
                postData[1]=result.getString("keyword");
                postData[2]=result.getString("content");
                postData[3]=result.getString("sentiment");
                postData[4]=String.valueOf(result.getInt("sent_score"));
                postData[5]=result.getString("created_at");
                posts.add(postData);
            }
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
        return posts;
    }
    
    public boolean createPost(String keyword, String content, String sentiment, int score) 
    {
        return createPost(keyword, content, sentiment, score, null);
    }

    public List<String[]> getPostsByUser(int userId) 
    {
        String sql = "SELECT * FROM posts WHERE user_id = ? ORDER BY created_at DESC";
        List<String[]> posts = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();PreparedStatement statement = con.prepareStatement(sql)) 
        {
            statement.setInt(1, userId);
            ResultSet result = statement.executeQuery();
            while (result.next()) 
            {
                String[] postData = new String[7]; 
                postData[0] = String.valueOf(result.getInt("id"));
                postData[1] = result.getString("keyword");
                postData[2] = result.getString("content");
                postData[3] = result.getString("sentiment");
                postData[4] = String.valueOf(result.getInt("sent_score"));
                postData[5] = result.getString("created_at");
                postData[6] = String.valueOf(result.getInt("user_id"));
                posts.add(postData);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return posts;
    }
    
    public List<String[]> getUserPostsByKeyword(int userId, String keyword) 
    {
        String sql = "SELECT content, sentiment, created_at FROM posts WHERE user_id = ? AND keyword = ? ORDER BY created_at DESC LIMIT 50";
        List<String[]> posts = new ArrayList<>();
        
        try (Connection con = DBConnection.getConnection(); PreparedStatement statement = con.prepareStatement(sql)) 
        {
            statement.setInt(1, userId);
            statement.setString(2, keyword);
            ResultSet result = statement.executeQuery();
            
            while (result.next()) 
            {
                String[] postData = new String[3];
                postData[0] = result.getString("content");
                postData[1] = result.getString("sentiment");
                postData[2] = result.getString("created_at");
                posts.add(postData);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return posts;
    }

    public int[] getUserSentimentCounts(int userId, String keyword) 
    {
        String sql = "SELECT sentiment, COUNT(*) as count FROM posts WHERE user_id = ? AND keyword = ? GROUP BY sentiment";
        int[] counts = new int[3];
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) 
        {
            stmt.setInt(1, userId);
            stmt.setString(2, keyword);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) 
            {
                String sentiment = rs.getString("sentiment");
                int count = rs.getInt("count");
                switch (sentiment) 
                {
                    case "positive": counts[0] = count; break;
                    case "neutral": counts[1] = count; break;
                    case "negative": counts[2] = count; break;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return counts;
    }
    public List<String[]> getPublicPostsByKeyword(String keyword) 
    {
        String sql = "SELECT content, sentiment, created_at FROM posts WHERE keyword = ? AND user_id IS NULL ORDER BY created_at DESC LIMIT 50";
        List<String[]> posts = new ArrayList<>();
        
        try (Connection con = DBConnection.getConnection(); PreparedStatement statement = con.prepareStatement(sql)) 
        {
            
            statement.setString(1, keyword);
            ResultSet result = statement.executeQuery();
            
            while (result.next()) 
            {
                String[] postData = new String[3];
                postData[0] = result.getString("content");
                postData[1] = result.getString("sentiment");
                postData[2] = result.getString("created_at");
                posts.add(postData);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return posts;
    }
    
    public int[] getPublicSentimentCounts(String keyword) 
    {
        String sql = "SELECT sentiment, COUNT(*) as count FROM posts WHERE keyword = ? AND user_id IS NULL GROUP BY sentiment";
        int[] counts = new int[3];
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) 
        {
            stmt.setString(1, keyword);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) 
            {
                String sentiment = rs.getString("sentiment");
                int count = rs.getInt("count");
                switch (sentiment) 
                {
                    case "positive": counts[0] = count; break;
                    case "neutral": counts[1] = count; break;
                    case "negative": counts[2] = count; break;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return counts;
    }
    
    public List<String[]> getAllPostsByKeyword(String keyword) 
    {
        String sql = "SELECT content, sentiment, created_at FROM posts WHERE keyword = ? ORDER BY created_at DESC LIMIT 50";
        List<String[]> posts = new ArrayList<>();
        
        try (Connection con = DBConnection.getConnection(); PreparedStatement statement = con.prepareStatement(sql)) 
        {
            statement.setString(1, keyword);
            ResultSet result = statement.executeQuery();
            
            while (result.next()) 
            {
                String[] postData = new String[3];
                postData[0] = result.getString("content");
                postData[1] = result.getString("sentiment");
                postData[2] = result.getString("created_at");
                posts.add(postData);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return posts;
    }
    public int[] getSentimentCounts(String keyword) 
    {
        String sql = "SELECT sentiment, COUNT(*) as count FROM posts WHERE keyword = ? GROUP BY sentiment";
        int[] counts = new int[3];
        try (Connection conn=DBConnection.getConnection();PreparedStatement stmt = conn.prepareStatement(sql)) 
        {
            stmt.setString(1, keyword);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) 
            {
                String sentiment = rs.getString("sentiment");
                int count = rs.getInt("count");
                switch (sentiment) 
                {
                    case "positive": counts[0] = count; break;
                    case "neutral": counts[1] = count; break;
                    case "negative": counts[2] = count; break;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return counts;
    }
    
    public List<String[]> getUserPostsInRange(int userId, int hours) 
    {
        String sql = "SELECT content, sentiment, created_at, keyword FROM posts " +
                 "WHERE user_id = ? AND created_at >= NOW() - INTERVAL ? HOUR " +
                 "ORDER BY created_at DESC";
        List<String[]> posts = new ArrayList<>();
    
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) 
        {
            stmt.setInt(1, userId);
            stmt.setInt(2, hours);
            ResultSet rs = stmt.executeQuery();
        
            while (rs.next()) 
            {
                String[] postData = new String[4];
                postData[0] = rs.getString("content");
                postData[1] = rs.getString("sentiment");
                postData[2] = rs.getString("created_at");
                postData[3] = rs.getString("keyword");
                posts.add(postData);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return posts;
    }
    
    public static void main(String[] args) 
    {
        PostDAO postDAO= new PostDAO();
        System.out.println("Test Case 1");
        boolean success = postDAO.createPost("TestProduct", "This is a test from Java.", "positive", 1);
        System.out.println(success);
        System.out.println("\n");
        List<String[]> post = postDAO.getPost();
        for(int i=0;i<post.size();i++) 
        {
            String[] row=post.get(i);
            for(int j=0;j<6;j++) 
            {
                System.out.print(row[j]+" "); 
            }
        System.out.println();
        }
    }
}