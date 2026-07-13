package com.AfriPass.afripass.Services;

import com.AfriPass.afripass.Client.EventCatalogClient;
import com.AfriPass.afripass.DTOs.EventDetails;
import com.AfriPass.afripass.Exception.ResourceNotFoundException;
import com.AfriPass.afripass.Model.EventInventory;
import com.AfriPass.afripass.Repositories.EventInventoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventCatalogClient eventCatalogClient;

    @Mock
    private EventInventoryRepository eventInventoryRepository;

    @InjectMocks
    private EventService eventService;

    @Test
    void getAllEvents_shouldReturnEventsWithAvailableSeats() {

        // Arrange

        EventDetails event1 = new EventDetails();
        event1.setId(1L);
        event1.setEventName("Spring Boot Conference");

        EventDetails event2 = new EventDetails();
        event2.setId(2L);
        event2.setEventName("Java Summit");

        List<EventDetails> events = List.of(event1, event2);

        EventInventory inventory1 = new EventInventory();
        inventory1.setEventId(1L);
        inventory1.setAvailableSeats(100);

        EventInventory inventory2 = new EventInventory();
        inventory2.setEventId(2L);
        inventory2.setAvailableSeats(50);

        when(eventCatalogClient.getAll())
                .thenReturn(events);

        when(eventInventoryRepository.findByEventId(1L))
                .thenReturn(Optional.of(inventory1));

        when(eventInventoryRepository.findByEventId(2L))
                .thenReturn(Optional.of(inventory2));

        // Act

        List<EventDetails> result = eventService.getAllEvents();

        // Assert

        assertEquals(2, result.size());

        assertEquals(100, result.get(0).getAvailableSeats());
        assertEquals(50, result.get(1).getAvailableSeats());

        verify(eventCatalogClient).getAll();
        verify(eventInventoryRepository).findByEventId(1L);
        verify(eventInventoryRepository).findByEventId(2L);
    }

    @Test
    void getAllEvents_shouldThrowWhenInventoryNotFound() {

        // Arrange

        EventDetails event = new EventDetails();
        event.setId(1L);
        event.setEventName("Spring Boot Conference");

        when(eventCatalogClient.getAll())
                .thenReturn(List.of(event));

        when(eventInventoryRepository.findByEventId(1L))
                .thenReturn(Optional.empty());

        // Act & Assert

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> eventService.getAllEvents()
        );

        assertEquals(
                "Inventory not found for event: 1",
                exception.getMessage()
        );

        verify(eventCatalogClient).getAll();
        verify(eventInventoryRepository).findByEventId(1L);
    }

    @Test
    void getEventById_shouldReturnEventWithAvailableSeats() {

        // Arrange

        Long eventId = 1L;

        EventDetails event = new EventDetails();
        event.setId(eventId);
        event.setEventName("Spring Boot Conference");

        EventInventory inventory = new EventInventory();
        inventory.setEventId(eventId);
        inventory.setAvailableSeats(100);

        when(eventCatalogClient.getById(eventId))
                .thenReturn(Optional.of(event));

        when(eventInventoryRepository.findByEventId(eventId))
                .thenReturn(Optional.of(inventory));

        // Act

        EventDetails result = eventService.getEventById(eventId);

        // Assert

        assertNotNull(result);
        assertEquals(eventId, result.getId());
        assertEquals("Spring Boot Conference", result.getEventName());
        assertEquals(100, result.getAvailableSeats());

        verify(eventCatalogClient).getById(eventId);
        verify(eventInventoryRepository).findByEventId(eventId);
    }

    @Test
    void getEventById_shouldThrowWhenInventoryNotFound() {

        // Arrange

        Long eventId = 1L;

        EventDetails event = new EventDetails();
        event.setId(eventId);
        event.setEventName("Spring Boot Conference");

        when(eventCatalogClient.getById(eventId))
                .thenReturn(Optional.of(event));

        when(eventInventoryRepository.findByEventId(eventId))
                .thenReturn(Optional.empty());

        // Act & Assert

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> eventService.getEventById(eventId)
        );

        assertEquals(
                "Inventory not found for event: 1",
                exception.getMessage()
        );

        verify(eventCatalogClient).getById(eventId);
        verify(eventInventoryRepository).findByEventId(eventId);
    }

}
