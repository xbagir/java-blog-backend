package com.blog.service;

import com.blog.exception.ResourceNotFoundException;
import com.blog.repository.PostRepository;

public abstract class AbstractPostService {

    protected final PostRepository postRepository;

    protected AbstractPostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    protected void requirePostExists(long id) {
        if (!postRepository.existsById(id)) {
            throw new ResourceNotFoundException("Post not found: " + id);
        }
    }

    protected void requireText(String field, String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
    }
}
