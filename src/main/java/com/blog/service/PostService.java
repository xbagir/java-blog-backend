package com.blog.service;

import com.blog.dto.FeedResponse;
import com.blog.dto.PostRequest;
import com.blog.dto.PostResponse;
import com.blog.dto.PostUpdateRequest;

public interface PostService {

    FeedResponse getFeed(String search, int pageNumber, int pageSize);

    PostResponse getPost(long id);

    PostResponse createPost(PostRequest request);

    PostResponse updatePost(long id, PostUpdateRequest request);

    void deletePost(long id);

    long likePost(long id);
}
