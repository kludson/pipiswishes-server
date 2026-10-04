package com.kludson.pipiswishes.kisscounter;

import org.springframework.stereotype.Component;

@Component 
public class KissCounterMapper {

    public KissCounter getDomain(KissCounterEntity entity) {
        return new KissCounter(
            entity.getId(),
            entity.getCounter(),
            entity.getCountDate()
        );
    }
    
    public KissCounterEntity getEntity(KissCounter counter) {
        return new KissCounterEntity(
            counter.id(),
            counter.counter(),
            counter.countDate()
        );
    }
}
