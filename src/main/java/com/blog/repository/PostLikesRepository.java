package com.blog.repository;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
public class PostLikesRepository {

    private static final String INCREMENT_LIKES_SQL = """
            UPDATE posts SET likes_count = likes_count + 1 WHERE id = :postId
            """;

    private static final String SELECT_LIKES_SQL = "SELECT likes_count FROM posts WHERE id = :postId";

    private final NamedParameterJdbcOperations jdbcOperations;

    public PostLikesRepository(NamedParameterJdbcOperations jdbcOperations) {
        this.jdbcOperations = jdbcOperations;
    }

    public long incrementLikes(long postId) {
        jdbcOperations.update(INCREMENT_LIKES_SQL, Map.of("postId", postId));
        Long count = jdbcOperations.queryForObject(SELECT_LIKES_SQL, Map.of("postId", postId), Long.class);
        return count == null ? 0L : count;
    }
}
