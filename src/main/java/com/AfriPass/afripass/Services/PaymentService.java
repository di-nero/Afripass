package com.AfriPass.afripass.Services;


import com.AfriPass.afripass.DTOs.EventDetails;
import com.AfriPass.afripass.DTOs.PaymentRequest;
import com.AfriPass.afripass.DTOs.TicketResponse;
import com.AfriPass.afripass.Enums.PaymentStatus;
import com.AfriPass.afripass.Exception.*;
import com.AfriPass.afripass.Model.Booking;
import com.AfriPass.afripass.Enums.BookingStatus;
import com.AfriPass.afripass.Model.Ticket;
import com.AfriPass.afripass.Model.User;
import com.AfriPass.afripass.Repositories.BookingRepository;
import com.AfriPass.afripass.Repositories.TicketRepository;
import com.AfriPass.afripass.Repositories.UserRepository;
import com.stripe.exception.CardException;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final TicketRepository ticketRepository;
    private final EventService eventService;
    private final EventPublisher eventPublisher;

    @Transactional
    public List<TicketResponse> processPayment(PaymentRequest request) throws StripeException {

        //know who is making the payment
        User user = getAuthenticatedUser();

        //get the booking
        Booking booking = getValidBooking(request.getBookingId() , user);

        //so we can pull event name and event date for the ticket and the publisher
        EventDetails eventDetails = eventService.getEventById(booking.getEventId());

        //per ticket amount calculation
        //total amount / quantity = price per ticket
        BigDecimal perTicketAmount = eventDetails.getPrice();
        BigDecimal totalAmount = calculateTotalAmount(perTicketAmount , booking.getQuantity());

        //stripe
        PaymentIntent intent;
        PaymentIntentCreateParams params;
        try {
            //convert the amount to cents
            long amountInCents = convertAmountToCent(totalAmount);

            params = PaymentIntentCreateParams.builder()
                    .setAmount(amountInCents)
                    .setCurrency("usd")
                    .setPaymentMethod(request.getPaymentMethodId())
                    .setConfirm(true)
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                    .setEnabled(true)
                                    .setAllowRedirects(
                                            PaymentIntentCreateParams
                                                    .AutomaticPaymentMethods
                                                    .AllowRedirects.NEVER
                                    )
                                    .build()
                    )
                    .build();
            intent = createPaymentIntent(params);
        } catch (CardException e) {
            //if card declines booking still stay pending user can retry payment within 15 mions
            log.warn("Card decline for bookingId={}: {}", booking.getId(), e.getMessage());
            throw new PaymentDeclinedException("Payment decline: " + e.getMessage());
        } catch (StripeException e) {
            //stripe infrastructure issue at this moment @Retry would have tried 3 times
            //then the @CircuitBreaker open if this keep happening
            log.error("Stripe error for bookingId={}: {}", booking.getId(), e.getMessage());
            throw new PaymentServiceUnavailableException("Payment failed: " + e.getMessage());
        }

        //check if payment really succeeded
        validatePaymentSucceeded(intent);

        //if payment confirms then we update the booking status
        booking.setBookingStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(booking);
        log.info("Booking {} confirmed for user {}", booking.getId(), user.getEmail());

        List<Ticket> tickets = generateTicket(booking , user , eventDetails , perTicketAmount);
        ticketRepository.saveAll(tickets);
        log.info("Generated {} tickets for bookingId={}", tickets.size(), booking.getId());

        List<String> ticketNumbers = tickets.stream()
                .map(Ticket::getTicketNumber)
                .toList();

        eventPublisher.publishBookingConfirmed(booking, user, eventDetails, ticketNumbers, totalAmount);

        return tickets.stream().map(
                        t -> new TicketResponse(t.getTicketNumber(),
                                t.getEventName(),
                                t.getEventDate(), t.getHolderName(),
                                t.getAmount(),
                                t.getBookingStatus().toString(),
                                t.getIssuedAt())
                )
                .toList();
    }

    @Retry(name = "stripePayment")
    @CircuitBreaker(name = "stripePayment", fallbackMethod = "paymentFallback")
    public PaymentIntent createPaymentIntent(PaymentIntentCreateParams params) throws StripeException {
        return PaymentIntent.create(params);
    }

    public PaymentIntent paymentFallback(PaymentIntentCreateParams params, Throwable t) {
        log.error("Payment circuit open or retries exhausted: {}", t.getMessage());
        throw new RuntimeException("Payment service currently unavailable. " +
                "Your booking is held. please try again shortly");
    }

    private User getAuthenticatedUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    private Booking getValidBooking(Long bookingId , User user) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + bookingId));

        if (!booking.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("This booking does not belong to you");
        }

        //check if the payment is confirmed or expired
        if (booking.getBookingStatus() != BookingStatus.PENDING)
            throw new ConflictException("This is no longer a pending payment");
        return booking;
    }

    private List<Ticket> generateTicket(Booking booking , User user , EventDetails eventDetails , BigDecimal amount){
        List<Ticket> tickets = new ArrayList<>();
        for (int i = 0; i < booking.getQuantity(); i++) {

            Ticket ticket = new Ticket();
            ticket.setBooking(booking);
            ticket.setTicketNumber("TKT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            ticket.setHolderName(user.getName());
            ticket.setEventName(eventDetails.getEventName());
            ticket.setEventDate(eventDetails.getDate());
            ticket.setAmount(amount);
            ticket.setBookingStatus(BookingStatus.CONFIRMED);
            ticket.setIssuedAt(LocalDateTime.now());
            tickets.add(ticket);
        }
        return tickets;
    }

    private BigDecimal calculateTotalAmount(BigDecimal perTicketAmount , int quantity){
        return perTicketAmount.multiply(BigDecimal.valueOf(quantity));
    }

    private long convertAmountToCent(BigDecimal totalAmount){
        return totalAmount.multiply(BigDecimal.valueOf(100)).longValue();
    }

    public void validatePaymentSucceeded(PaymentIntent intent){
        if (!PaymentStatus.SUCCEEDED.getValue().equals(intent.getStatus())) {
            throw new PaymentServiceUnavailableException("Payment did not succeed , status: " + intent.getStatus());
        }
    }

}
