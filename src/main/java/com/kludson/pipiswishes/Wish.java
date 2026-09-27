package com.kludson.pipiswishes;

public record Wish(
        Long id,
        String title,
        String description,
        WishStatus status
)
{}