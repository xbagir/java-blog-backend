package com.blog.service;

import com.blog.config.DataConfig;
import com.blog.dto.FeedResponse;
import com.blog.dto.PostRequest;
import com.blog.dto.PostResponse;
import com.blog.dto.PostUpdateRequest;
import com.blog.exception.ResourceNotFoundException;
import com.blog.repository.PostFeedRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(SpringExtension.class)
@SpringJUnitConfig(classes = {DataConfig.class, PostServiceImpl.class, CommentServiceImpl.class,
        ImageServiceImpl.class, PostFeedRepository.class})
@TestPropertySource(locations = "classpath:test-application.properties")
@Sql(scripts = {"classpath:schema-h2.sql", "classpath:data-h2.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
class PostServiceIntegrationTest {

    @Autowired
    private PostService postService;

    @Autowired
    private CommentService commentService;

    @Test
    void feedReturnsAllPostsOnSinglePage() {
        FeedResponse feed = postService.getFeed("", 1, 5);

        assertThat(feed.posts()).hasSize(4);
        assertThat(feed.lastPage()).isEqualTo(1);
        assertThat(feed.hasPrev()).isFalse();
        assertThat(feed.hasNext()).isFalse();
    }

    @Test
    void paginationAcrossMultiplePages() {
        FeedResponse firstPage = postService.getFeed("", 1, 3);
        assertThat(firstPage.posts()).hasSize(3);
        assertThat(firstPage.lastPage()).isEqualTo(2);
        assertThat(firstPage.hasPrev()).isFalse();
        assertThat(firstPage.hasNext()).isTrue();

        FeedResponse secondPage = postService.getFeed("", 2, 3);
        assertThat(secondPage.posts()).hasSize(1);
        assertThat(secondPage.lastPage()).isEqualTo(2);
        assertThat(secondPage.hasPrev()).isTrue();
        assertThat(secondPage.hasNext()).isFalse();
    }

    @Test
    void pageBeyondLastReturnsEmptyPosts() {
        FeedResponse feed = postService.getFeed("", 5, 3);

        assertThat(feed.posts()).isEmpty();
        assertThat(feed.lastPage()).isEqualTo(2);
        assertThat(feed.hasPrev()).isTrue();
        assertThat(feed.hasNext()).isFalse();
    }

    @Test
    void searchFiltersPostsCaseInsensitively() {
        FeedResponse feed = postService.getFeed("Lalala", 1, 5);

        assertThat(feed.posts()).hasSize(1);
        assertThat(feed.posts().get(0).id()).isEqualTo(2);
    }

    @Test
    void searchFiltersPostsByTag() {
        FeedResponse feed = postService.getFeed("tag3", 1, 5);

        assertThat(feed.posts()).hasSize(1);
        assertThat(feed.posts().get(0).id()).isEqualTo(3);
        assertThat(feed.posts().get(0).tags()).containsExactly("tag3");
    }

    @Test
    void searchByTagRespectsPagination() {
        FeedResponse feed = postService.getFeed("tag", 1, 1);

        assertThat(feed.posts()).hasSize(1);
        assertThat(feed.posts().get(0).id()).isEqualTo(3);
        assertThat(feed.lastPage()).isEqualTo(2);
        assertThat(feed.hasNext()).isTrue();
    }

    @Test
    void longTextIsTruncatedWithEllipsis() {
        FeedResponse feed = postService.getFeed("", 1, 5);

        PostResponse longPost = feed.posts().stream()
                .filter(post -> post.id() == 3)
                .findFirst()
                .orElseThrow();

        assertThat(longPost.text()).endsWith("\u2026");
        assertThat(longPost.text().length()).isEqualTo(129);
    }

    @Test
    void getPostReturnsFullTextWithoutTruncation() {
        PostResponse post = postService.getPost(3L);

        assertThat(post.text().length()).isGreaterThan(128);
        assertThat(post.text()).doesNotEndWith("\u2026");
    }

    @Test
    void getMissingPostThrowsNotFound() {
        assertThatThrownBy(() -> postService.getPost(999L)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @Transactional
    void createPostPersistsAndIsReadable() {
        PostResponse created = postService.createPost(new PostRequest("Новый пост", "Текст нового поста", List.of("newtag")));

        assertThat(created.id()).isNotNull();
        assertThat(created.likesCount()).isZero();
        assertThat(created.commentsCount()).isZero();

        PostResponse found = postService.getPost(created.id());
        assertThat(found.title()).isEqualTo("Новый пост");
        assertThat(found.tags()).containsExactly("newtag");
    }

    @Test
    @Transactional
    void updatePostChangesTitleTextAndTags() {
        PostResponse updated = postService.updatePost(1L, new PostUpdateRequest(1L, "Новый заголовок", "Новый текст", List.of("x")));

        assertThat(updated.title()).isEqualTo("Новый заголовок");
        assertThat(updated.tags()).containsExactly("x");
        assertThat(postService.getPost(1L).text()).isEqualTo("Новый текст");
    }

    @Test
    @Transactional
    void deletePostRemovesPostAndItsComments() {
        postService.deletePost(1L);

        assertThatThrownBy(() -> postService.getPost(1L)).isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> commentService.getComments(1L)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @Transactional
    void likePostIncrementsCount() {
        long before = postService.getPost(1L).likesCount();
        long after = postService.likePost(1L);
        assertThat(after).isEqualTo(before + 1);
    }
}
