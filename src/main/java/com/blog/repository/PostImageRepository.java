package com.blog.repository;

import com.blog.dto.PostImage;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class PostImageRepository {

    private static final String UPDATE_IMAGE_SQL = """
            UPDATE posts SET image = :image, image_content_type = :contentType WHERE id = :postId
            """;

    private static final String SELECT_IMAGE_SQL = """
            SELECT image, image_content_type FROM posts WHERE id = :postId
            """;

    private final NamedParameterJdbcOperations jdbcOperations;

    public PostImageRepository(NamedParameterJdbcOperations jdbcOperations) {
        this.jdbcOperations = jdbcOperations;
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
}
