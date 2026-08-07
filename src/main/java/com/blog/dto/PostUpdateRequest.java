package com.blog.dto;

import java.util.List;

public record PostUpdateRequest(long id, String title, String text, List<String> tags) {
}
