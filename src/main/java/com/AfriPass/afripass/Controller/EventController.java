package com.AfriPass.afripass.Controller;

import com.AfriPass.afripass.DTOs.EventDetails;
import com.AfriPass.afripass.Services.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/events")
public class EventController {

    @Autowired
    private EventService eventService;

    @GetMapping
    public ResponseEntity<List<EventDetails>> getAllEvents() {
        System.out.println("endpoint was hit");
        return ResponseEntity.ok(eventService.getAllEvents());
    }

    @GetMapping("/{id}")
    public EventDetails getEventById(@PathVariable Long id) {
        return eventService.getEventById(id);
    }
}
