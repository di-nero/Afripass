package com.AfriPass.afripass.Client;

import com.AfriPass.afripass.DTOs.EventDetails;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
public class FakeEventCatalogClient implements EventCatalogClient {

    private final List<EventDetails> events = List.of(
            new EventDetails(1L, "Afrobeats Festival Lagos", LocalDateTime.of(2026, 8, 15, 19, 0), "Eko Convention Centre", "Lagos", new BigDecimal("150.00"), 0),
            new EventDetails(2L, "Champions League Final", LocalDateTime.of(2026, 5, 30, 19, 0), "Wembley Stadium", "London", new BigDecimal("350.00"), 0),
            new EventDetails(3L, "Burna Boy World Tour", LocalDateTime.of(2026, 9, 20, 19, 0), "O2 Arena", "London", new BigDecimal("200.00"), 0),
            new EventDetails(4L, "Comedy Night Africa", LocalDateTime.of(2026, 7, 10, 19, 0), "Landmark Beach", "Lagos", new BigDecimal("80.00"), 0),
            new EventDetails(5L, "Wizkid Live in Concert", LocalDateTime.of(2026, 10, 5, 19, 0), "Madison Square Garden", "New York", new BigDecimal("300.00"), 0),
            new EventDetails(6L, "Tech Summit Africa", LocalDateTime.of(2026, 8, 1, 9, 0), "Eko Hotel", "Lagos", new BigDecimal("50.00"), 0),
            new EventDetails(7L, "Coldplay World Tour", LocalDateTime.of(2026, 11, 15, 19, 0), "Camp Nou", "Barcelona", new BigDecimal("250.00"), 0),
            new EventDetails(8L, "Davido 30 Billion Concert", LocalDateTime.of(2026, 12, 25, 19, 0), "National Stadium", "Abuja", new BigDecimal("100.00"), 0)
    );

    @Override
    public List<EventDetails> getAll() {
        System.out.println("reach here");
        return events;
    }

    @Override
    public Optional<EventDetails> getById(Long id) {
        return events
                .stream()
                .filter(events -> events.getId().equals(id))
                .findFirst();
    }
}