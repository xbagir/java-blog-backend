package com.blog.repository;

import com.blog.config.DataConfig;
import com.blog.dto.PostResponse;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringJUnitConfig(classes = {DataConfig.class, PostServiceImpl.class, CommentServiceImpl.class,
        ImageServiceImpl.class, PostFeedRepository.class})
@TestPropertySource(locations = "classpath:test-application.properties")
@Sql(scripts = {"classpath:schema-h2.sql", "classpath:data-h2.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
class PostFeedRepositoryIntegrationTest {

    @Autowired
    private PostFeedRepository postFeedRepository;

    @Test
    void findPostsMapsTagsAndCounters() {
        List<PostResponse> posts = postFeedRepository.findPosts("", 5, 0);

        assertThat(posts).hasSize(4);
        PostResponse first = posts.get(0);
        assertThat(first.id()).isEqualTo(4);
        assertThat(first.tags()).isEmpty();

        PostResponse post1 = posts.stream().filter(p -> p.id() == 1).findFirst().orElseThrow();
        assertThat(post1.title()).isEqualTo("Пост первый");
        assertThat(post1.tags()).containsExactly("tag1", "tag2");
        assertThat(post1.likesCount()).isEqualTo(2);
        assertThat(post1.commentsCount()).isEqualTo(1);

        PostResponse post2 = posts.stream().filter(p -> p.id() == 2).findFirst().orElseThrow();
        assertThat(post2.tags()).containsExactly("lalala");
        assertThat(post2.likesCount()).isEqualTo(1);
        assertThat(post2.commentsCount()).isEqualTo(1);
    }

    @Test
    void findPostsAppliesLimitAndOffset() {
        List<PostResponse> firstPage = postFeedRepository.findPosts("", 2, 0);
        assertThat(firstPage).extracting(PostResponse::id).containsExactly(4L, 3L);

        List<PostResponse> secondPage = postFeedRepository.findPosts("", 2, 2);
        assertThat(secondPage).extracting(PostResponse::id).containsExactly(2L, 1L);
    }

    @Test
    void countPostsReturnsTotalRows() {
        assertThat(postFeedRepository.countPosts("")).isEqualTo(4);
    }

    @Test
    void searchMatchesCaseInsensitively() {
        assertThat(postFeedRepository.countPosts("lalala")).isEqualTo(1);
        assertThat(postFeedRepository.countPosts("LALALA")).isEqualTo(1);

        List<PostResponse> posts = postFeedRepository.findPosts("LALALA", 5, 0);
        assertThat(posts).extracting(PostResponse::id).containsExactly(2L);
    }

    @Test
    void searchMatchesTagsCaseInsensitively() {
        assertThat(postFeedRepository.countPosts("tag3")).isEqualTo(1);
        assertThat(postFeedRepository.countPosts("TAG3")).isEqualTo(1);

        List<PostResponse> posts = postFeedRepository.findPosts("tag3", 5, 0);
        assertThat(posts).extracting(PostResponse::id).containsExactly(3L);
        assertThat(posts.get(0).tags()).containsExactly("tag3");
    }

    @Test
    void searchMatchesTagsWithLimitAndOffset() {
        List<PostResponse> page = postFeedRepository.findPosts("tag", 1, 1);
        assertThat(page).extracting(PostResponse::id).containsExactly(1L);
    }

    @Test
    void searchEscapesLikeWildcards() {
        assertThat(postFeedRepository.countPosts("%")).isZero();
        assertThat(postFeedRepository.countPosts("_")).isZero();
        assertThat(postFeedRepository.countPosts("\\")).isZero();
        assertThat(postFeedRepository.countPosts("tag_")).isZero();
    }
}
