package com.blog.controller;

import com.blog.config.WebConfig;
import com.blog.service.CommentService;
import com.blog.service.ImageService;
import com.blog.service.PostService;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(WebConfig.class)
public class MvcTestConfig {

    @Bean
    public PostService postService() {
        return Mockito.mock(PostService.class);
    }

    @Bean
    public CommentService commentService() {
        return Mockito.mock(CommentService.class);
    }

    @Bean
    public ImageService imageService() {
        return Mockito.mock(ImageService.class);
    }

    @Bean
    public PostController postController(PostService postService,
                                         CommentService commentService,
                                         ImageService imageService) {
        return new PostController(postService, commentService, imageService);
    }

    @Bean
    public GlobalExceptionHandler globalExceptionHandler() {
        return new GlobalExceptionHandler();
    }
}
