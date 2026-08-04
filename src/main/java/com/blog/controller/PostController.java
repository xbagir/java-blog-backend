package com.blog.controller;

import com.blog.dto.CommentRequest;
import com.blog.dto.CommentResponse;
import com.blog.dto.CommentUpdateRequest;
import com.blog.dto.FeedResponse;
import com.blog.dto.PostImage;
import com.blog.dto.PostRequest;
import com.blog.dto.PostResponse;
import com.blog.dto.PostUpdateRequest;
import com.blog.service.CommentService;
import com.blog.service.ImageService;
import com.blog.service.PostService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;
    private final CommentService commentService;
    private final ImageService imageService;

    public PostController(PostService postService,
                          CommentService commentService,
                          ImageService imageService) {
        this.postService = postService;
        this.commentService = commentService;
        this.imageService = imageService;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public FeedResponse getPosts(@RequestParam("search") String search,
                                 @RequestParam("pageNumber") int pageNumber,
                                 @RequestParam("pageSize") int pageSize) {
        return postService.getFeed(search, pageNumber, pageSize);
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public PostResponse getPost(@PathVariable("id") long id) {
        return postService.getPost(id);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public PostResponse createPost(@RequestBody PostRequest request) {
        return postService.createPost(request);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public PostResponse updatePost(@PathVariable("id") long id, @RequestBody PostUpdateRequest request) {
        return postService.updatePost(id, request);
    }

    @DeleteMapping("/{id}")
    public void deletePost(@PathVariable("id") long id) {
        postService.deletePost(id);
    }

    @PostMapping(value = "/{id}/likes", produces = MediaType.APPLICATION_JSON_VALUE)
    public long likePost(@PathVariable("id") long id) {
        return postService.likePost(id);
    }

    @PutMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void updateImage(@PathVariable("id") long id, @RequestPart("image") MultipartFile image) throws IOException {
        imageService.updateImage(id, image.getBytes(), image.getContentType());
    }

    @GetMapping("/{id}/image")
    public ResponseEntity<byte[]> getImage(@PathVariable("id") long id) {
        PostImage image = imageService.getImage(id);
        MediaType mediaType = image.contentType() == null
                ? MediaType.APPLICATION_OCTET_STREAM
                : MediaType.parseMediaType(image.contentType());
        return ResponseEntity.ok().contentType(mediaType).body(image.data());
    }

    @GetMapping(value = "/{postId}/comments", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<CommentResponse> getComments(@PathVariable("postId") long postId) {
        return commentService.getComments(postId);
    }

    @GetMapping(value = "/{postId}/comments/{commentId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CommentResponse getComment(@PathVariable("postId") long postId,
                                      @PathVariable("commentId") long commentId) {
        return commentService.getComment(postId, commentId);
    }

    @PostMapping(value = "/{postId}/comments", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public CommentResponse addComment(@PathVariable("postId") long postId, @RequestBody CommentRequest request) {
        return commentService.addComment(postId, request);
    }

    @PutMapping(value = "/{postId}/comments/{commentId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public CommentResponse updateComment(@PathVariable("postId") long postId,
                                         @PathVariable("commentId") long commentId,
                                         @RequestBody CommentUpdateRequest request) {
        return commentService.updateComment(postId, commentId, request);
    }

    @DeleteMapping("/{postId}/comments/{commentId}")
    public void deleteComment(@PathVariable("postId") long postId,
                              @PathVariable("commentId") long commentId) {
        commentService.deleteComment(postId, commentId);
    }
}
