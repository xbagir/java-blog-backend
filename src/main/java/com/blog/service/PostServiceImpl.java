package com.blog.service;

import com.blog.dto.FeedResponse;
import com.blog.dto.PostRequest;
import com.blog.dto.PostResponse;
import com.blog.dto.PostUpdateRequest;
import com.blog.exception.ResourceNotFoundException;
import com.blog.model.Post;
import com.blog.repository.CommentRepository;
import com.blog.repository.PostFeedRepository;
import com.blog.repository.PostLikesRepository;
import com.blog.repository.PostRepository;
import com.blog.repository.PostTagRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class PostServiceImpl extends AbstractPostService implements PostService {

    private static final int MAX_TEXT_LENGTH = 128;
    private static final String ELLIPSIS = "\u2026";

    private final PostFeedRepository postFeedRepository;
    private final PostTagRepository postTagRepository;
    private final PostLikesRepository postLikesRepository;
    private final CommentRepository commentRepository;

    public PostServiceImpl(PostRepository postRepository,
                           PostFeedRepository postFeedRepository,
                           PostTagRepository postTagRepository,
                           PostLikesRepository postLikesRepository,
                           CommentRepository commentRepository) {
        super(postRepository);
        this.postFeedRepository = postFeedRepository;
        this.postTagRepository = postTagRepository;
        this.postLikesRepository = postLikesRepository;
        this.commentRepository = commentRepository;
    }

    @Override
    public FeedResponse getFeed(String search, int pageNumber, int pageSize) {
        if (pageNumber < 1) {
            throw new IllegalArgumentException("pageNumber must be >= 1");
        }
        if (pageSize < 1) {
            throw new IllegalArgumentException("pageSize must be >= 1");
        }

        String normalizedSearch = search == null ? "" : search;
        long total = postFeedRepository.countPosts(normalizedSearch);
        int lastPage = total == 0 ? 1 : (int) Math.ceil((double) total / pageSize);

        long offset = (long) (pageNumber - 1) * pageSize;
        List<PostResponse> posts = postFeedRepository.findPosts(normalizedSearch, pageSize, offset)
                .stream()
                .map(this::truncateText)
                .toList();

        return new FeedResponse(posts, pageNumber > 1, pageNumber < lastPage, lastPage);
    }

    @Override
    public PostResponse getPost(long id) {
        Post post = findPost(id);
        return toPostResponse(post);
    }

    @Override
    @Transactional
    public PostResponse createPost(PostRequest request) {
        validatePostRequest(request.title(), request.text(), request.tags());

        Post post = Post.builder()
                .title(request.title().trim())
                .text(request.text())
                .likesCount(0)
                .build();
        Post saved = postRepository.save(post);
        postTagRepository.replaceTags(saved.getId(), request.tags());

        return new PostResponse(saved.getId(), saved.getTitle(), saved.getText(), request.tags(), 0, 0);
    }

    @Override
    @Transactional
    public PostResponse updatePost(long id, PostUpdateRequest request) {
        if (request.id() != id) {
            throw new IllegalArgumentException("id in body does not match the path");
        }
        validatePostRequest(request.title(), request.text(), request.tags());

        Post post = findPost(id);
        post.setTitle(request.title().trim());
        post.setText(request.text());
        Post saved = postRepository.save(post);
        postTagRepository.replaceTags(saved.getId(), request.tags());

        return toPostResponse(saved);
    }

    @Override
    @Transactional
    public void deletePost(long id) {
        requirePostExists(id);
        postRepository.deleteById(id);
    }

    @Override
    @Transactional
    public long likePost(long id) {
        requirePostExists(id);
        return postLikesRepository.incrementLikes(id);
    }

    private Post findPost(long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found: " + id));
    }

    private PostResponse toPostResponse(Post post) {
        return new PostResponse(
                post.getId(),
                post.getTitle(),
                post.getText(),
                postTagRepository.findTags(post.getId()),
                post.getLikesCount(),
                commentRepository.countByPostId(post.getId()));
    }

    private void validatePostRequest(String title, String text, List<String> tags) {
        requireText("title", title);
        requireText("text", text);
        if (tags == null) {
            throw new IllegalArgumentException("tags must not be null");
        }
    }

    private PostResponse truncateText(PostResponse post) {
        if (post.text() == null || post.text().codePointCount(0, post.text().length()) <= MAX_TEXT_LENGTH) {
            return post;
        }
        int end = post.text().offsetByCodePoints(0, MAX_TEXT_LENGTH);
        return new PostResponse(
                post.id(),
                post.title(),
                post.text().substring(0, end) + ELLIPSIS,
                post.tags(),
                post.likesCount(),
                post.commentsCount());
    }
}
