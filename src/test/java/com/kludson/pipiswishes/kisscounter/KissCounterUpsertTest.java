package com.kludson.pipiswishes.kisscounter;

import java.sql.Connection;
import java.sql.DriverManager;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.data.jpa.repository.Query;

import static org.junit.jupiter.api.Assertions.*;

/** Uses a disposable schema, never the application's counter table. */
@EnabledIfEnvironmentVariable(named = "KISS_TEST_DB_URL", matches = ".+")
class KissCounterUpsertTest {
    private Connection connect(String schema) throws Exception {
        Connection connection = DriverManager.getConnection(
                System.getenv("KISS_TEST_DB_URL"),
                System.getenv().getOrDefault("KISS_TEST_DB_USER", "postgres"),
                System.getenv("KISS_TEST_DB_PASSWORD"));
        try (var statement = connection.createStatement()) {
            statement.execute("SET search_path TO " + schema);
        }
        return connection;
    }

    private void click(Connection connection, LocalDate date) throws Exception {
        String sql = KissCounterRepository.class
                .getMethod("incrementCounter", Long.class, LocalDate.class)
                .getAnnotation(Query.class).value()
                .replace(":id", "?").replace(":today", "?");
        try (var statement = connection.prepareStatement(sql)) {
            statement.setLong(1, 1L);
            statement.setObject(2, date);
            assertEquals(1, statement.executeUpdate());
        }
    }

    private void assertCounter(Connection connection, int count, LocalDate date) throws Exception {
        try (var statement = connection.createStatement();
             var result = statement.executeQuery("SELECT id, counter, count_date FROM kiss_counters")) {
            assertTrue(result.next());
            assertEquals(1L, result.getLong("id"));
            assertEquals(count, result.getInt("counter"));
            assertEquals(date, result.getObject("count_date", LocalDate.class));
            assertFalse(result.next(), "Only one counter row may exist");
        }
    }

    private void concurrentClicks(String schema, LocalDate date) throws Exception {
        try (var executor = Executors.newFixedThreadPool(8)) {
            var tasks = new ArrayList<Callable<Void>>();
            for (int i = 0; i < 24; i++) {
                tasks.add(() -> {
                    try (Connection connection = connect(schema)) {
                        click(connection, date);
                    }
                    return null;
                });
            }
            for (var result : executor.invokeAll(tasks, 30, TimeUnit.SECONDS)) {
                result.get();
            }
        }
    }

    @Test
    void createsIncrementsResetsAndPreservesConcurrentClicks() throws Exception {
        String schema = "kiss_counter_test_" + UUID.randomUUID().toString().replace("-", "");
        LocalDate day = LocalDate.of(2026, 10, 4);
        try (Connection connection = connect(schema); var statement = connection.createStatement()) {
            statement.execute("CREATE SCHEMA " + schema);
            try {
                statement.execute("""
                        CREATE TABLE kiss_counters (
                            id bigint PRIMARY KEY CHECK (id = 1),
                            counter integer NOT NULL CHECK (counter >= 0),
                            count_date date NOT NULL
                        )
                        """);
                click(connection, day);
                assertCounter(connection, 1, day);
                click(connection, day);
                assertCounter(connection, 2, day);
                click(connection, day.plusDays(1));
                assertCounter(connection, 1, day.plusDays(1));

                concurrentClicks(schema, day.plusDays(2));
                assertCounter(connection, 24, day.plusDays(2));

                statement.execute("TRUNCATE kiss_counters");
                concurrentClicks(schema, day.plusDays(3));
                assertCounter(connection, 24, day.plusDays(3));
            } finally {
                statement.execute("DROP SCHEMA " + schema + " CASCADE");
            }
        }
    }
}
