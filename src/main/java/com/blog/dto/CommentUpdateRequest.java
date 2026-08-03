package com.blog.dto;

public record CommentUpdateRequest(long id, String text, Long postId) {
}
