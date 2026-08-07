package com.blog.service;

import com.blog.config.DataConfig;
import com.blog.dto.PostImage;
import com.blog.exception.ResourceNotFoundException;
import com.blog.repository.PostFeedRepository;
import com.blog.repository.PostImageRepository;
import com.blog.repository.PostLikesRepository;
import com.blog.repository.PostTagRepository;
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
        ImageServiceImpl.class, PostFeedRepository.class, PostTagRepository.class,
        PostLikesRepository.class, PostImageRepository.class})
@TestPropertySource(locations = "classpath:test-application.properties")
@Sql(scripts = {"classpath:schema-h2.sql", "classpath:data-h2.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
class ImageServiceIntegrationTest {

    @Autowired
    private ImageService imageService;

    @Test
    @Transactional
    void updateAndReadImage() {
        imageService.updateImage(1L, new byte[]{10, 20, 30}, "image/png");

        PostImage image = imageService.getImage(1L);
        assertThat(image.data()).containsExactly(10, 20, 30);
        assertThat(image.contentType()).isEqualTo("image/png");
    }

    @Test
    void getImageWithoutUploadThrowsNotFound() {
        assertThatThrownBy(() -> imageService.getImage(4L)).isInstanceOf(ResourceNotFoundException.class);
    }
}
