package com.blog.repository;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Repository
public class PostTagRepository {

    private static final String SELECT_TAGS_BY_POST_SQL = """
            SELECT tag
            FROM post_tags
            WHERE post_id = :postId
            ORDER BY tag
            """;

    private static final String SELECT_TAGS_BY_IDS_SQL = """
            SELECT post_id, tag
            FROM post_tags
            WHERE post_id IN (:ids)
            ORDER BY post_id, tag
            """;

    private static final String DELETE_TAGS_SQL = "DELETE FROM post_tags WHERE post_id = :postId";

    private static final String INSERT_TAG_SQL = "INSERT INTO post_tags (post_id, tag) VALUES (:postId, :tag)";

    private final NamedParameterJdbcOperations jdbcOperations;

    public PostTagRepository(NamedParameterJdbcOperations jdbcOperations) {
        this.jdbcOperations = jdbcOperations;
    }

    public List<String> findTags(long postId) {
        return jdbcOperations.query(
                SELECT_TAGS_BY_POST_SQL,
                new MapSqlParameterSource("postId", postId),
                (rs, rowNum) -> rs.getString("tag"));
    }

    public Map<Long, List<String>> findTagsByPostIds(List<Long> postIds) {
        Map<Long, List<String>> tagsByPostId = new LinkedHashMap<>();
        jdbcOperations.query(
                SELECT_TAGS_BY_IDS_SQL,
                new MapSqlParameterSource("ids", postIds),
                rs -> {
                    long postId = rs.getLong("post_id");
                    tagsByPostId.computeIfAbsent(postId, key -> new ArrayList<>()).add(rs.getString("tag"));
                });
        return tagsByPostId;
    }

    public void replaceTags(long postId, List<String> tags) {
        jdbcOperations.update(DELETE_TAGS_SQL, new MapSqlParameterSource("postId", postId));
        for (String tag : tags) {
            jdbcOperations.update(
                    INSERT_TAG_SQL,
                    new MapSqlParameterSource("postId", postId).addValue("tag", tag));
        }
    }
}
