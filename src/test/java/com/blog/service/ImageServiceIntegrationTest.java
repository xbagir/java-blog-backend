package com.blog.service;

import com.blog.dto.PostImage;
import com.blog.exception.ResourceNotFoundException;
import com.blog.repository.PostFeedRepository;
import com.blog.repository.PostImageRepository;
import com.blog.repository.PostLikesRepository;
import com.blog.repository.PostTagRepository;
import com.blog.service.CommentServiceImpl;
import com.blog.service.ImageServiceImpl;
import com.blog.service.PostServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJdbcTest(useDefaultFilters = false)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({PostServiceImpl.class, CommentServiceImpl.class, ImageServiceImpl.class,
        PostFeedRepository.class, PostTagRepository.class, PostLikesRepository.class, PostImageRepository.class})
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
