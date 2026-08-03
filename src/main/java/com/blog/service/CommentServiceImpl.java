package com.blog.service;

import com.blog.dto.CommentRequest;
import com.blog.dto.CommentResponse;
import com.blog.dto.CommentUpdateRequest;
import com.blog.exception.ResourceNotFoundException;
import com.blog.model.Comment;
import com.blog.repository.CommentRepository;
import com.blog.repository.PostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CommentServiceImpl extends AbstractPostService implements CommentService {

    private final CommentRepository commentRepository;

    public CommentServiceImpl(PostRepository postRepository, CommentRepository commentRepository) {
        super(postRepository);
        this.commentRepository = commentRepository;
    }

    @Override
    public List<CommentResponse> getComments(long postId) {
        requirePostExists(postId);
        return commentRepository.findByPostIdOrderByIdAsc(postId)
                .stream()
                .map(this::toCommentResponse)
                .toList();
    }

    @Override
    public CommentResponse getComment(long postId, long commentId) {
        Comment comment = findComment(postId, commentId);
        return toCommentResponse(comment);
    }

    @Override
    @Transactional
    public CommentResponse addComment(long postId, CommentRequest request) {
        requireMatchingPostId(postId, request.postId());
        requirePostExists(postId);
        requireText("text", request.text());

        Comment saved = commentRepository.save(Comment.builder().postId(postId).text(request.text()).build());
        return toCommentResponse(saved);
    }

    @Override
    @Transactional
    public CommentResponse updateComment(long postId, long commentId, CommentUpdateRequest request) {
        requireMatchingPostId(postId, request.postId());
        if (request.id() != commentId) {
            throw new IllegalArgumentException("id in body does not match the path");
        }
        requireText("text", request.text());

        Comment comment = findComment(postId, commentId);
        comment.setText(request.text());
        return toCommentResponse(commentRepository.save(comment));
    }

    @Override
    @Transactional
    public void deleteComment(long postId, long commentId) {
        Comment comment = findComment(postId, commentId);
        commentRepository.deleteById(comment.getId());
    }

    private Comment findComment(long postId, long commentId) {
        return commentRepository.findById(commentId)
                .filter(comment -> comment.getPostId() == postId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found: " + commentId));
    }

    private void requireMatchingPostId(long pathPostId, Long bodyPostId) {
        if (!Long.valueOf(pathPostId).equals(bodyPostId)) {
            throw new IllegalArgumentException("postId in body does not match the path");
        }
    }

    private CommentResponse toCommentResponse(Comment comment) {
        return new CommentResponse(comment.getId(), comment.getText(), comment.getPostId());
    }
}
