package com.AfriPass.afripass.Services;

import com.AfriPass.afripass.DTOs.BookingRequest;
import com.AfriPass.afripass.DTOs.BookingResponse;
import com.AfriPass.afripass.Enums.BookingStatus;
import com.AfriPass.afripass.Model.Booking;
import com.AfriPass.afripass.Model.EventInventory;
import com.AfriPass.afripass.Model.User;
import com.AfriPass.afripass.Repositories.BookingRepository;
import com.AfriPass.afripass.Repositories.EventInventoryRepository;
import com.AfriPass.afripass.Repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Optional;
import static org.mockito.Mockito.never;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.AfriPass.afripass.Exception.ResourceNotFoundException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EventInventoryRepository eventInventoryRepository;

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private BookingService bookingService;
    @Test
    void bookEvent_shouldCreateBookingSuccessfully() {

        // Arrange

        // Fake logged-in user
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "joseph@gmail.com",
                        null
                )
        );
        SecurityContextHolder.setContext(context);

        BookingRequest request = new BookingRequest();
        request.setEventId(1L);
        request.setQuantity(2);

        User user = User.builder()
                .name("Joseph")
                .email("joseph@gmail.com")
                .build();

        EventInventory inventory = new EventInventory();
        inventory.setEventId(1L);
        inventory.setAvailableSeats(10);

        when(userRepository.findByEmail("joseph@gmail.com"))
                .thenReturn(Optional.of(user));

        when(eventInventoryRepository.findByEventIdWithLock(1L))
                .thenReturn(Optional.of(inventory));

        when(bookingRepository.save(any(Booking.class)))
                .thenAnswer(invocation -> {
                    Booking booking = invocation.getArgument(0);
                    booking.setId(1L);
                    booking.setCreatedAt(LocalDateTime.now());
                    return booking;
                });

        // Act
        BookingResponse response = bookingService.bookEvent(request);

        // Assert

        assertNotNull(response);
        assertEquals(1L, response.getBookingId());
        assertEquals(1L, response.getEventId());
        assertEquals(BookingStatus.PENDING, response.getBookingStatus());

        assertEquals(8, inventory.getAvailableSeats());

        verify(userRepository).findByEmail("joseph@gmail.com");
        verify(eventInventoryRepository).findByEventIdWithLock(1L);
        verify(eventInventoryRepository).save(inventory);
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void bookEvent_shouldThrowWhenUserNotFound() {

        // Arrange

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "joseph@gmail.com",
                        null
                )
        );
        SecurityContextHolder.setContext(context);

        BookingRequest request = new BookingRequest();
        request.setEventId(1L);
        request.setQuantity(2);

        when(userRepository.findByEmail("joseph@gmail.com"))
                .thenReturn(Optional.empty());

        // Act & Assert

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> bookingService.bookEvent(request)
        );

        assertEquals(
                "user not found by email: joseph@gmail.com",
                exception.getMessage()
        );

        verify(userRepository).findByEmail("joseph@gmail.com");
        verifyNoInteractions(eventInventoryRepository);
        verifyNoInteractions(bookingRepository);
    }

    @Test
    void bookEvent_shouldThrowWhenInventoryNotFound() {

        // Arrange

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "joseph@gmail.com",
                        null
                )
        );
        SecurityContextHolder.setContext(context);

        BookingRequest request = new BookingRequest();
        request.setEventId(1L);
        request.setQuantity(2);

        User user = User.builder()
                .name("Joseph")
                .email("joseph@gmail.com")
                .build();

        when(userRepository.findByEmail("joseph@gmail.com"))
                .thenReturn(Optional.of(user));

        when(eventInventoryRepository.findByEventIdWithLock(1L))
                .thenReturn(Optional.empty());

        // Act & Assert

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> bookingService.bookEvent(request)
        );

        assertEquals(
                "Inventory not found for event: 1",
                exception.getMessage()
        );

        verify(userRepository).findByEmail("joseph@gmail.com");
        verify(eventInventoryRepository).findByEventIdWithLock(1L);

        verify(bookingRepository, never()).save(any());
        verify(eventInventoryRepository, never()).save(any());
    }

    @Test
    void bookEvent_shouldThrowWhenSeatsAreInsufficient() {

        // Arrange

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "joseph@gmail.com",
                        null
                )
        );
        SecurityContextHolder.setContext(context);

        BookingRequest request = new BookingRequest();
        request.setEventId(1L);
        request.setQuantity(5);

        User user = User.builder()
                .name("Joseph")
                .email("joseph@gmail.com")
                .build();

        EventInventory inventory = new EventInventory();
        inventory.setEventId(1L);
        inventory.setAvailableSeats(2);

        when(userRepository.findByEmail("joseph@gmail.com"))
                .thenReturn(Optional.of(user));

        when(eventInventoryRepository.findByEventIdWithLock(1L))
                .thenReturn(Optional.of(inventory));

        // Act & Assert

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> bookingService.bookEvent(request)
        );

        assertEquals("Out of seats", exception.getMessage());

        verify(userRepository).findByEmail("joseph@gmail.com");
        verify(eventInventoryRepository).findByEventIdWithLock(1L);

        verify(eventInventoryRepository, never()).save(any());
        verify(bookingRepository, never()).save(any());
    }

}
