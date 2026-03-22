/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.jainam.sentiment.stream;

import com.jainam.sentiment.model.Post;
import com.jainam.sentiment.dao.PostDAO;
import com.jainam.sentiment.dao.UserDAO;
import com.jainam.sentiment.analysis.sentimentAnalysis;
import com.jainam.sentiment.ml.AISentimentClient;

public class streamConsumer implements Runnable 
{
    private volatile boolean running = true;
    private PostDAO postDAO;
    private UserDAO userDAO;
    private PostSource source;
    private sentimentAnalysis analyzer;
    private int savedCount = 0;
    private Integer userId;

    public streamConsumer(PostSource source) 
    {
        this(source, null); 
    }
    
    public streamConsumer(PostSource source, Integer userId) 
    {
        this.source = source;
        this.userId = userId;
        this.postDAO = new PostDAO();
        this.userDAO = new UserDAO();
        this.analyzer = new sentimentAnalysis();
        System.out.println("Consumer created, userId: " + userId + (userId != null ? " (PRIVATE MODE)" : " (PUBLIC/GUEST MODE)"));
    }

    @Override
    public void run() 
    {
        System.out.println("CONSUMER STARTED for user: " + userId);
        int privatePostCount = 0;
        int publicPostCount = 0;
        
        while (running) 
        {
            try 
            {
                System.out.println("Waiting for post from queue");
                Post post = source.getPostQueue().take();
                
                Integer effectiveUserId = null;
                
                if (this.userId != null) 
                {
                    effectiveUserId = this.userId;
                    System.out.println("Using consumer userId: " + effectiveUserId + " (PRIVATE MODE)");
                } 
                else if (post.getUserId() != null) 
                {
                    effectiveUserId = post.getUserId();
                    System.out.println("Using post userId: " + effectiveUserId);
                }
                
                System.out.println("Got post: " + post.getContent().substring(0, Math.min(30, post.getContent().length())));
                System.out.println("Post details - Keyword: " + post.getKeyword() + ", Post.userId: " + post.getUserId() + ", Effective userId: " + effectiveUserId);
                AISentimentClient.SentimentResult result = AISentimentClient.analyze(post.getContent());
                String sentiment;
                if (result != null && result.isSuccess()) 
                {
                    sentiment = result.getSentiment();
                    System.out.println("AI Sentiment: " + sentiment + " (confidence: " + result.getConfidence() + ")");
                } 
                else 
                {
                    sentiment = analyzer.analyzeSentimentLexicon(post.getContent());
                    System.out.println("Using lexicon as AI failed");
                }
                int score = 0;
                if (result != null && result.isSuccess()) 
                {
                    score = result.getScore();
                } 
                else 
                {
                    score = analyzer.calcLexicon(post.getContent());
                }

                boolean saved = postDAO.createPost(
                    post.getKeyword(), 
                    post.getContent(), 
                    sentiment, 
                    score,
                    effectiveUserId );
                
                if (saved) 
                {
                    savedCount++;
                    if (effectiveUserId != null) 
                    {
                        privatePostCount++;
                        System.out.println("PRIVATE post saved for user " + effectiveUserId + ". Private total: " + privatePostCount);
                    } 
                    else 
                    {
                        publicPostCount++;
                        System.out.println("PUBLIC post saved. Public total: " + publicPostCount);
                    }
                    
                    System.out.println("Total saved overall: " + savedCount);
                    
                    if (effectiveUserId != null) 
                    {
                        boolean keywordSaved = userDAO.addUserKeyword(effectiveUserId, post.getKeyword());
                        if (keywordSaved) 
                        {
                            System.out.println("Keyword '" + post.getKeyword() + "' saved for user ID: " + effectiveUserId);
                        }
                    }
                } 
                else 
                {
                    System.err.println("Failed to save post!");
                }
                
            } catch (InterruptedException e) {
                System.out.println("Consumer interrupted");
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                System.err.println("Consumer error: " + e.getMessage());
                e.printStackTrace();
            }
        }
        System.out.println("FINAL STATS - Private posts: " + privatePostCount + ", Public posts: " + publicPostCount + ", Total saved: " + savedCount);
        System.out.println("Consumer stopped");
    }
    public void stop() 
    {
        running = false;
    }
}