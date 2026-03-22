package com.jainam.sentiment.analysis;

/**
 *
 * @author Jainam Mehta
 */


import java.util.*;

public class sentimentAnalysis 
{
    private static final Set<String> pos_word = new HashSet<>(Arrays.asList(
        "great", "awesome", "love", "amazing", "excellent", "fantastic", 
        "happy", "best", "perfect", "wonderful", "good", "superb",
        "incredible", "impressed", "exceeded", "recommend", "marvelous"
    ));
    
    private static final Set<String> neg_word = new HashSet<>(Arrays.asList(
        "bad", "terrible", "hate", "awful", "worst", "disappointed",
        "poor", "horrible", "useless", "waste", "pathetic", "crashing",
        "stopped", "unhappy", "disgusting"
    ));

    public String analyzeSentiment(String text) 
    {
        int score = calcLexicon(text);
        if (score > 0) return "positive";
        else if (score < 0) return "negative";
        else return "neutral";
    }

    public int calcLexicon(String text) 
    {
        if (text == null) return 0;
        String[] words = text.toLowerCase().split("\\W+");
        int score = 0;
        for (String word : words) 
        {
            if (pos_word.contains(word)) score++;
            else if (neg_word.contains(word)) score--;
        }
        return Integer.compare(score, 0);
    }
    public String analyzeSentimentLexicon(String text) 
    {
        return analyzeSentiment(text);
    }
}