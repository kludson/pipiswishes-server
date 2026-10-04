package com.kludson.pipiswishes.wishes;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WishService {

    private static final int DEFAULT_PAGE_SIZE = 5;
    private static final int DEFAULT_PAGE_NUM = 0;

    private WishRepository repository;

    public WishService(WishRepository repository) {
        this.repository = repository;
    }

    public List<Wish> searchAllWishesByFilter(WishFilter filter) {

        int pageSize = filter.pageSize() != null
                ? filter.pageSize() : DEFAULT_PAGE_SIZE;

        int pageNum = filter.pageNum() != null
                ? filter.pageNum() : DEFAULT_PAGE_NUM;

        var pageable = PageRequest.of(
            pageNum,
            pageSize,
            Sort.by("status").descending()
                .and(Sort.by("id")));

        List<WishEntity> allWishEntities = repository.searchAllWishesByFilter(
                filter.status(),
                pageable
        );

        return allWishEntities.stream()
                .map(this::toDomainWish)
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
                WishStatus.PENDING,
                wishToCreate.deadline()
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
                WishStatus.PENDING,
                wishToUpdate.deadline()
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
                WishStatus.EXECUTED,
                wishEntityToExecute.getDeadline()
        );

        var executedWish = repository.save(executedWishEntity);

        return toDomainWish(executedWish);
    }

    private Wish toDomainWish(WishEntity entity) {
        return new Wish(
                entity.getId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getStatus(),
                entity.getDeadline()
        );
    }
}
