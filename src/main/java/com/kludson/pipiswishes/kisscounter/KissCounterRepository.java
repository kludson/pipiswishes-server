package com.kludson.pipiswishes.kisscounter;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;

import jakarta.transaction.Transactional;

public interface KissCounterRepository extends JpaRepository<KissCounterEntity, Long> {

    @Transactional 
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = """
        INSERT INTO kiss_counters AS k (id, counter, count_date)
        VALUES (:id, 1, :today)
        ON CONFLICT (id) DO UPDATE
        SET counter = CASE
                WHEN k.count_date = EXCLUDED.count_date THEN k.counter + 1
                ELSE 1
            END,
            count_date = EXCLUDED.count_date
    """, nativeQuery = true)
    int incrementCounter(
        @Param("id") Long id,
        @Param("today") LocalDate today
    );
}
