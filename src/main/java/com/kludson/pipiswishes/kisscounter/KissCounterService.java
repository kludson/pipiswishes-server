package com.kludson.pipiswishes.kisscounter;

import java.time.LocalDate;
import java.time.ZoneId;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service 
public class KissCounterService {

    private final KissCounterRepository repository;

    private final KissCounterMapper mapper;
    private final ZoneId zone;
    private static final long KISS_COUNTER_ID = 1L;

    public KissCounterService(KissCounterRepository repository, KissCounterMapper mapper,
            @Value("${app.kiss-counter.time-zone:Europe/Moscow}") String timeZone) {
        this.repository = repository;
        this.mapper = mapper;
        this.zone = ZoneId.of(timeZone);
    }

    public KissCounter getKissCounter(Long id) {
        requireSingletonId(id);
        LocalDate today = LocalDate.now(zone);
        var kissCounterEntity = repository.findById(id)
            .orElse(null);

        if (kissCounterEntity == null) {
            return new KissCounter(KISS_COUNTER_ID, 0, today);
        }
        
        if (!today.equals(kissCounterEntity.getCountDate())) {
            return new KissCounter(kissCounterEntity.getId(), 0, today);
        }
        
        return mapper.getDomain(kissCounterEntity);
    }

    public void incrementKissCounter(Long id) {
        requireSingletonId(id);
        LocalDate today = LocalDate.now(zone);
        if (repository.incrementCounter(id, today) != 1) {
            throw new IllegalStateException("Expected one kissCounter row to be inserted or updated");
        }
    }

    private void requireSingletonId(Long id) {
        if (id == null || id != KISS_COUNTER_ID) {
            throw new IllegalArgumentException("Kiss counter id must be 1");
        }
    }
    
}
