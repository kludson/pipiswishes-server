package com.kludson.pipiswishes.web;

import java.time.LocalDateTime;

public record ErrorResponseDto(
    String message,
    String detailedMessage,
    LocalDateTime errorTime
) {
}
