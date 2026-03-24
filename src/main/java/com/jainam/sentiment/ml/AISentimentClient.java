/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.jainam.sentiment.ml;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class AISentimentClient 
{
    private static final String AI_SERVICE_URL = System.getenv().getOrDefault("AI_SERVICE_URL", "http://localhost:5001/analyze");
    private static final int TIMEOUT_SECONDS = 5;
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(TIMEOUT_SECONDS))
            .build();
    private static final Gson gson = new Gson();
    
    public static class SentimentResult 
    {
        private final String sentiment;
        private final double confidence;
        private final int score; 
        private final boolean success;
        private final String errorMessage;
        
        private SentimentResult(String sentiment, double confidence) 
        {
            this.sentiment = sentiment;
            this.confidence = confidence;
            this.success = true;
            this.errorMessage = null;
            
            if ("positive".equals(sentiment)) 
            {
                this.score = 1;
            } 
            else if ("negative".equals(sentiment)) 
            {
                this.score = -1;
            } 
            else 
            {
                this.score = 0;
            }
        }
        
        private SentimentResult(String errorMessage) 
        {
            this.sentiment = "neutral";
            this.confidence = 0.0;
            this.score = 0; 
            this.success = false;
            this.errorMessage = errorMessage;
        }
        
        public String getSentiment() { return sentiment; }
        public double getConfidence() { return confidence; }
        public int getScore() { return score; }  
        public boolean isSuccess() { return success; }
        public String getErrorMessage() { return errorMessage; }
        
        @Override
        public String toString() 
        {
            return String.format("SentimentResult{success=%s, sentiment='%s', confidence=%.2f, score=%d, error='%s'}", success, sentiment, confidence, score, errorMessage);
        }
    }
    
    public static SentimentResult analyze(String text) 
    {
        System.out.println("AI Client: Starting analysis for: " + text.substring(0, Math.min(30, text.length())));
    
        if (text == null || text.trim().isEmpty()) 
        {
            System.out.println("AI Client: Empty text provided");
            return new SentimentResult("Empty text provided");
        }
    
        try 
        {
            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("text", text);
            String requestJson = requestBody.toString();
            System.out.println("AI Client: Sending request: " + requestJson);
        
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(AI_SERVICE_URL))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                    .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                    .build();
        
            System.out.println("AI Client: Waiting for response");
            long start = System.currentTimeMillis();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            long end = System.currentTimeMillis();
        
            System.out.println("AI Client: Response time: " + (end - start) + "ms");
            System.out.println("AI Client: Response status: " + response.statusCode());
            System.out.println("AI Client: Response body: " + response.body());
        
            if (response.statusCode() != 200) 
            {
                System.out.println("AI Client: Non-200 status code");
                return new SentimentResult("AI service error: " + response.statusCode());
            }
        
            JsonObject jsonResponse = JsonParser.parseString(response.body()).getAsJsonObject();
            System.out.println("AI Client: Parsed JSON: " + jsonResponse);
        
            String sentiment = jsonResponse.get("sentiment").getAsString();
            double confidence = jsonResponse.get("confidence").getAsDouble();
        
            System.out.println("AI Client: Success - sentiment=" + sentiment + ", confidence=" + confidence);
            
            SentimentResult result = new SentimentResult(sentiment, confidence);
            System.out.println("AI Client: Mapped score=" + result.getScore() + " for sentiment=" + sentiment);
            
            return result;
        
        } catch (java.net.ConnectException e) {
            System.out.println("AI Client: Connection refused - Is Python service running on port 5001?");
            return new SentimentResult("Cannot connect to AI service");
        } catch (java.net.http.HttpTimeoutException e) {
            System.out.println("AI Client: Timeout after " + TIMEOUT_SECONDS + " seconds");
            return new SentimentResult("AI service timeout");
        } catch (Exception e) {
            System.out.println("AI Client: Exception: " + e.getMessage());
            e.printStackTrace();
            return new SentimentResult("Error: " + e.getMessage());
        }
    }
}