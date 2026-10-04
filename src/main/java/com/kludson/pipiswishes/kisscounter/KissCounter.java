package com.kludson.pipiswishes.kisscounter;

import java.time.LocalDate;

public record KissCounter(
    Long id,
    Integer counter,
    LocalDate countDate
) {
}
