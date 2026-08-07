package com.blog.service;

import com.blog.dto.PostImage;

public interface ImageService {

    void updateImage(long id, byte[] image, String contentType);

    PostImage getImage(long id);
}
