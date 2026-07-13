package com.AfriPass.afripass.Services;

import com.AfriPass.afripass.Enums.BookingStatus;
import com.AfriPass.afripass.Model.Booking;
import com.AfriPass.afripass.Model.EventInventory;
import com.AfriPass.afripass.Repositories.BookingRepository;
import com.AfriPass.afripass.Repositories.EventInventoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
    class BookingExpirationJobTest {

        @Mock
        private BookingRepository bookingRepository;

        @Mock
        private EventInventoryRepository eventInventoryRepository;

        @InjectMocks
        private BookingExpirationJob bookingExpirationJob;

        @Test
        void expireStaleBookings_shouldExpireBookingAndRestoreSeats() {

            // Arrange
            Booking booking = new Booking();
            booking.setEventId(1L);
            booking.setQuantity(2);
            booking.setBookingStatus(BookingStatus.PENDING);

            EventInventory inventory = new EventInventory();
            inventory.setAvailableSeats(10);

            when(bookingRepository.findByBookingStatusAndExpiresAtBefore(
                    eq(BookingStatus.PENDING),
                    any(LocalDateTime.class)
            )).thenReturn(List.of(booking));

            when(eventInventoryRepository.findByEventIdWithLock(1L))
                    .thenReturn(Optional.of(inventory));

            // Act
            bookingExpirationJob.expireStaleBookings();

            // Assert
            assertEquals(12, inventory.getAvailableSeats());
            assertEquals(BookingStatus.EXPIRED, booking.getBookingStatus());

            verify(eventInventoryRepository).save(inventory);
            verify(bookingRepository).save(booking);
        }

        @Test
        void expireStaleBookings_shouldThrowExceptionWhenInventoryNotFound() {

            // Arrange
            Booking booking = new Booking();
            booking.setEventId(1L);
            booking.setQuantity(2);
            booking.setBookingStatus(BookingStatus.PENDING);

            when(bookingRepository.findByBookingStatusAndExpiresAtBefore(
                    eq(BookingStatus.PENDING),
                    any(LocalDateTime.class)
            )).thenReturn(List.of(booking));

            when(eventInventoryRepository.findByEventIdWithLock(1L))
                    .thenReturn(Optional.empty());

            // Act & Assert
            IllegalStateException exception = assertThrows(
                    IllegalStateException.class,
                    () -> bookingExpirationJob.expireStaleBookings()
            );

            assertEquals(
                    "Inventory not found for event 1",
                    exception.getMessage()
            );

            verify(eventInventoryRepository).findByEventIdWithLock(1L);
            verify(bookingRepository, never()).save(any());
        }

}
