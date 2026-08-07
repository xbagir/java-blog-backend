package com.blog.repository;

import com.blog.dto.PostResponse;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Repository
public class PostFeedRepository {

    private static final String SELECT_SQL = """
            SELECT p.id,
                   p.title,
                   p.text,
                   p.likes_count AS likes_count,
                   (SELECT COUNT(*) FROM comments c WHERE c.post_id = p.id) AS comments_count
            FROM posts p
            WHERE (:search = ''
                   OR LOWER(p.title) LIKE :pattern ESCAPE '\\'
                   OR LOWER(p.text) LIKE :pattern ESCAPE '\\'
                   OR EXISTS (SELECT 1 FROM post_tags t
                              WHERE t.post_id = p.id AND LOWER(t.tag) LIKE :pattern ESCAPE '\\'))
            ORDER BY p.created_at DESC, p.id DESC
            LIMIT :limit OFFSET :offset
            """;

    private static final String COUNT_SQL = """
            SELECT COUNT(*)
            FROM posts p
            WHERE (:search = ''
                   OR LOWER(p.title) LIKE :pattern ESCAPE '\\'
                   OR LOWER(p.text) LIKE :pattern ESCAPE '\\'
                   OR EXISTS (SELECT 1 FROM post_tags t
                              WHERE t.post_id = p.id AND LOWER(t.tag) LIKE :pattern ESCAPE '\\'))
            """;

    private final NamedParameterJdbcOperations jdbcOperations;
    private final PostTagRepository postTagRepository;

    public PostFeedRepository(NamedParameterJdbcOperations jdbcOperations,
                              PostTagRepository postTagRepository) {
        this.jdbcOperations = jdbcOperations;
        this.postTagRepository = postTagRepository;
    }

    public List<PostResponse> findPosts(String search, int limit, long offset) {
        List<PostResponse> posts = jdbcOperations.query(
                SELECT_SQL,
                searchParameters(search).addValue("limit", limit).addValue("offset", offset),
                (rs, rowNum) -> new PostResponse(
                        rs.getLong("id"),
                        rs.getString("title"),
                        rs.getString("text"),
                        null,
                        rs.getLong("likes_count"),
                        rs.getLong("comments_count")));

        if (posts.isEmpty()) {
            return posts;
        }

        List<Long> postIds = posts.stream().map(PostResponse::id).toList();
        Map<Long, List<String>> tagsByPostId = postTagRepository.findTagsByPostIds(postIds);
        return posts.stream()
                .map(post -> new PostResponse(
                        post.id(),
                        post.title(),
                        post.text(),
                        tagsByPostId.getOrDefault(post.id(), Collections.emptyList()),
                        post.likesCount(),
                        post.commentsCount()))
                .toList();
    }

    public long countPosts(String search) {
        Long count = jdbcOperations.queryForObject(COUNT_SQL, searchParameters(search), Long.class);
        return count == null ? 0L : count;
    }

    private MapSqlParameterSource searchParameters(String search) {
        String normalized = search == null ? "" : search;
        return new MapSqlParameterSource()
                .addValue("search", normalized)
                .addValue("pattern", "%" + escapeLike(normalized.toLowerCase(Locale.ROOT)) + "%");
    }

    private String escapeLike(String value) {
        return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
