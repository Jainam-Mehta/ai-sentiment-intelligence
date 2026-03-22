package com.jainam.sentiment.stream;

import com.jainam.sentiment.analysis.sentimentAnalysis;
import com.jainam.sentiment.model.Post;
import com.jainam.sentiment.dao.PostDAO; 
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class streamGenerator implements Runnable, PostSource 
{
    private volatile boolean running = true;
    private String keyword;
    private Random random;
    private PostDAO postDAO;
    private sentimentAnalysis analyzer;
    private BlockingQueue<Post> postQueue;
    
    private static final String[] pos_temp = 
    {
        "I absolutely love my new %s! It's amazing!",
        "The %s is fantastic, best purchase ever!",
        "So happy with the %s, it exceeds expectations!",
        "This %s is incredible, highly recommend!",
        "Wow! The %s works perfectly, I'm impressed!",
        "I would definitely purchase %s!",
        "%s would be mine alone :D"
    };
    
    private static final String[] neg_temp = {
        "I hate this %s, it's terrible quality.",
        "The %s keeps crashing, very disappointed.",
        "Worst product ever, this %s is a complete waste.",
        "Very unhappy with the %s, don't buy it.",
        "The %s stopped working after one day!",
        "Terrifc and annoying &s",
        "No one would give a single penny for this %s!"
    };
    
    private static final String[] neu_temp = {
        "The %s is okay, nothing special.",
        "It's an average %s, works as expected.",
        "Not bad, not great, just a regular %s.",
        "The %s does what it's supposed to do.",
        "Decent %s for the price, nothing amazing.",
        "I would say ok-ok for %s.",
        "It's your choice for %s, i am neutral."
    };

    public streamGenerator(String keyword) 
    {
        this.keyword = keyword;
        this.analyzer = new sentimentAnalysis();
        this.postDAO = new PostDAO();
        this.postQueue = new LinkedBlockingQueue<>(100);
        this.random = new Random();
    }
    
    @Override
    public void run() 
    {
        System.out.println("SIMULATED Generator started for: " + keyword);
        int postCount = 0;
        
        while (running) 
        {
            try 
            {
                postCount++;
                System.out.println("Generating post #" + postCount);
                Post post = generatePost();
                System.out.println("Created: " + post.getContent());
                System.out.println("Sentiment: " + post.getSentiment());
                postQueue.put(post);
                System.out.println("Queue size: " + postQueue.size());
                
                Thread.sleep(1000 + random.nextInt(3000));
            } catch (InterruptedException e) {
                System.err.println("Generator interrupted");
                Thread.currentThread().interrupt();
                break;
            }
        }
        System.out.println("Generator stopped. Total posts: " + postCount);
    }
    
    private Post generatePost() 
    {
        int sent_type = random.nextInt(10);
        String content;
        String sentiment;
        int score;
        
        if (sent_type < 4) 
        {
            sentiment = "positive";
            score = 1;
            content = String.format(pos_temp[random.nextInt(pos_temp.length)], keyword);
        } 
        else if (sent_type < 8) 
        {
            sentiment = "neutral";
            score = 0;
            content = String.format(neu_temp[random.nextInt(neu_temp.length)], keyword);
        } 
        else 
        {
            sentiment = "negative";
            score = -1;
            content = String.format(neg_temp[random.nextInt(neg_temp.length)], keyword);
        }
        return new Post(keyword, content, sentiment, score);
    }
    
    @Override
    public BlockingQueue<Post> getPostQueue() 
    {
        return postQueue;
    }
    
    @Override
    public void stop() 
    {
        running = false;
    }
}