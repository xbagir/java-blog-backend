package com.blog.service;

import com.blog.dto.FeedResponse;
import com.blog.dto.PostRequest;
import com.blog.dto.PostResponse;
import com.blog.dto.PostUpdateRequest;
import com.blog.exception.ResourceNotFoundException;
import com.blog.model.Post;
import com.blog.repository.PostFeedRepository;
import com.blog.repository.PostRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private PostFeedRepository postFeedRepository;

    @InjectMocks
    private PostServiceImpl postService;

    @Test
    void shortTextIsNotTruncated() {
        String text = "abc".repeat(30);
        when(postFeedRepository.countPosts("")).thenReturn(1L);
        when(postFeedRepository.findPosts(anyString(), anyInt(), anyLong()))
                .thenReturn(List.of(new PostResponse(1L, "Title", text, List.of(), 0, 0)));

        FeedResponse feed = postService.getFeed("", 1, 5);

        assertThat(feed.posts()).hasSize(1);
        assertThat(feed.posts().get(0).text()).isEqualTo(text);
    }

    @Test
    void longTextIsTruncatedWithEllipsis() {
        String longText = "a".repeat(200);
        when(postFeedRepository.countPosts("")).thenReturn(1L);
        when(postFeedRepository.findPosts(anyString(), anyInt(), anyLong()))
                .thenReturn(List.of(new PostResponse(1L, "Title", longText, List.of(), 0, 0)));

        FeedResponse feed = postService.getFeed("", 1, 5);

        assertThat(feed.posts().get(0).text()).hasSize(129).endsWith("\u2026");
        assertThat(feed.posts().get(0).text().substring(0, 128)).isEqualTo("a".repeat(128));
    }

    @Test
    void textOfExactlyMaxLengthIsNotTruncated() {
        String text = "b".repeat(128);
        when(postFeedRepository.countPosts("")).thenReturn(1L);
        when(postFeedRepository.findPosts(anyString(), anyInt(), anyLong()))
                .thenReturn(List.of(new PostResponse(1L, "Title", text, List.of(), 0, 0)));

        FeedResponse feed = postService.getFeed("", 1, 5);

        assertThat(feed.posts().get(0).text()).isEqualTo(text);
    }

    @Test
    void unicodeTextIsTruncatedByCodePoints() {
        String emoji = "\uD83D\uDE00".repeat(200);
        when(postFeedRepository.countPosts("")).thenReturn(1L);
        when(postFeedRepository.findPosts(anyString(), anyInt(), anyLong()))
                .thenReturn(List.of(new PostResponse(1L, "Title", emoji, List.of(), 0, 0)));

        FeedResponse feed = postService.getFeed("", 1, 5);

        String truncated = feed.posts().get(0).text();
        assertThat(truncated).endsWith("\u2026");
        assertThat(truncated.substring(0, truncated.length() - 1).codePointCount(0, truncated.length() - 1)).isEqualTo(128);
    }

    @Test
    void firstPageHasPrevFalseHasNextTrue() {
        when(postFeedRepository.countPosts("")).thenReturn(11L);
        when(postFeedRepository.findPosts("", 5, 0L)).thenReturn(List.of());

        FeedResponse feed = postService.getFeed("", 1, 5);

        assertThat(feed.lastPage()).isEqualTo(3);
        assertThat(feed.hasPrev()).isFalse();
        assertThat(feed.hasNext()).isTrue();
    }

    @Test
    void middlePageHasPrevAndHasNextTrue() {
        when(postFeedRepository.countPosts("")).thenReturn(11L);
        when(postFeedRepository.findPosts("", 5, 5L)).thenReturn(List.of());

        FeedResponse feed = postService.getFeed("", 2, 5);

        assertThat(feed.lastPage()).isEqualTo(3);
        assertThat(feed.hasPrev()).isTrue();
        assertThat(feed.hasNext()).isTrue();
    }

    @Test
    void lastPageHasPrevTrueHasNextFalse() {
        when(postFeedRepository.countPosts("")).thenReturn(11L);
        when(postFeedRepository.findPosts("", 5, 10L)).thenReturn(List.of());

        FeedResponse feed = postService.getFeed("", 3, 5);

        assertThat(feed.lastPage()).isEqualTo(3);
        assertThat(feed.hasPrev()).isTrue();
        assertThat(feed.hasNext()).isFalse();
    }

    @Test
    void pageBeyondLastReturnsEmptyPostsAndHasNextFalse() {
        when(postFeedRepository.countPosts("")).thenReturn(11L);
        when(postFeedRepository.findPosts("", 5, 15L)).thenReturn(List.of());

        FeedResponse feed = postService.getFeed("", 4, 5);

        assertThat(feed.posts()).isEmpty();
        assertThat(feed.lastPage()).isEqualTo(3);
        assertThat(feed.hasPrev()).isTrue();
        assertThat(feed.hasNext()).isFalse();
    }

    @Test
    void emptyFeedReportsSingleEmptyPage() {
        when(postFeedRepository.countPosts("")).thenReturn(0L);
        when(postFeedRepository.findPosts("", 5, 0L)).thenReturn(List.of());

        FeedResponse feed = postService.getFeed("", 1, 5);

        assertThat(feed.posts()).isEmpty();
        assertThat(feed.lastPage()).isEqualTo(1);
        assertThat(feed.hasPrev()).isFalse();
        assertThat(feed.hasNext()).isFalse();
    }

    @Test
    void pageNumberZeroIsRejected() {
        assertThatThrownBy(() -> postService.getFeed("", 0, 5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("pageNumber");
    }

    @Test
    void pageSizeZeroIsRejected() {
        assertThatThrownBy(() -> postService.getFeed("", 1, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("pageSize");
    }

    @Test
    void searchTermIsPassedToRepository() {
        when(postFeedRepository.countPosts("Lalala")).thenReturn(0L);
        when(postFeedRepository.findPosts("Lalala", 5, 0L)).thenReturn(List.of());

        postService.getFeed("Lalala", 1, 5);

        verify(postFeedRepository).countPosts(eq("Lalala"));
        verify(postFeedRepository).findPosts(eq("Lalala"), eq(5), eq(0L));
    }

    @Test
    void getPostReturnsFullTextWithTagsAndCounters() {
        Post post = Post.builder().id(5L).title("Заголовок").text("Очень длинный текст поста").likesCount(3).build();
        when(postRepository.findById(5L)).thenReturn(Optional.of(post));
        when(postFeedRepository.findTags(5L)).thenReturn(List.of("a", "b"));
        when(postFeedRepository.countComments(5L)).thenReturn(2L);

        PostResponse response = postService.getPost(5L);

        assertThat(response.id()).isEqualTo(5L);
        assertThat(response.text()).isEqualTo("Очень длинный текст поста");
        assertThat(response.tags()).containsExactly("a", "b");
        assertThat(response.likesCount()).isEqualTo(3);
        assertThat(response.commentsCount()).isEqualTo(2);
    }

    @Test
    void getPostMissingThrowsNotFound() {
        when(postRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.getPost(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void createPostPersistsWithZeroCounters() {
        Post saved = Post.builder().id(10L).title("Заголовок").text("Текст").likesCount(0).build();
        when(postRepository.save(any(Post.class))).thenReturn(saved);

        PostResponse response = postService.createPost(new PostRequest("Заголовок", "Текст", List.of("a", "b")));

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.title()).isEqualTo("Заголовок");
        assertThat(response.tags()).containsExactly("a", "b");
        assertThat(response.likesCount()).isZero();
        assertThat(response.commentsCount()).isZero();
        verify(postFeedRepository).replaceTags(10L, List.of("a", "b"));
    }

    @Test
    void createPostRejectsBlankTitle() {
        assertThatThrownBy(() -> postService.createPost(new PostRequest(" ", "Текст", List.of())))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("title");
        verify(postRepository, never()).save(any());
    }

    @Test
    void createPostRejectsNullTags() {
        assertThatThrownBy(() -> postService.createPost(new PostRequest("Заголовок", "Текст", null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("tags");
    }

    @Test
    void updatePostUpdatesTitleTextAndTags() {
        Post existing = Post.builder().id(5L).title("Старое").text("Старый текст").likesCount(7).build();
        when(postRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(postFeedRepository.findTags(5L)).thenReturn(List.of("x"));
        when(postFeedRepository.countComments(5L)).thenReturn(4L);

        PostResponse response = postService.updatePost(5L, new PostUpdateRequest(5L, "Новое", "Новый текст", List.of("x")));

        assertThat(response.title()).isEqualTo("Новое");
        assertThat(response.text()).isEqualTo("Новый текст");
        assertThat(response.likesCount()).isEqualTo(7);
        assertThat(response.commentsCount()).isEqualTo(4);
        verify(postFeedRepository).replaceTags(5L, List.of("x"));
    }

    @Test
    void updatePostRejectsIdMismatch() {
        assertThatThrownBy(() -> postService.updatePost(1L, new PostUpdateRequest(2L, "T", "X", List.of())))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("id");
    }

    @Test
    void deletePostDeletesExistingPost() {
        when(postRepository.existsById(1L)).thenReturn(true);

        postService.deletePost(1L);

        verify(postRepository).deleteById(1L);
    }

    @Test
    void deletePostMissingThrowsNotFound() {
        when(postRepository.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() -> postService.deletePost(1L)).isInstanceOf(ResourceNotFoundException.class);
        verify(postRepository, never()).deleteById(anyLong());
    }

    @Test
    void likePostReturnsIncrementedCount() {
        when(postRepository.existsById(1L)).thenReturn(true);
        when(postFeedRepository.incrementLikes(1L)).thenReturn(6L);

        assertThat(postService.likePost(1L)).isEqualTo(6L);
    }

    @Test
    void likeMissingPostThrowsNotFound() {
        when(postRepository.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() -> postService.likePost(1L)).isInstanceOf(ResourceNotFoundException.class);
        verify(postFeedRepository, never()).incrementLikes(anyLong());
    }
}
