package com.kludson.pipiswishes.kisscounter;

import java.time.LocalDate;

import org.hibernate.annotations.ColumnDefault;

import jakarta.persistence.CheckConstraint;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity 
@Table(
    name = "KissCounters",
    check = {
        @CheckConstraint(
            name = "kiss_counter_singleton",
            constraint = "id = 1"
        ),
        @CheckConstraint(
            name = "kiss_counter_non_negative",
            constraint = "counter >= 0"
        )
    }
)
public class KissCounterEntity {
    
    @Id
    @Column(name = "id", nullable = false)
    @ColumnDefault("1")
    private Long id = 1L;

    @Column(name = "counter", nullable = false)
    @ColumnDefault("0")
    private Integer counter = 0;

    @Column(name = "count_date", nullable = false)
    private LocalDate countDate = LocalDate.now();
    
    public KissCounterEntity() {

    }

    public KissCounterEntity(
        Long id,
        Integer counter,
        LocalDate countDate
    ) {
        this.id  = id;
        this.counter = counter;
        this.countDate = countDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getCounter() {
        return counter;
    }

    public void setCounter(Integer counter) {
        this.counter = counter;
    }

    public LocalDate getCountDate() {
        return countDate;
    }

    public void setCountDate(LocalDate countDate) {
        this.countDate = countDate;
    }


} 