package com.AfriPass.afripass.Services;


import com.AfriPass.afripass.Client.EventCatalogClient;
import com.AfriPass.afripass.DTOs.EventDetails;
import com.AfriPass.afripass.Exception.ResourceNotFoundException;
import com.AfriPass.afripass.Model.EventInventory;
import com.AfriPass.afripass.Repositories.EventInventoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class EventService {

    @Autowired
    private EventCatalogClient eventCatalogClient;
    @Autowired
    private EventInventoryRepository eventInventoryRepository;

//    @Cacheable(value = "events")
    public List<EventDetails> getAllEvent() {
        List<EventDetails> events = eventCatalogClient.getAll();

        for (EventDetails event : events) {
            EventInventory inventory = eventInventoryRepository.findByEventId(event.getId()).orElseThrow(() -> new ResourceNotFoundException("Cant find event by id: " + event.getId()));
            if (inventory != null) {
                event.setAvailableSeats(inventory.getAvailableSeats());
            }
        }

        return events;
    }

//    @Cacheable(value = "eventById", key = "#id")
    public EventDetails getEventById(Long id){
        EventDetails event = eventCatalogClient.getById(id).orElseThrow(() -> new ResourceNotFoundException("no event by id: " + id));

        EventInventory inventory = eventInventoryRepository.findByEventId(id).orElseThrow(() -> new ResourceNotFoundException("Inventory not found for event: " + id));

        event.setAvailableSeats(inventory.getAvailableSeats());
        return event;
    }
}
