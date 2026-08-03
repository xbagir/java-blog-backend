package com.blog.dto;

import java.util.List;

public record PostRequest(String title, String text, List<String> tags) {
}
