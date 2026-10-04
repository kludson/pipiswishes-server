package com.kludson.pipiswishes.wishes;

public record WishFilter(
        WishStatus status,
        Integer pageSize,
        Integer pageNum
) {
}
