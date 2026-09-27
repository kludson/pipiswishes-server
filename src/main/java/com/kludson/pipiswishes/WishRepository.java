package com.kludson.pipiswishes;

import org.springframework.data.jpa.repository.JpaRepository;

public interface WishRepository extends JpaRepository<WishEntity, Long> {
}
