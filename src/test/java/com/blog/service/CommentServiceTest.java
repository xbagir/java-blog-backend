package com.blog.service;

import com.blog.dto.CommentRequest;
import com.blog.dto.CommentResponse;
import com.blog.dto.CommentUpdateRequest;
import com.blog.exception.ResourceNotFoundException;
import com.blog.model.Comment;
import com.blog.repository.CommentRepository;
import com.blog.repository.PostRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private CommentServiceImpl commentService;

    @Test
    void getCommentsMapsEntities() {
        when(postRepository.existsById(1L)).thenReturn(true);
        when(commentRepository.findByPostIdOrderByIdAsc(1L))
                .thenReturn(List.of(Comment.builder().id(2L).postId(1L).text("Комментарий").build()));

        List<CommentResponse> comments = commentService.getComments(1L);

        assertThat(comments).containsExactly(new CommentResponse(2L, "Комментарий", 1L));
    }

    @Test
    void getCommentFiltersByPostId() {
        when(commentRepository.findById(2L))
                .thenReturn(Optional.of(Comment.builder().id(2L).postId(5L).text("Чужой пост").build()));

        assertThatThrownBy(() -> commentService.getComment(1L, 2L)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void addCommentPersistsAndReturns() {
        when(postRepository.existsById(1L)).thenReturn(true);
        Comment saved = Comment.builder().id(7L).postId(1L).text("Текст комментария").build();
        when(commentRepository.save(any(Comment.class))).thenReturn(saved);

        CommentResponse response = commentService.addComment(1L, new CommentRequest("Текст комментария", 1L));

        assertThat(response).isEqualTo(new CommentResponse(7L, "Текст комментария", 1L));
    }

    @Test
    void addCommentRejectsPostIdMismatch() {
        assertThatThrownBy(() -> commentService.addComment(1L, new CommentRequest("текст", 5L)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("postId");
        verify(commentRepository, never()).save(any());
    }

    @Test
    void updateCommentChangesText() {
        Comment existing = Comment.builder().id(2L).postId(1L).text("Старый").build();
        when(commentRepository.findById(2L)).thenReturn(Optional.of(existing));
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CommentResponse response = commentService.updateComment(1L, 2L, new CommentUpdateRequest(2L, "Новый", 1L));

        assertThat(response.text()).isEqualTo("Новый");
    }

    @Test
    void deleteCommentRemovesExisting() {
        when(commentRepository.findById(2L))
                .thenReturn(Optional.of(Comment.builder().id(2L).postId(1L).text("Комментарий").build()));

        commentService.deleteComment(1L, 2L);

        verify(commentRepository).deleteById(2L);
    }
}
