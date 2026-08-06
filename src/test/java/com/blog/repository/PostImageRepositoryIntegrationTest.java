package com.blog.repository;

import com.blog.config.DataConfig;
import com.blog.dto.PostImage;
import com.blog.service.CommentServiceImpl;
import com.blog.service.ImageServiceImpl;
import com.blog.service.PostServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringJUnitConfig(classes = {DataConfig.class, PostServiceImpl.class, CommentServiceImpl.class,
        ImageServiceImpl.class, PostFeedRepository.class, PostTagRepository.class,
        PostLikesRepository.class, PostImageRepository.class})
@TestPropertySource(locations = "classpath:test-application.properties")
@Sql(scripts = {"classpath:schema-h2.sql", "classpath:data-h2.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
class PostImageRepositoryIntegrationTest {

    @Autowired
    private PostImageRepository postImageRepository;

    @Test
    @Transactional
    void updateImageOverwritesPreviousValue() {
        postImageRepository.updateImage(1L, new byte[]{1}, "image/png");
        postImageRepository.updateImage(1L, new byte[]{2}, "image/jpeg");

        PostImage image = postImageRepository.findImage(1L);
        assertThat(image.data()).containsExactly(2);
        assertThat(image.contentType()).isEqualTo("image/jpeg");
    }
}
