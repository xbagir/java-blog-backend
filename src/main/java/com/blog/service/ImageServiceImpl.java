package com.blog.service;

import com.blog.dto.PostImage;
import com.blog.exception.ResourceNotFoundException;
import com.blog.repository.PostImageRepository;
import com.blog.repository.PostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ImageServiceImpl extends AbstractPostService implements ImageService {

    private final PostImageRepository postImageRepository;

    public ImageServiceImpl(PostRepository postRepository, PostImageRepository postImageRepository) {
        super(postRepository);
        this.postImageRepository = postImageRepository;
    }

    @Override
    @Transactional
    public void updateImage(long id, byte[] image, String contentType) {
        requirePostExists(id);
        postImageRepository.updateImage(id, image, contentType);
    }

    @Override
    public PostImage getImage(long id) {
        PostImage image = postImageRepository.findImage(id);
        if (image == null || image.data() == null) {
            throw new ResourceNotFoundException("Image not found: " + id);
        }
        return image;
    }
}
