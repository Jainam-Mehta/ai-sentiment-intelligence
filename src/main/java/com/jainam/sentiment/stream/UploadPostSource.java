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

public class UploadPostSource implements PostSource 
{
    @Override
    public BlockingQueue<Post> getPostQueue() 
    {
        return UploadQueue.getQueue();
    }

    @Override
    public void stop() {}
}