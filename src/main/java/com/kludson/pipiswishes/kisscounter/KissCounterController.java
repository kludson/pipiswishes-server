package com.kludson.pipiswishes.kisscounter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/kisscount")
public class KissCounterController {

    private final KissCounterService service;

    private final static Logger logger = LoggerFactory.getLogger(KissCounterController.class);

    public KissCounterController(KissCounterService service) {
        this.service = service;
    }

    @GetMapping()
    public ResponseEntity<KissCounter> getLastCounter(
        @RequestParam(name = "id") Long id 
    ) {
        logger.info("Called getLastCounter");

        return ResponseEntity.status(200)
            .body(service.getKissCounter(id));
    }

    @PutMapping()
    public ResponseEntity<Void> incremCounter(
        @RequestParam(name = "id") Long id
    ) {
        logger.info("Called incremCounter");
        
        service.incrementKissCounter(id);

        return ResponseEntity.status(200)
            .build();
    }
    
}
