package com.AfriPass.afripass.Services;

import com.AfriPass.afripass.DTOs.EventDetails;
import com.AfriPass.afripass.DTOs.PaymentRequest;
import com.AfriPass.afripass.DTOs.TicketResponse;
import com.AfriPass.afripass.Enums.BookingStatus;
import com.AfriPass.afripass.Exception.*;
import com.AfriPass.afripass.Model.Booking;
import com.AfriPass.afripass.Model.User;
import com.AfriPass.afripass.Repositories.BookingRepository;
import com.AfriPass.afripass.Repositories.TicketRepository;
import com.AfriPass.afripass.Repositories.UserRepository;
import com.stripe.exception.CardException;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private EventService eventService;

    @Mock
    private BookingEventPublisher bookingEventPublisher;

    @Spy
    @InjectMocks
    private PaymentService paymentService;

    @Test
    void processPayment_shouldProcessPaymentSuccessfully() throws StripeException {

        // Arrange

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "joseph@gmail.com",
                        null
                )
        );
        SecurityContextHolder.setContext(context);

        PaymentRequest request = new PaymentRequest();
        request.setBookingId(1L);
        request.setPaymentMethodId("pm_card_visa");

        User user = User.builder()
                .id(1L)
                .name("Joseph")
                .email("joseph@gmail.com")
                .build();

        Booking booking = Booking.builder()
                .id(1L)
                .eventId(100L)
                .quantity(2)
                .bookingStatus(BookingStatus.PENDING)
                .user(user)
                .build();

        EventDetails event = new EventDetails();
        event.setId(100L);
        event.setEventName("Spring Boot Conference");
        event.setDate(LocalDateTime.of(2026, 7, 20, 10, 0));
        event.setPrice(new BigDecimal("5000"));

        PaymentIntent paymentIntent = mock(PaymentIntent.class);

        when(paymentIntent.getStatus())
                .thenReturn("succeeded");

        when(userRepository.findByEmail("joseph@gmail.com"))
                .thenReturn(Optional.of(user));

        when(bookingRepository.findById(1L))
                .thenReturn(Optional.of(booking));

        when(eventService.getEventById(100L))
                .thenReturn(event);

        doReturn(paymentIntent)
                .when(paymentService)
                .createPaymentIntent(any());

        when(bookingRepository.save(any(Booking.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(ticketRepository.saveAll(anyList()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act

        List<TicketResponse> response = paymentService.processPayment(request);

        // Assert

        assertNotNull(response);
        assertEquals(2, response.size());

        assertEquals(
                BookingStatus.CONFIRMED.toString(),
                response.get(0).getBookingStatus()
        );

        assertEquals(
                "Spring Boot Conference",
                response.get(0).getEventName()
        );

        assertEquals(
                new BigDecimal("5000"),
                response.get(0).getAmount()
        );

        verify(userRepository).findByEmail("joseph@gmail.com");
        verify(bookingRepository).findById(1L);
        verify(eventService).getEventById(100L);
        verify(bookingRepository).save(any(Booking.class));
        verify(ticketRepository).saveAll(anyList());

        verify(bookingEventPublisher).publishBookingConfirmed(
                eq(booking),
                eq(user),
                eq(event),
                anyList(),
                eq(new BigDecimal("10000"))
        );
    }

    @Test
    void processPayment_shouldThrowWhenUserNotFound() throws StripeException {

        // Arrange

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "joseph@gmail.com",
                        null
                )
        );
        SecurityContextHolder.setContext(context);

        PaymentRequest request = new PaymentRequest();
        request.setBookingId(1L);
        request.setPaymentMethodId("pm_card_visa");

        when(userRepository.findByEmail("joseph@gmail.com"))
                .thenReturn(Optional.empty());

        // Act & Assert

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> paymentService.processPayment(request)
        );

        assertEquals(
                "User not found: joseph@gmail.com",
                exception.getMessage()
        );

        verify(userRepository).findByEmail("joseph@gmail.com");

        verifyNoInteractions(
                bookingRepository,
                ticketRepository,
                eventService,
                bookingEventPublisher
        );
    }

    @Test
    void processPayment_shouldThrowWhenBookingNotFound() throws StripeException {

        // Arrange

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "joseph@gmail.com",
                        null
                )
        );
        SecurityContextHolder.setContext(context);

        PaymentRequest request = new PaymentRequest();
        request.setBookingId(1L);
        request.setPaymentMethodId("pm_card_visa");

        User user = User.builder()
                .id(1L)
                .name("Joseph")
                .email("joseph@gmail.com")
                .build();

        when(userRepository.findByEmail("joseph@gmail.com"))
                .thenReturn(Optional.of(user));

        when(bookingRepository.findById(1L))
                .thenReturn(Optional.empty());

        // Act & Assert

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> paymentService.processPayment(request)
        );

        assertEquals(
                "Booking not found: 1",
                exception.getMessage()
        );

        verify(userRepository).findByEmail("joseph@gmail.com");
        verify(bookingRepository).findById(1L);

        verifyNoInteractions(
                ticketRepository,
                eventService,
                bookingEventPublisher
        );
    }

    @Test
    void processPayment_shouldThrowWhenBookingBelongsToAnotherUser() throws StripeException {

        // Arrange

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "joseph@gmail.com",
                        null
                )
        );
        SecurityContextHolder.setContext(context);

        PaymentRequest request = new PaymentRequest();
        request.setBookingId(1L);
        request.setPaymentMethodId("pm_card_visa");

        User authenticatedUser = User.builder()
                .id(1L)
                .name("Joseph")
                .email("joseph@gmail.com")
                .build();

        User anotherUser = User.builder()
                .id(2L)
                .name("David")
                .email("david@gmail.com")
                .build();

        Booking booking = Booking.builder()
                .id(1L)
                .eventId(100L)
                .quantity(2)
                .bookingStatus(BookingStatus.PENDING)
                .user(anotherUser)
                .build();

        when(userRepository.findByEmail("joseph@gmail.com"))
                .thenReturn(Optional.of(authenticatedUser));

        when(bookingRepository.findById(1L))
                .thenReturn(Optional.of(booking));

        // Act & Assert

        ForbiddenException exception = assertThrows(
                ForbiddenException.class,
                () -> paymentService.processPayment(request)
        );

        assertEquals(
                "This booking does not belong to you",
                exception.getMessage()
        );

        verify(userRepository).findByEmail("joseph@gmail.com");
        verify(bookingRepository).findById(1L);

        verifyNoInteractions(
                ticketRepository,
                eventService,
                bookingEventPublisher
        );
    }

    @Test
    void processPayment_shouldThrowWhenBookingIsNotPending() throws StripeException {

        // Arrange

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "joseph@gmail.com",
                        null
                )
        );
        SecurityContextHolder.setContext(context);

        PaymentRequest request = new PaymentRequest();
        request.setBookingId(1L);
        request.setPaymentMethodId("pm_card_visa");

        User user = User.builder()
                .id(1L)
                .name("Joseph")
                .email("joseph@gmail.com")
                .build();

        Booking booking = Booking.builder()
                .id(1L)
                .eventId(100L)
                .quantity(2)
                .bookingStatus(BookingStatus.CONFIRMED) // Not pending
                .user(user)
                .build();

        when(userRepository.findByEmail("joseph@gmail.com"))
                .thenReturn(Optional.of(user));

        when(bookingRepository.findById(1L))
                .thenReturn(Optional.of(booking));

        // Act & Assert

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> paymentService.processPayment(request)
        );

        assertEquals(
                "This is no longer a pending payment",
                exception.getMessage()
        );

        verify(userRepository).findByEmail("joseph@gmail.com");
        verify(bookingRepository).findById(1L);

        verifyNoInteractions(
                ticketRepository,
                eventService,
                bookingEventPublisher
        );
    }

    @Test
    void processPayment_shouldThrowWhenCardIsDeclined() throws StripeException {

        // Arrange

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "joseph@gmail.com",
                        null
                )
        );
        SecurityContextHolder.setContext(context);

        PaymentRequest request = new PaymentRequest();
        request.setBookingId(1L);
        request.setPaymentMethodId("pm_card_declined");

        User user = User.builder()
                .id(1L)
                .name("Joseph")
                .email("joseph@gmail.com")
                .build();

        Booking booking = Booking.builder()
                .id(1L)
                .eventId(100L)
                .quantity(2)
                .bookingStatus(BookingStatus.PENDING)
                .user(user)
                .build();

        EventDetails event = new EventDetails();
        event.setId(100L);
        event.setEventName("Spring Boot Conference");
        event.setPrice(new BigDecimal("5000"));
        event.setDate(LocalDateTime.of(2026, 7, 20, 10, 0));

        when(userRepository.findByEmail("joseph@gmail.com"))
                .thenReturn(Optional.of(user));

        when(bookingRepository.findById(1L))
                .thenReturn(Optional.of(booking));

        when(eventService.getEventById(100L))
                .thenReturn(event);

        CardException cardException = mock(CardException.class);

        when(cardException.getMessage())
                .thenReturn("Your card was declined.");

        doThrow(cardException)
                .when(paymentService)
                .createPaymentIntent(any());

        // Act & Assert

        PaymentDeclinedException exception = assertThrows(
                PaymentDeclinedException.class,
                () -> paymentService.processPayment(request)
        );

        assertEquals(
                "Payment decline: Your card was declined.",
                exception.getMessage()
        );

        verify(userRepository).findByEmail("joseph@gmail.com");
        verify(bookingRepository).findById(1L);
        verify(eventService).getEventById(100L);

        verifyNoInteractions(ticketRepository, bookingEventPublisher);
    }

    @Test
    void processPayment_shouldThrowWhenStripeFails() throws StripeException {

        // Arrange

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "joseph@gmail.com",
                        null
                )
        );
        SecurityContextHolder.setContext(context);

        PaymentRequest request = new PaymentRequest();
        request.setBookingId(1L);
        request.setPaymentMethodId("pm_card_visa");

        User user = User.builder()
                .id(1L)
                .name("Joseph")
                .email("joseph@gmail.com")
                .build();

        Booking booking = Booking.builder()
                .id(1L)
                .eventId(100L)
                .quantity(2)
                .bookingStatus(BookingStatus.PENDING)
                .user(user)
                .build();

        EventDetails event = new EventDetails();
        event.setId(100L);
        event.setEventName("Spring Boot Conference");
        event.setPrice(new BigDecimal("5000"));
        event.setDate(LocalDateTime.of(2026, 7, 20, 10, 0));

        when(userRepository.findByEmail("joseph@gmail.com"))
                .thenReturn(Optional.of(user));

        when(bookingRepository.findById(1L))
                .thenReturn(Optional.of(booking));

        when(eventService.getEventById(100L))
                .thenReturn(event);

        StripeException stripeException = mock(StripeException.class);

        when(stripeException.getMessage())
                .thenReturn("Stripe service unavailable");

        doThrow(stripeException)
                .when(paymentService)
                .createPaymentIntent(any());

        // Act & Assert

        PaymentServiceUnavailableException exception = assertThrows(
                PaymentServiceUnavailableException.class,
                () -> paymentService.processPayment(request)
        );

        assertEquals(
                "Payment failed: Stripe service unavailable",
                exception.getMessage()
        );

        verify(userRepository).findByEmail("joseph@gmail.com");
        verify(bookingRepository).findById(1L);
        verify(eventService).getEventById(100L);

        verifyNoInteractions(ticketRepository, bookingEventPublisher);
    }

    @Test
    void processPayment_shouldThrowWhenPaymentStatusIsNotSucceeded() throws StripeException {

        // Arrange

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "joseph@gmail.com",
                        null
                )
        );
        SecurityContextHolder.setContext(context);

        PaymentRequest request = new PaymentRequest();
        request.setBookingId(1L);
        request.setPaymentMethodId("pm_card_visa");

        User user = User.builder()
                .id(1L)
                .name("Joseph")
                .email("joseph@gmail.com")
                .build();

        Booking booking = Booking.builder()
                .id(1L)
                .eventId(100L)
                .quantity(2)
                .bookingStatus(BookingStatus.PENDING)
                .user(user)
                .build();

        EventDetails event = new EventDetails();
        event.setId(100L);
        event.setEventName("Spring Boot Conference");
        event.setPrice(new BigDecimal("5000"));
        event.setDate(LocalDateTime.of(2026, 7, 20, 10, 0));

        PaymentIntent paymentIntent = mock(PaymentIntent.class);

        when(paymentIntent.getStatus())
                .thenReturn("processing");

        when(userRepository.findByEmail("joseph@gmail.com"))
                .thenReturn(Optional.of(user));

        when(bookingRepository.findById(1L))
                .thenReturn(Optional.of(booking));

        when(eventService.getEventById(100L))
                .thenReturn(event);

        doReturn(paymentIntent)
                .when(paymentService)
                .createPaymentIntent(any());

        // Act & Assert

        PaymentServiceUnavailableException exception = assertThrows(
                PaymentServiceUnavailableException.class,
                () -> paymentService.processPayment(request)
        );

        assertEquals(
                "Payment did not succeed , status: processing",
                exception.getMessage()
        );

        verify(userRepository).findByEmail("joseph@gmail.com");
        verify(bookingRepository).findById(1L);
        verify(eventService).getEventById(100L);

        verifyNoInteractions(ticketRepository, bookingEventPublisher);
    }

    @Test
    void paymentFallback_shouldThrowPaymentServiceUnavailableException() {

        // Arrange

        PaymentIntentCreateParams params =
                PaymentIntentCreateParams.builder()
                        .setAmount(1000L)
                        .setCurrency("usd")
                        .build();

        Throwable throwable = new RuntimeException("Stripe unavailable");

        // Act & Assert

        PaymentServiceUnavailableException exception = assertThrows(
                PaymentServiceUnavailableException.class,
                () -> paymentService.paymentFallback(params, throwable)
        );

        assertEquals(
                "Payment service currently unavailable. Your booking is held. please try again shortly",
                exception.getMessage()
        );
    }



}
