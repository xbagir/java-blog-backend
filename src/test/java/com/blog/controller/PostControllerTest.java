package com.blog.controller;

import com.blog.dto.CommentRequest;
import com.blog.dto.CommentResponse;
import com.blog.dto.CommentUpdateRequest;
import com.blog.dto.FeedResponse;
import com.blog.dto.PostImage;
import com.blog.dto.PostRequest;
import com.blog.dto.PostResponse;
import com.blog.exception.ResourceNotFoundException;
import com.blog.service.CommentService;
import com.blog.service.ImageService;
import com.blog.service.PostService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringJUnitWebConfig(MvcTestConfig.class)
class PostControllerTest {

    @Autowired
    private PostService postService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private ImageService imageService;

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void getPostsReturnsFeedJson() throws Exception {
        PostResponse post = new PostResponse(1, "Название поста 1", "Текст поста", List.of("tag_1", "tag_2"), 5, 1);
        FeedResponse feed = new FeedResponse(List.of(post), true, false, 3);

        when(postService.getFeed("Lalala", 1, 5)).thenReturn(feed);

        mockMvc.perform(get("/api/posts")
                        .param("search", "Lalala")
                        .param("pageNumber", "1")
                        .param("pageSize", "5"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.posts[0].id").value(1))
                .andExpect(jsonPath("$.posts[0].title").value("Название поста 1"))
                .andExpect(jsonPath("$.posts[0].text").value("Текст поста"))
                .andExpect(jsonPath("$.posts[0].tags[0]").value("tag_1"))
                .andExpect(jsonPath("$.posts[0].tags[1]").value("tag_2"))
                .andExpect(jsonPath("$.posts[0].likesCount").value(5))
                .andExpect(jsonPath("$.posts[0].commentsCount").value(1))
                .andExpect(jsonPath("$.hasPrev").value(true))
                .andExpect(jsonPath("$.hasNext").value(false))
                .andExpect(jsonPath("$.lastPage").value(3));
    }

    @Test
    void missingSearchParamReturnsBadRequestJson() throws Exception {
        mockMvc.perform(get("/api/posts")
                        .param("pageNumber", "1")
                        .param("pageSize", "5"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value(containsString("search")));
    }

    @Test
    void nonNumericPageNumberReturnsBadRequestJson() throws Exception {
        mockMvc.perform(get("/api/posts")
                        .param("search", "")
                        .param("pageNumber", "abc")
                        .param("pageSize", "5"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value(containsString("pageNumber")));
    }

    @Test
    void invalidPageNumberReturnsBadRequestJson() throws Exception {
        when(postService.getFeed("", 0, 5))
                .thenThrow(new IllegalArgumentException("pageNumber must be >= 1"));

        mockMvc.perform(get("/api/posts")
                        .param("search", "")
                        .param("pageNumber", "0")
                        .param("pageSize", "5"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("pageNumber must be >= 1"));
    }

    @Test
    void getPostReturnsJson() throws Exception {
        when(postService.getPost(1L)).thenReturn(new PostResponse(1L, "Название поста 1", "Текст поста", List.of("tag_1", "tag_2"), 5, 1));

        mockMvc.perform(get("/api/posts/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Название поста 1"))
                .andExpect(jsonPath("$.tags[0]").value("tag_1"))
                .andExpect(jsonPath("$.tags[1]").value("tag_2"))
                .andExpect(jsonPath("$.likesCount").value(5))
                .andExpect(jsonPath("$.commentsCount").value(1));
    }

    @Test
    void getPostNotFoundReturns404Json() throws Exception {
        when(postService.getPost(99L)).thenThrow(new ResourceNotFoundException("Post not found: 99"));

        mockMvc.perform(get("/api/posts/99"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("404"))
                .andExpect(jsonPath("$.error").value("Post not found: 99"));
    }

    @Test
    void unknownPathReturns404Json() throws Exception {
        MockMvc strictMvc = MockMvcBuilders.webAppContextSetup(context)
                .addDispatcherServletCustomizer(
                        dispatcherServlet -> dispatcherServlet.setThrowExceptionIfNoHandlerFound(true))
                .build();

        strictMvc.perform(get("/api/nonexistent"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("404"))
                .andExpect(jsonPath("$.error").value(containsString("No handler found")));
    }

    @Test
    void createPostAcceptsJsonAndReturnsPost() throws Exception {
        when(postService.createPost(any(PostRequest.class)))
                .thenReturn(new PostResponse(3L, "Название поста 3", "Текст поста 3", List.of("tag_1", "tag_2"), 0, 0));

        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Название поста 3","text":"Текст поста 3","tags":["tag_1","tag_2"]}
                                """))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.likesCount").value(0))
                .andExpect(jsonPath("$.commentsCount").value(0));
    }

    @Test
    void createPostWithBlankTitleReturns400Json() throws Exception {
        when(postService.createPost(any(PostRequest.class)))
                .thenThrow(new IllegalArgumentException("title must not be blank"));

        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":" ","text":"Текст","tags":[]}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("title must not be blank"));
    }

    @Test
    void updatePostAcceptsJsonAndReturnsPost() throws Exception {
        when(postService.updatePost(eq(3L), any(com.blog.dto.PostUpdateRequest.class)))
                .thenReturn(new PostResponse(3L, "Название поста 3", "Новый текст", List.of("tag_1"), 0, 0));

        mockMvc.perform(put("/api/posts/3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"id":3,"title":"Название поста 3","text":"Новый текст","tags":["tag_1"]}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Название поста 3"))
                .andExpect(jsonPath("$.text").value("Новый текст"));
    }

    @Test
    void deletePostReturnsOk() throws Exception {
        mockMvc.perform(delete("/api/posts/1"))
                .andExpect(status().isOk());
        verify(postService).deletePost(1L);
    }

    @Test
    void likePostReturnsNewCount() throws Exception {
        when(postService.likePost(1L)).thenReturn(6L);

        mockMvc.perform(post("/api/posts/1/likes"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").value(6));
    }

    @Test
    void updateImageAcceptsMultipartAndReturnsOk() throws Exception {
        MockMultipartFile image = new MockMultipartFile("image", "image.jpg", "image/jpeg", new byte[]{1, 2, 3});

        mockMvc.perform(multipart(HttpMethod.PUT, "/api/posts/1/image").file(image))
                .andExpect(status().isOk());

        verify(imageService).updateImage(eq(1L), eq(new byte[]{1, 2, 3}), eq("image/jpeg"));
    }

    @Test
    void getImageReturnsBytesWithContentType() throws Exception {
        when(imageService.getImage(1L)).thenReturn(new PostImage(new byte[]{1, 2, 3}, "image/png"));

        mockMvc.perform(get("/api/posts/1/image"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/png"))
                .andExpect(content().bytes(new byte[]{1, 2, 3}));
    }

    @Test
    void getCommentsReturnsJsonArray() throws Exception {
        when(commentService.getComments(1L)).thenReturn(List.of(
                new CommentResponse(1L, "Комментарий к посту 1", 1L),
                new CommentResponse(2L, "Ещё один комментарий к посту 1", 1L)));

        mockMvc.perform(get("/api/posts/1/comments"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].text").value("Комментарий к посту 1"))
                .andExpect(jsonPath("$[0].postId").value(1))
                .andExpect(jsonPath("$[1].id").value(2));
    }

    @Test
    void getCommentReturnsJson() throws Exception {
        when(commentService.getComment(1L, 2L)).thenReturn(new CommentResponse(2L, "Ещё один комментарий к посту 1", 1L));

        mockMvc.perform(get("/api/posts/1/comments/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.postId").value(1));
    }

    @Test
    void addCommentAcceptsJsonAndReturnsComment() throws Exception {
        when(commentService.addComment(eq(1L), any(CommentRequest.class)))
                .thenReturn(new CommentResponse(2L, "Комментарий к посту", 1L));

        mockMvc.perform(post("/api/posts/1/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"text":"Комментарий к посту","postId":1}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.text").value("Комментарий к посту"))
                .andExpect(jsonPath("$.postId").value(1));
    }

    @Test
    void updateCommentReturnsJson() throws Exception {
        when(commentService.updateComment(eq(1L), eq(2L), any(CommentUpdateRequest.class)))
                .thenReturn(new CommentResponse(2L, "Второй комментарий к посту 1", 1L));

        mockMvc.perform(put("/api/posts/1/comments/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"id":2,"text":"Второй комментарий к посту 1","postId":1}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.text").value("Второй комментарий к посту 1"));
    }

    @Test
    void deleteCommentReturnsOk() throws Exception {
        mockMvc.perform(delete("/api/posts/1/comments/2"))
                .andExpect(status().isOk());
        verify(commentService).deleteComment(1L, 2L);
    }
}
