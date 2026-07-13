package com.AfriPass.afripass.Services;

import com.AfriPass.afripass.Enums.BookingStatus;
import com.AfriPass.afripass.Model.Booking;
import com.AfriPass.afripass.Model.EventInventory;
import com.AfriPass.afripass.Repositories.BookingRepository;
import com.AfriPass.afripass.Repositories.EventInventoryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingExpirationJob {

    private final BookingRepository bookingRepository;
    private final EventInventoryRepository eventInventoryRepository;

    @Scheduled(fixedRate = 6000)
    @Transactional
    public void expireStaleBookings() {

        List<Booking> expiredBookings = bookingRepository.findByBookingStatusAndExpiresAtBefore(BookingStatus.PENDING, LocalDateTime.now());

        for (Booking booking : expiredBookings) {
            EventInventory inventory = eventInventoryRepository.findByEventIdWithLock(booking
                            .getEventId())
                    .orElseThrow(() ->
                            new IllegalStateException("Inventory not found for event " + booking.getEventId()));
            inventory.setAvailableSeats(inventory.getAvailableSeats() + booking.getQuantity());
            eventInventoryRepository.save(inventory);

            booking.setBookingStatus(BookingStatus.EXPIRED);
            bookingRepository.save(booking);
        }

    }
}
