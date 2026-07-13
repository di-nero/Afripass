package com.AfriPass.afripass.Services;

import com.AfriPass.afripass.DTOs.BookingRequest;
import com.AfriPass.afripass.DTOs.BookingResponse;
import com.AfriPass.afripass.Enums.BookingStatus;
import com.AfriPass.afripass.Exception.ResourceNotFoundException;
import com.AfriPass.afripass.Model.Booking;
import com.AfriPass.afripass.Model.EventInventory;
import com.AfriPass.afripass.Model.User;
import com.AfriPass.afripass.Repositories.BookingRepository;
import com.AfriPass.afripass.Repositories.EventInventoryRepository;
import com.AfriPass.afripass.Repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookingService {

    private final UserRepository userRepository;
    private final EventInventoryRepository eventInventoryRepository;
    private final BookingRepository bookingRepository;


    @Transactional
    public BookingResponse bookEvent(BookingRequest booking) {

        //this is how to get the logged-in user email
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByEmail(email).orElseThrow(() ->
                new ResourceNotFoundException("user not found by email: " + email));

        //this is where pessimistic lock come in
        EventInventory eventInventory = eventInventoryRepository
                .findByEventIdWithLock(booking.getEventId())
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found for event: " + booking.getEventId()));

        if (booking.getQuantity() > eventInventory.getAvailableSeats()) {
            throw new ResourceNotFoundException("Out of seats");
        }
        eventInventory.setAvailableSeats(eventInventory.getAvailableSeats() - booking.getQuantity());

        eventInventoryRepository.save(eventInventory);

        Booking bookingEntity = Booking.builder()
                .eventId(booking.getEventId())
                .quantity(booking.getQuantity())
                .bookingStatus(BookingStatus.PENDING)
                .user(user)
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .build();

        bookingRepository.save(bookingEntity);

        log.info(
                "Booking {} created for user {}",
                bookingEntity.getId(),
                email);

        return BookingResponse.builder()
                .bookingId(bookingEntity.getId())
                .eventId(bookingEntity.getEventId())
                .bookingStatus(bookingEntity.getBookingStatus())
                .createdAt(bookingEntity.getCreatedAt())
                .build();

    }

}
