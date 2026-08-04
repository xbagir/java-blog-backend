package com.blog.service;

import com.blog.dto.PostImage;
import com.blog.exception.ResourceNotFoundException;
import com.blog.repository.PostFeedRepository;
import com.blog.repository.PostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ImageServiceImpl extends AbstractPostService implements ImageService {

    private final PostFeedRepository postFeedRepository;

    public ImageServiceImpl(PostRepository postRepository, PostFeedRepository postFeedRepository) {
        super(postRepository);
        this.postFeedRepository = postFeedRepository;
    }

    @Override
    @Transactional
    public void updateImage(long id, byte[] image, String contentType) {
        requirePostExists(id);
        postFeedRepository.updateImage(id, image, contentType);
    }

    @Override
    public PostImage getImage(long id) {
        PostImage image = postFeedRepository.findImage(id);
        if (image == null || image.data() == null) {
            throw new ResourceNotFoundException("Image not found: " + id);
        }
        return image;
    }
}
