/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.jainam.sentiment.stream;

/**
 *
 * @author Jainam Mehta
 */

import com.jainam.sentiment.model.Post;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class UploadQueue 
{
    private static final BlockingQueue<Post> queue = new LinkedBlockingQueue<>(1000);

    public static BlockingQueue<Post> getQueue() 
    {
        return queue;
    }
}