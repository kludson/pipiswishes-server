package com.kludson.pipiswishes.wishes;

import java.time.LocalDate;

public record Wish(
        Long id,
        String title,
        String description,
        WishStatus status,
        LocalDate deadline
)
{}