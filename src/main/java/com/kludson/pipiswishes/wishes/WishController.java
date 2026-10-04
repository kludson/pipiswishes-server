package com.kludson.pipiswishes.wishes;

import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/wish")
public class WishController {
    private final WishService wishService;

    private static final Logger logger = LoggerFactory.getLogger(WishController.class);

    public WishController(WishService wishService) {
        this.wishService = wishService;
    }

    @GetMapping()
    public ResponseEntity<List<Wish>> getAllWishes(
            @RequestParam(name = "status", required = false) WishStatus status,
            @RequestParam(name = "pageSize", required = false) Integer pageSize,
            @RequestParam(name = "pageNum", required = false) Integer pageNum
    ) {
        logger.info("Called getAllWishes");

        var filter = new WishFilter(
                status,
                pageSize,
                pageNum
        );
        return ResponseEntity.status(HttpStatus.OK)
                .body(wishService.searchAllWishesByFilter(filter));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Wish> getWishById(
            @PathVariable("id") Long id
    ) {
        logger.info("Called getWishById with id=" + id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(wishService.getWishById(id));
    }

    @PostMapping()
    public ResponseEntity<Wish> createWish(
            @RequestBody Wish wishToCreate
    ) {
        logger.info("Called createWish");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(wishService.createWish(wishToCreate));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Wish> updateWish(
            @PathVariable("id") Long id,
            @RequestBody Wish wishToUpdate
    ) {
        logger.info("Called updateWish");
        return ResponseEntity.status(HttpStatus.OK)
                .body(wishService.updateWish(id, wishToUpdate));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWish(
            @PathVariable("id") Long id
    ) {
        logger.info("Called deleteWish with id=" + id);
        try {
            wishService.deleteWish(id);

            return ResponseEntity.status(HttpStatus.OK).build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping("/{id}/execute")
    public ResponseEntity<Wish> executeWish(
            @PathVariable("id") Long id
    ) {
        logger.info("Called executeWish with id=" + id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(wishService.executeWish(id));
    }
}