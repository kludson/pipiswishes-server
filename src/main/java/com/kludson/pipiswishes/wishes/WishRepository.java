package com.kludson.pipiswishes.wishes;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WishRepository extends JpaRepository<WishEntity, Long> {

    @Query("""
            SELECT w FROM WishEntity w
                WHERE (:status IS NULL OR w.status = :status)
            """)
    List<WishEntity> searchAllWishesByFilter(
        @Param("status")WishStatus status,
        Pageable pageable
    );
}
