/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.jainam.sentiment.dao;

/**
 *
 * @author Jainam Mehta
 */

import com.jainam.sentiment.model.User;
import com.jainam.sentiment.utils.DBConnection;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UserDAO 
{
    
    public boolean register(User user, String plainPassword) {
        String sql = "INSERT INTO users (username, password, email, full_name, role) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS))
        {
            String hashedPassword = BCrypt.hashpw(plainPassword, BCrypt.gensalt());
            stmt.setString(1, user.getUsername());
            stmt.setString(2, hashedPassword);
            stmt.setString(3, user.getEmail());
            stmt.setString(4, user.getFullName());
            stmt.setString(5, user.getRole() != null ? user.getRole() : "user");
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) 
            {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) 
                {
                    user.setId(rs.getInt(1));
                }
                System.out.println("User registered: " + user.getUsername());
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Registration error: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    public User authenticate(String username, String plainPassword) 
    {
        String sql = "SELECT * FROM users WHERE username = ? AND is_active = TRUE";
        try (Connection conn = DBConnection.getConnection();PreparedStatement stmt = conn.prepareStatement(sql)) 
        {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) 
            {
                String hashedPassword = rs.getString("password");
                if (BCrypt.checkpw(plainPassword, hashedPassword)) 
                {
                    System.out.println("User authenticated: " + username);
                    return mapResultSetToUser(rs);
                } 
                else 
                {
                    System.out.println("Invalid password for: " + username);
                }
            } 
            else 
            {
                System.out.println("User not found: " + username);
            }
        } catch (SQLException e) {
            System.err.println("Authentication error: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    
    public User getUserById(int userId) 
    {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) 
        {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) 
            {
                return mapResultSetToUser(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error getting user by ID " + userId + ": " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    public User getUserByUsername(String username) 
    {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) 
        {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) 
            {
                return mapResultSetToUser(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error getting user by username " + username + ": " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    public void updateLastLogin(int userId) 
    {
        String sql = "UPDATE users SET last_login = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();PreparedStatement stmt = conn.prepareStatement(sql)) 
        {
            stmt.setInt(1, userId);
            int updated = stmt.executeUpdate();
            if (updated > 0) 
            {
                System.out.println("Updated last login for user ID: " + userId);
            }
        } catch (SQLException e) {
            System.err.println("Error updating last login: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public String createSession(int userId, String ipAddress, String userAgent) 
    {
        String sql = "INSERT INTO user_sessions (user_id, session_token, ip_address, user_agent, expires_at) VALUES (?, ?, ?, ?, DATE_ADD(NOW(), INTERVAL 7 DAY))";
        String sessionToken = UUID.randomUUID().toString();
        
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) 
        {
            stmt.setInt(1, userId);
            stmt.setString(2, sessionToken);
            stmt.setString(3, ipAddress);
            stmt.setString(4, userAgent);  
            stmt.executeUpdate();
            System.out.println("Session created for user ID: " + userId + " token: " + sessionToken);
            return sessionToken;
        } catch (SQLException e) {
            System.err.println("Error creating session: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    
    public Integer validateSession(String sessionToken) 
    {
        if (sessionToken == null || sessionToken.trim().isEmpty()) 
        {
            System.out.println("validateSession: Empty session token");
            return null;
        }
        
        String sql = "SELECT user_id FROM user_sessions WHERE session_token = ? AND expires_at > NOW()";
        
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) 
        {
            stmt.setString(1, sessionToken);
            System.out.println("Validating session token: " + sessionToken);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) 
            {
                int userId = rs.getInt("user_id");
                System.out.println("Session valid for user ID: " + userId);
                refreshSessionExpiry(sessionToken);
                return userId;
            } 
            else 
            {
                System.out.println("Invalid or expired session token");
            }
            
        } catch (SQLException e) {
            System.err.println("Error validating session: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    
    private void refreshSessionExpiry(String sessionToken) 
    {
        String sql = "UPDATE user_sessions SET expires_at = DATE_ADD(NOW(), INTERVAL 7 DAY) WHERE session_token = ?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) 
        {
            stmt.setString(1, sessionToken);
            int updated = stmt.executeUpdate();
            if (updated > 0) 
            {
                System.out.println("Session expiry refreshed for token: " + sessionToken);
            }
        } catch (SQLException e) {
            System.err.println("Error refreshing session expiry: " + e.getMessage());
        }
    }
    
    public void invalidateSession(String sessionToken) 
    {
        if (sessionToken == null || sessionToken.trim().isEmpty()) 
        {
            return;
        }
        String sql = "DELETE FROM user_sessions WHERE session_token = ?";
        
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) 
        {
            stmt.setString(1, sessionToken);
            int deleted = stmt.executeUpdate();
            if (deleted > 0) 
            {
                System.out.println("Session invalidated: " + sessionToken);
            }
        } catch (SQLException e) {
            System.err.println("Error invalidating session: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public List<String> getUserKeywords(int userId) 
    {
        List<String> keywords = new ArrayList<>();
        String sql = "SELECT keyword FROM user_keywords WHERE user_id = ? AND is_active = TRUE";
        
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) 
        {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) 
            {
                keywords.add(rs.getString("keyword"));
            }
            System.out.println("Retrieved " + keywords.size() + " keywords for user ID: " + userId);
        } catch (SQLException e) {
            System.err.println("Error getting user keywords: " + e.getMessage());
            e.printStackTrace();
        }
        return keywords;
    }
    
    public boolean addUserKeyword(int userId, String keyword) 
    {
        System.out.println("🔍 addUserKeyword CALLED - userId: " + userId + ", keyword: '" + keyword + "'");
        String sql = "INSERT INTO user_keywords (user_id, keyword) VALUES (?, ?) ON DUPLICATE KEY UPDATE is_active = TRUE";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) 
        {
            stmt.setInt(1, userId);
            stmt.setString(2, keyword);
            int updated = stmt.executeUpdate();
            if (updated > 0) 
            {
                System.out.println("Keyword '" + keyword + "' added for user ID: " + userId);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error adding keyword: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    public boolean removeUserKeyword(int userId, String keyword) 
    {
        String sql = "UPDATE user_keywords SET is_active = FALSE WHERE user_id = ? AND keyword = ?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) 
        {
            stmt.setInt(1, userId);
            stmt.setString(2, keyword);
            int updated = stmt.executeUpdate();
            if (updated > 0) 
            {
                System.out.println("Keyword '" + keyword + "' removed for user ID: " + userId);
                return true;
            }
        } catch (SQLException e) 
        {
            System.err.println("Error removing keyword: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    private User mapResultSetToUser(ResultSet rs) throws SQLException 
    {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setEmail(rs.getString("email"));
        try 
        {
            user.setFullName(rs.getString("full_name"));
        } catch (SQLException e) {
            try 
            {
                user.setFullName(rs.getString("fullname")); 
            } catch (SQLException ex) {
                user.setFullName("");
            }
        }
        
        try 
        {
            user.setReportHour(rs.getInt("report_hour"));
        } catch (SQLException e) {
            user.setReportHour(8);
        }

        try 
        {
            user.setReportMinute(rs.getInt("report_minute"));
        } catch (SQLException e) {
            user.setReportMinute(0);
        }

        try 
        {
            user.setReportTimezone(rs.getString("report_timezone"));
        } catch (SQLException e) {
            user.setReportTimezone("Asia/Kolkata");
        }
        
        try 
        {
            user.setRole(rs.getString("role"));
        } catch (SQLException e) {
            user.setRole("user"); 
        }
        
        try 
        {
            user.setCreatedAt(rs.getTimestamp("created_at"));
        } catch (SQLException e) {}
        
        try 
        {
            user.setLastLogin(rs.getTimestamp("last_login"));
        } catch (SQLException e) {}
        
        try 
        {
            user.setActive(rs.getBoolean("is_active"));
        } catch (SQLException e) {
            user.setActive(true);
        }
        
        try 
        {
            user.setReceiveReports(rs.getBoolean("receive_reports"));
        } catch (SQLException e) {
            user.setReceiveReports(true);
        }
        
        try 
        {
            user.setReportFrequency(rs.getString("report_frequency"));
        } catch (SQLException e) {
            user.setReportFrequency("daily");
        }
        
        try 
        {
            user.setReportFormat(rs.getString("report_format"));
        } catch (SQLException e) {
            user.setReportFormat("html");
        }
        
        try 
        {
            user.setLastReportSent(rs.getTimestamp("last_report_sent"));
        } catch (SQLException e) {}
        
        return user;   
    }
    
    public List<User> getAllUsers() 
    {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE email IS NOT NULL AND email != ''";
        
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) 
        {
            while (rs.next()) 
            {
                users.add(mapResultSetToUser(rs));
            }
            System.out.println("Retrieved " + users.size() + " users with emails");
        } catch (SQLException e) {
            System.err.println("Error getting all users: " + e.getMessage());
            e.printStackTrace();
        }
        return users;
    }
    
    public boolean updateEmailPreferences(int userId, String email, boolean receiveReports, String frequency, String format, int hour, int minute, String timezone) 
    {
        String sql = "UPDATE users SET email = ?, receive_reports = ?, " +
                 "report_frequency = ?, report_format = ?, " +
                 "report_hour = ?, report_minute = ?, report_timezone = ? " +
                 "WHERE id = ?";
    
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) 
        {
            stmt.setString(1, email);
            stmt.setBoolean(2, receiveReports);
            stmt.setString(3, frequency);
            stmt.setString(4, format);
            stmt.setInt(5, hour);
            stmt.setInt(6, minute);
            stmt.setString(7, timezone);
            stmt.setInt(8, userId);
        
            int updated = stmt.executeUpdate();
            if (updated > 0) 
            {
                System.out.println("Email preferences updated for user ID: " + userId);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error updating email preferences: " + e.getMessage());
            e.printStackTrace();
        }
    return false;
    }
}