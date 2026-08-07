package com.blog.service;

import com.blog.dto.CommentRequest;
import com.blog.dto.CommentResponse;
import com.blog.dto.CommentUpdateRequest;

import java.util.List;

public interface CommentService {

    List<CommentResponse> getComments(long postId);

    CommentResponse getComment(long postId, long commentId);

    CommentResponse addComment(long postId, CommentRequest request);

    CommentResponse updateComment(long postId, long commentId, CommentUpdateRequest request);

    void deleteComment(long postId, long commentId);
}
