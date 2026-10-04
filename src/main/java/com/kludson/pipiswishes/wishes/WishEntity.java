package com.kludson.pipiswishes.wishes;

import java.time.LocalDate;

import jakarta.persistence.*;

@Table(name="wishes")
@Entity
public class WishEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "title")
    private String title;

    @Column(name = "description")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private WishStatus status;

    @Column(name = "deadline")
    private LocalDate deadline;

    public WishEntity() {
    }

    public WishEntity(Long id, String title, String description, WishStatus status, LocalDate deadline) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.deadline = deadline;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public WishStatus getStatus() {
        return status;
    }

    public void setStatus(WishStatus status) {
        this.status = status;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }
}