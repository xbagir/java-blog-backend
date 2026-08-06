package com.blog.repository;

import com.blog.dto.PostImage;
import com.blog.dto.PostResponse;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
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

    private static final String SELECT_TAGS_SQL = """
            SELECT post_id, tag
            FROM post_tags
            WHERE post_id IN (:ids)
            ORDER BY post_id, tag
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

    private static final String SELECT_TAGS_BY_POST_SQL = """
            SELECT tag
            FROM post_tags
            WHERE post_id = :postId
            ORDER BY tag
            """;

    private static final String DELETE_TAGS_SQL = "DELETE FROM post_tags WHERE post_id = :postId";

    private static final String INSERT_TAG_SQL = "INSERT INTO post_tags (post_id, tag) VALUES (:postId, :tag)";

    private static final String COUNT_COMMENTS_SQL = "SELECT COUNT(*) FROM comments WHERE post_id = :postId";

    private static final String INCREMENT_LIKES_SQL = """
            UPDATE posts SET likes_count = likes_count + 1 WHERE id = :postId
            """;

    private static final String SELECT_LIKES_SQL = "SELECT likes_count FROM posts WHERE id = :postId";

    private static final String UPDATE_IMAGE_SQL = """
            UPDATE posts SET image = :image, image_content_type = :contentType WHERE id = :postId
            """;

    private static final String SELECT_IMAGE_SQL = """
            SELECT image, image_content_type FROM posts WHERE id = :postId
            """;

    private final NamedParameterJdbcOperations jdbcOperations;

    public PostFeedRepository(NamedParameterJdbcOperations jdbcOperations) {
        this.jdbcOperations = jdbcOperations;
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

        Map<Long, List<String>> tagsByPostId = findTagsByPostIds(posts);
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

    public List<String> findTags(long postId) {
        return jdbcOperations.query(
                SELECT_TAGS_BY_POST_SQL,
                new MapSqlParameterSource("postId", postId),
                (rs, rowNum) -> rs.getString("tag"));
    }

    public void replaceTags(long postId, List<String> tags) {
        jdbcOperations.update(DELETE_TAGS_SQL, new MapSqlParameterSource("postId", postId));
        for (String tag : tags) {
            jdbcOperations.update(
                    INSERT_TAG_SQL,
                    new MapSqlParameterSource("postId", postId).addValue("tag", tag));
        }
    }

    public long countComments(long postId) {
        Long count = jdbcOperations.queryForObject(COUNT_COMMENTS_SQL, Map.of("postId", postId), Long.class);
        return count == null ? 0L : count;
    }

    public long incrementLikes(long postId) {
        jdbcOperations.update(INCREMENT_LIKES_SQL, Map.of("postId", postId));
        Long count = jdbcOperations.queryForObject(SELECT_LIKES_SQL, Map.of("postId", postId), Long.class);
        return count == null ? 0L : count;
    }

    public void updateImage(long postId, byte[] image, String contentType) {
        jdbcOperations.update(
                UPDATE_IMAGE_SQL,
                new MapSqlParameterSource("postId", postId)
                        .addValue("image", image)
                        .addValue("contentType", contentType));
    }

    public PostImage findImage(long postId) {
        List<PostImage> images = jdbcOperations.query(
                SELECT_IMAGE_SQL,
                Map.of("postId", postId),
                (rs, rowNum) -> new PostImage(rs.getBytes("image"), rs.getString("image_content_type")));
        return images.isEmpty() ? null : images.get(0);
    }

    private Map<Long, List<String>> findTagsByPostIds(List<PostResponse> posts) {
        List<Long> ids = posts.stream().map(PostResponse::id).toList();
        Map<Long, List<String>> tagsByPostId = new LinkedHashMap<>();
        jdbcOperations.query(
                SELECT_TAGS_SQL,
                new MapSqlParameterSource("ids", ids),
                rs -> {
                    long postId = rs.getLong("post_id");
                    tagsByPostId.computeIfAbsent(postId, key -> new ArrayList<>()).add(rs.getString("tag"));
                });
        return tagsByPostId;
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
