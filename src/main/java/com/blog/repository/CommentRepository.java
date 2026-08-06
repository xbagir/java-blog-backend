package com.blog.repository;

import com.blog.model.Comment;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CommentRepository extends CrudRepository<Comment, Long> {

    List<Comment> findByPostIdOrderByIdAsc(Long postId);

    long countByPostId(Long postId);
}
