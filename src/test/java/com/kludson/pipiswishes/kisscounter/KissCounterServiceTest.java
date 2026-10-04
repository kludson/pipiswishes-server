package com.kludson.pipiswishes.kisscounter;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class KissCounterServiceTest {
    private final KissCounterRepository repository = mock(KissCounterRepository.class);
    private final KissCounterService service = new KissCounterService(
            repository, new KissCounterMapper(), "Europe/Moscow");

    private LocalDate today() {
        return LocalDate.now(ZoneId.of("Europe/Moscow"));
    }

    @Test
    void presentsZeroForYesterdayWithoutMutatingTheStoredEntity() {
        LocalDate yesterday = today().minusDays(1);
        var entity = new KissCounterEntity(1L, 12, yesterday);
        when(repository.findById(1L)).thenReturn(Optional.of(entity));

        KissCounter result = service.getKissCounter(1L);

        assertEquals(0, result.counter());
        assertEquals(today(), result.countDate());
        assertEquals(12, entity.getCounter());
        assertEquals(yesterday, entity.getCountDate());
        verify(repository).findById(1L);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void presentsTodaysStoredCount() {
        when(repository.findById(1L)).thenReturn(Optional.of(
                new KissCounterEntity(1L, 7, today())));

        assertEquals(7, service.getKissCounter(1L).counter());
    }

    @Test
    void incrementsWithOneRepositoryCallAndTodaysDate() {
        when(repository.incrementCounter(1L, today())).thenReturn(1);

        service.incrementKissCounter(1L);

        verify(repository).incrementCounter(1L, today());
        verifyNoMoreInteractions(repository);
    }

    @Test
    void presentsZeroBeforeTheFirstClickWithoutCreatingARow() {
        when(repository.findById(1L)).thenReturn(Optional.empty());
        KissCounter result = service.getKissCounter(1L);
        assertEquals(1L, result.id());
        assertEquals(0, result.counter());
        assertEquals(today(), result.countDate());
        verify(repository).findById(1L);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void rejectsOtherIdsBeforeAccessingTheDatabase() {
        assertThrows(IllegalArgumentException.class, () -> service.incrementKissCounter(2L));
        assertThrows(IllegalArgumentException.class, () -> service.getKissCounter(2L));
        assertThrows(IllegalArgumentException.class, () -> service.incrementKissCounter(null));
        verifyNoInteractions(repository);
    }

    @Test
    void rejectsAnUnexpectedUpsertResult() {
        assertThrows(IllegalStateException.class, () -> service.incrementKissCounter(1L));
    }
}
