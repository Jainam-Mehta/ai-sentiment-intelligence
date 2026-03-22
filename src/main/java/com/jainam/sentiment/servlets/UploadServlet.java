/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.jainam.sentiment.servlets;

/**
 *
 * @author Jainam Mehta
 */


import com.jainam.sentiment.model.Post;
import com.jainam.sentiment.stream.UploadQueue;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.*;
import java.util.List;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

@WebServlet("/uploadCSV")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024,
    maxFileSize = 1024 * 1024 * 10,
    maxRequestSize = 1024 * 1024 * 15 )
public class UploadServlet extends HttpServlet 
{
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Part filePart = request.getPart("file");
        String keyword = request.getParameter("keyword");
        String textColumnStr = request.getParameter("textColumn");
        String hasHeaderStr = request.getParameter("hasHeader");

        if (filePart == null || keyword == null || keyword.trim().isEmpty()) 
        {
            response.sendRedirect("upload.html?error=missing");
            return;
        }

        int textColumn = 0;
        try 
        {
            textColumn = Integer.parseInt(textColumnStr) - 1;
    
            if (textColumn < 0) 
            {
                textColumn = 0;
            }
        } catch (NumberFormatException e) {
            response.sendRedirect("upload.html?error=invalidColumn");
            return;
        }
        
        boolean hasHeader = "true".equals(hasHeaderStr);

        HttpSession session = request.getSession(false);
        Integer userId = (session != null) ? (Integer) session.getAttribute("userId") : null;

        int total = 0;
        int errors = 0;
        int skipped = 0;

        try (InputStream fileContent = filePart.getInputStream();Reader reader = new InputStreamReader(fileContent);CSVReader csvReader = new CSVReader(reader)) 
        {
            List<String[]> rows = csvReader.readAll();
            int startRow = hasHeader ? 1 : 0;
            
            for (int i = startRow; i < rows.size(); i++) 
            {
                String[] row = rows.get(i);
                if (row.length <= textColumn) 
                {
                    System.out.println("Row " + (i+1) + " has only " + row.length + " columns, skipping (needed column " + textColumn + ")");
                    skipped++;
                    continue;
                }
                String text = row[textColumn].trim();
                if (text.isEmpty()) 
                {
                    skipped++;
                    continue;
                }
                Post post = new Post(keyword, text, null, 0);
                post.setUserId(userId);

                try 
                {
                    UploadQueue.getQueue().put(post);
                    total++;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    errors++;
                }
            }

            String redirect = "upload.html?success=" + total + "&skipped=" + skipped + "&errors=" + errors;
            response.sendRedirect(redirect);

        } catch (IOException | CsvException e) {
            e.printStackTrace();
            response.sendRedirect("upload.html?error=processing");
        }
    }
}