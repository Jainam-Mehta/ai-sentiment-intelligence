/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.jainam.sentiment.utils;

/**
 *
 * @author Jainam Mehta
 */

import java.sql.*;
import com.mysql.cj.jdbc.AbandonedConnectionCleanupThread;

public class DBConnection 
{
    private static final String url = System.getenv().getOrDefault("MYSQL_URL",
    "jdbc:mysql://" + System.getenv("MYSQL_ADDON_HOST") + ":" + System.getenv("MYSQL_ADDON_PORT") + "/" + System.getenv("MYSQL_ADDON_DB") + "?useSSL=false&serverTimezone=UTC");
    private static final String name = System.getenv().getOrDefault("MYSQL_USER", System.getenv("MYSQL_ADDON_USER"));
    private static final String password = System.getenv().getOrDefault("MYSQL_PASSWORD", System.getenv("MYSQL_ADDON_PASSWORD"));
    
    static 
    {
        try 
        {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("MySQL JDBC Driver registered");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found!");
            e.printStackTrace();
        }
    }
    
    private DBConnection() {}
    public static Connection getConnection() throws SQLException {
        try 
        {
            Connection newConn = DriverManager.getConnection(url, name, password);
            System.out.println("New database connection created");
            return newConn;
        } catch (SQLException e) {
            System.err.println("Database connection error: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    public static void closeConnection(Connection con) 
    {
        if (con != null) 
        {
            try 
            {
                con.close();
                System.out.println("Connection closed");
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
    }
    
    public static void deregisterDriver() 
    {
        try 
        {
            Driver mysqlDriver = DriverManager.getDriver("jdbc:mysql://localhost:3306/");
            DriverManager.deregisterDriver(mysqlDriver);
            System.out.println("MySQL driver deregistered");
            AbandonedConnectionCleanupThread.checkedShutdown();
            System.out.println("MySQL cleanup thread shutdown");
            
        } catch (SQLException e) {
            System.err.println("Error deregistering driver: " + e.getMessage());
        }
    }
}
