package com.blog.service;

import com.blog.dto.PostImage;
import com.blog.exception.ResourceNotFoundException;
import com.blog.repository.PostFeedRepository;
import com.blog.repository.PostRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImageServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private PostFeedRepository postFeedRepository;

    @InjectMocks
    private ImageServiceImpl imageService;

    @Test
    void updateImageStoresBytesAndContentType() {
        when(postRepository.existsById(1L)).thenReturn(true);

        imageService.updateImage(1L, new byte[]{1, 2, 3}, "image/png");

        verify(postFeedRepository).updateImage(1L, new byte[]{1, 2, 3}, "image/png");
    }

    @Test
    void getImageReturnsStoredImage() {
        when(postFeedRepository.findImage(1L)).thenReturn(new PostImage(new byte[]{1, 2, 3}, "image/png"));

        PostImage image = imageService.getImage(1L);

        assertThat(image.data()).containsExactly(1, 2, 3);
        assertThat(image.contentType()).isEqualTo("image/png");
    }

    @Test
    void getImageWithoutBytesThrowsNotFound() {
        when(postFeedRepository.findImage(1L)).thenReturn(new PostImage(null, null));

        assertThatThrownBy(() -> imageService.getImage(1L)).isInstanceOf(ResourceNotFoundException.class);
    }
}
