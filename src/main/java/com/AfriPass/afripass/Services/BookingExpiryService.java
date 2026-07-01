package com.AfriPass.afripass.Services;

import com.AfriPass.afripass.Model.Booking;
import com.AfriPass.afripass.Model.BookingStatus;
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
public class BookingExpiryService {

    private final BookingRepository bookingRepository;
    private final EventInventoryRepository eventInventoryRepository;

    @Scheduled(fixedRate = 6000)
    @Transactional
    public void expiredStaleBookings() {

        List<Booking> expiredBooking = bookingRepository.findByBookingStatusAndExpiresAtBefore(BookingStatus.PENDING, LocalDateTime.now());

        for (Booking booking : expiredBooking) {
            EventInventory inventory = eventInventoryRepository.findByEventIdWithLock(booking.getEventId());
            inventory.setAvailableSeats(booking.getQuantity() + inventory.getAvailableSeats());
            eventInventoryRepository.save(inventory);

            booking.setBookingStatus(BookingStatus.EXPIRED);
            bookingRepository.save(booking);
        }

    }
}
