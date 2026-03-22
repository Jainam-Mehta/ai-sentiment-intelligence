/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.jainam.sentiment.model;
import java.time.*;
/**
 *
 * @author Jainam Mehta
 */
public class Post 
{
    private LocalDateTime timestamp;
    private String keyword;
    private String content;
    private String sentiment;
    private int sent_score;
    private Integer userId;
    public Integer getUserId() 
    { 
        return userId; 
    }
    public void setUserId(Integer userId) 
    { 
        this.userId = userId; 
    }
    
    public Post(String keyword, String content, String sentiment, int sent_score)
    {
        this.keyword=keyword;
        this.content=content;
        this.sent_score=sent_score;
        this.sentiment=sentiment;
        this.timestamp=LocalDateTime.now();
    };
    
    public String getKeyword()
    {
        return keyword;
    }
    public String getSentiment()
    {
        return sentiment;
    }
    public String getContent()
    {
        return content;
    }
    public void setKeyword(String keyword)
    {
        this.keyword=keyword;
    }
    public void setSentiment(String sentiment)
    {
        this.sentiment=sentiment;
    }
    public void setContent(String content)
    {
        this.content=content;
    }
    public int getSent_score()
    {
        return sent_score;
    }
    public void setSent_score(int sent_score)
    {
        this.sent_score=sent_score;
    }
    public LocalDateTime getTimestamp()
    {
        return timestamp;
    }
    public void setTimestamp(LocalDateTime timestamp)
    {
        this.timestamp=timestamp;
    }
}