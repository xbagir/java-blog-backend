package com.blog.dto;

import java.util.List;

public record FeedResponse(
        List<PostResponse> posts,
        boolean hasPrev,
        boolean hasNext,
        int lastPage) {
}
