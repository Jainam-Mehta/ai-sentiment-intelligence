/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.jainam.sentiment.stream;

import com.jainam.sentiment.model.Post;
import java.util.concurrent.BlockingQueue;

public interface PostSource 
{
    BlockingQueue<Post> getPostQueue();
    void stop();
}