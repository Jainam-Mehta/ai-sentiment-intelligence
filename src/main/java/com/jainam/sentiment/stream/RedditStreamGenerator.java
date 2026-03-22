package com.jainam.sentiment.stream;

import com.jainam.sentiment.analysis.sentimentAnalysis;
import com.jainam.sentiment.model.Post;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.json.JSONArray;
import org.json.JSONObject;

public class RedditStreamGenerator implements Runnable, PostSource 
{
    private volatile boolean running = true;
    private final String subreddit;
    private final BlockingQueue<Post> postQueue;
    private final sentimentAnalysis analyzer;
    private final HttpClient httpClient;
    private final Set<String> seenIds;
    private int commentCount = 0;

    public RedditStreamGenerator(String subreddit) 
    {
        this.subreddit = subreddit;
        this.postQueue = new LinkedBlockingQueue<>(100);
        this.analyzer = new sentimentAnalysis();
        this.httpClient = HttpClient.newHttpClient();
        this.seenIds = new HashSet<>();
    }

    @Override
    public void run() 
    {
        System.out.println("REDDIT stream started for r/" + subreddit);
        
        while (running) 
        {
            try 
            {
                String url = "https://www.reddit.com/r/" + subreddit + "/comments.json?limit=25";
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("User-Agent", "Java Sentiment Dashboard/1.0")
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200) 
                {
                    JSONObject json = new JSONObject(response.body());
                    JSONArray children = json.getJSONObject("data").getJSONArray("children");

                    int newComments = 0;
                    for (int i = 0; i < children.length(); i++) 
                    {
                        JSONObject comment = children.getJSONObject(i).getJSONObject("data");
                        String id = comment.getString("id");
                        String body = comment.optString("body", "");
                        if (!seenIds.contains(id) && !body.isBlank() && body.length() > 10) 
                        {
                            seenIds.add(id);
                            commentCount++;
                            newComments++;
                            String sentiment = analyzer.analyzeSentiment(body);
                            int score = analyzer.calcLexicon(body);
                            Post post = new Post(subreddit, body, sentiment, score);
                            postQueue.put(post);
                            System.out.println("Reddit comment #" + commentCount + ": " + body.substring(0, Math.min(50, body.length())) + "...");
                        }
                    }
                    
                    if (newComments > 0) 
                    {
                        System.out.println("Added " + newComments + " new comments. Queue size: " + postQueue.size());
                    }
                } 
                else 
                {
                    System.err.println("Reddit API error: " + response.statusCode());
                }
                Thread.sleep(3000);

            } catch (InterruptedException e) {
                System.out.println("Reddit stream interrupted");
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                System.err.println("Error in Reddit stream: " + e.getMessage());
                e.printStackTrace();
                try 
                { 
                    Thread.sleep(5000); 
                } catch (InterruptedException ie) { 
                    break; 
                }
            }
        }
        System.out.println("Reddit stream stopped. Total comments: " + commentCount);
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