package com.kludson.pipiswishes.wishes;

public record Wish(
        Long id,
        String title,
        String description,
        WishStatus status
)
{}