package com.blog.service;

import com.blog.config.DataConfig;
import com.blog.dto.CommentRequest;
import com.blog.dto.CommentResponse;
import com.blog.dto.CommentUpdateRequest;
import com.blog.exception.ResourceNotFoundException;
import com.blog.repository.PostFeedRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(SpringExtension.class)
@SpringJUnitConfig(classes = {DataConfig.class, PostServiceImpl.class, CommentServiceImpl.class,
        ImageServiceImpl.class, PostFeedRepository.class})
@TestPropertySource(locations = "classpath:test-application.properties")
@Sql(scripts = {"classpath:schema-h2.sql", "classpath:data-h2.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
class CommentServiceIntegrationTest {

    @Autowired
    private CommentService commentService;

    @Autowired
    private PostService postService;

    @Test
    @Transactional
    void addCommentIncrementsCommentsCount() {
        CommentResponse comment = commentService.addComment(1L, new CommentRequest("Новый комментарий", 1L));

        assertThat(comment.id()).isNotNull();
        assertThat(commentService.getComments(1L)).hasSize(2);
        assertThat(postService.getPost(1L).commentsCount()).isEqualTo(2);
    }

    @Test
    @Transactional
    void commentCrudRoundTrip() {
        CommentResponse added = commentService.addComment(1L, new CommentRequest("Первый", 1L));

        CommentResponse updated = commentService.updateComment(1L, added.id(), new CommentUpdateRequest(added.id(), "Изменённый", 1L));
        assertThat(updated.text()).isEqualTo("Изменённый");

        assertThat(commentService.getComment(1L, added.id()).text()).isEqualTo("Изменённый");

        commentService.deleteComment(1L, added.id());
        assertThatThrownBy(() -> commentService.getComment(1L, added.id())).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getCommentOfAnotherPostThrowsNotFound() {
        assertThatThrownBy(() -> commentService.getComment(1L, 2L)).isInstanceOf(ResourceNotFoundException.class);
    }
}
