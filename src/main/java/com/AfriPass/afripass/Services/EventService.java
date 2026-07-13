package com.AfriPass.afripass.Services;


import com.AfriPass.afripass.Client.EventCatalogClient;
import com.AfriPass.afripass.DTOs.EventDetails;
import com.AfriPass.afripass.Exception.ResourceNotFoundException;
import com.AfriPass.afripass.Model.EventInventory;
import com.AfriPass.afripass.Repositories.EventInventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventService {

    private final EventCatalogClient eventCatalogClient;
    private final EventInventoryRepository eventInventoryRepository;

    public List<EventDetails> getAllEvents() {
        List<EventDetails> events = eventCatalogClient.getAll();

        for (EventDetails event : events) {
            EventInventory inventory = eventInventoryRepository
                    .findByEventId(event.getId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException("Inventory not found for event: " + event.getId()));
            event.setAvailableSeats(inventory.getAvailableSeats());
        }
        log.debug("Fetching all events");
        return events;
    }


    public EventDetails getEventById(Long id) {
        EventDetails event = eventCatalogClient.getById(id).orElseThrow(() -> new ResourceNotFoundException("no event by id: " + id));

        EventInventory inventory = eventInventoryRepository.findByEventId(id).orElseThrow(() -> new ResourceNotFoundException("Inventory not found for event: " + id));

        event.setAvailableSeats(inventory.getAvailableSeats());
        log.debug("Fetching event {}", id);
        return event;
    }
}
