package com.kludson.pipiswishes;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WishService {
    private WishRepository repository;

    public WishService(WishRepository repository) {
        this.repository = repository;
    }

    public List<Wish> getAllWishes() {
        List<WishEntity> allWishEntities = repository.findAll();

        return allWishEntities.stream()
                .map(this::toDomainWish)
                .toList();
    }

    public List<Wish> getAllWishesWithStatusPending() {
        List<WishEntity> allWishEntities = repository.findAll();

        return allWishEntities.stream()
                .map(this::toDomainWish)
                .filter(it -> it.status() == WishStatus.PENDING)
                .toList();
    }

    public List<Wish> getAllWishesWithStatusExecuted() {
        List<WishEntity> allWishEntities = repository.findAll();

        return allWishEntities.stream()
                .map(this::toDomainWish)
                .filter(it -> it.status() == WishStatus.EXECUTED)
                .toList();
    }

    public Wish getWishById(Long id) {
        WishEntity wishEntity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Not found wish by id=" + id
                ));
        return toDomainWish(wishEntity);
    }

    public Wish createWish(Wish wishToCreate) {
        if (wishToCreate.id() != null) {
            throw new IllegalArgumentException("Id should be empty");
        }

        if (wishToCreate.status() != null) {
            throw new IllegalArgumentException("Status should be empty");
        }

        var wishEntity = new WishEntity(
                null,
                wishToCreate.title(),
                wishToCreate.description(),
                WishStatus.PENDING
        );

        var createdEntity = repository.save(wishEntity);

        return toDomainWish(createdEntity);
    }

    public Wish updateWish(Long id, Wish wishToUpdate) {
        WishEntity wishEntity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Not found wish by id=" + id
                ));

        if (wishEntity.getStatus() != WishStatus.PENDING) {
            throw new IllegalArgumentException("Cannot modify wish: status=" + wishEntity.getStatus());
        }

        var updatedWishEntity = new WishEntity(
                wishEntity.getId(),
                wishToUpdate.title(),
                wishToUpdate.description(),
                WishStatus.PENDING
        );

        var updatedWish = repository.save(updatedWishEntity);

        return toDomainWish(updatedWish);
    }

    public void deleteWish(Long id) {
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException("Not found wish by id=" + id);
        }

        repository.deleteById(id);
    }

    public Wish executeWish(Long id) {
        WishEntity wishEntityToExecute = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Not found wish by id=" + id));

        if (wishEntityToExecute.getStatus() != WishStatus.PENDING) {
            throw new IllegalArgumentException("Cannot modify wish: status=" + wishEntityToExecute.getStatus());
        }

        var executedWishEntity = new WishEntity(
                wishEntityToExecute.getId(),
                wishEntityToExecute.getTitle(),
                wishEntityToExecute.getDescription(),
                WishStatus.EXECUTED
        );

        var executedWish = repository.save(executedWishEntity);

        return toDomainWish(executedWish);
    }

    private Wish toDomainWish(WishEntity entity) {
        return new Wish(
                entity.getId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getStatus()
        );
    }
}
