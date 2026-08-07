package com.blog.repository;

import com.blog.config.DataConfig;
import com.blog.model.Post;
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
class PostRepositoryIntegrationTest {

    @Autowired
    private PostRepository postRepository;

    @Test
    void countReturnsSeededPosts() {
        assertThat(postRepository.count()).isEqualTo(4);
    }

    @Test
    @Transactional
    void savePersistsNewPostWithGeneratedId() {
        Post post = Post.builder().title("Новый пост").text("Текст нового поста").build();

        Post saved = postRepository.save(post);

        assertThat(saved.getId()).isNotNull();
        assertThat(postRepository.findById(saved.getId())).isPresent()
                .get()
                .extracting(Post::getTitle, Post::getText)
                .containsExactly("Новый пост", "Текст нового поста");
    }

    @Test
    @Transactional
    void saveAndReadBackImage() {
        Post post = Post.builder()
                .title("С картинкой")
                .text("Текст")
                .image(new byte[]{1, 2, 3})
                .imageContentType("image/png")
                .build();

        Post saved = postRepository.save(post);

        assertThat(postRepository.findById(saved.getId()))
                .get()
                .extracting(Post::getImage, Post::getImageContentType)
                .containsExactly(new byte[]{1, 2, 3}, "image/png");
    }
}
