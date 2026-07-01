package com.AfriPass.afripass.Services;

import com.AfriPass.afripass.DTOs.BookingRequest;
import com.AfriPass.afripass.DTOs.BookingResponse;
import com.AfriPass.afripass.DTOs.EventDetails;
import com.AfriPass.afripass.Exception.ResourceNotFoundException;
import com.AfriPass.afripass.Model.Booking;
import com.AfriPass.afripass.Model.BookingStatus;
import com.AfriPass.afripass.Model.EventInventory;
import com.AfriPass.afripass.Model.User;
import com.AfriPass.afripass.Repositories.BookingRepository;
import com.AfriPass.afripass.Repositories.EventInventoryRepository;
import com.AfriPass.afripass.Repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service
@Slf4j
public class BookingService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EventInventoryRepository eventInventoryRepository;
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private EventService eventService;
    @Autowired
    @Qualifier("taskExecutor")
    private Executor taskExecutor;

    @Transactional
    public BookingResponse bookEvent(BookingRequest booking){

        //this is how to get the logged-in user email
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("user not found by email: " + email));

        //check if the event exists in our web catalog
        CompletableFuture<EventDetails> eventFuture = CompletableFuture.supplyAsync(() -> eventService.getEventById(booking.getEventId()) , taskExecutor);

        //check if the row actually exists in our repository
        CompletableFuture<Void> inventoryCheckFuture = CompletableFuture
                .runAsync(() -> {
                    if (!eventInventoryRepository.existsByEventId(
                            booking.getEventId())) {
                        throw new ResourceNotFoundException(
                                "No inventory found for event: "
                                        + booking.getEventId());
                    }
                }, taskExecutor);

        CompletableFuture.allOf(eventFuture , inventoryCheckFuture).join();

        //this is where pessimistic lock come in
        EventInventory eventInventory = eventInventoryRepository.findByEventIdWithLock(booking.getEventId());

        if (booking.getQuantity() <= eventInventory.getAvailableSeats()){
            int newQuantity = eventInventory.getAvailableSeats() - booking.getQuantity() ;
            eventInventory.setAvailableSeats(newQuantity);
        }else {
            throw new ResourceNotFoundException("Out of seats");
        }
        eventInventoryRepository.save(eventInventory);

        Booking book = Booking.builder()
                .eventId(booking.getEventId())
                .quantity(booking.getQuantity())
                .bookingStatus(BookingStatus.PENDING)
                .user(user)
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .build();

        bookingRepository.save(book);
        return BookingResponse.builder()
                .bookingId(book.getId())
                .eventId(book.getEventId())
                .bookingStatus(book.getBookingStatus())
                .createdAt(book.getCreatesAt())
                .build();
    }

}
