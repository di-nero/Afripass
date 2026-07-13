package com.AfriPass.afripass.Services;

import com.AfriPass.afripass.Config.RabbitMQConfig;
import com.AfriPass.afripass.DTOs.BookingConfirmedEvent;
import com.AfriPass.afripass.DTOs.EventDetails;
import com.AfriPass.afripass.Model.Booking;
import com.AfriPass.afripass.Model.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class BookingEventPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private BookingEventPublisher bookingEventPublisher;

    @Test
    void publishBookingConfirmed_shouldSendBookingEvent() {

        // Arrange
        Booking booking = new Booking();
        booking.setId(1L);

        User user = User.builder()
                .name("Joseph")
                .email("joseph@gmail.com")
                .build();

        EventDetails eventDetails = new EventDetails();
        eventDetails.setEventName("Spring Boot Conference");
        eventDetails.setDate(LocalDateTime.of(2026, 7, 20, 10, 0));

        List<String> ticketNumbers = List.of("A123", "A124");

        BigDecimal totalAmount = new BigDecimal("5000");

        // Act
        bookingEventPublisher.publishBookingConfirmed(
                booking,
                user,
                eventDetails,
                ticketNumbers,
                totalAmount
        );

        // Assert
        ArgumentCaptor<BookingConfirmedEvent> captor =
                ArgumentCaptor.forClass(BookingConfirmedEvent.class);

        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.EXCHANGE),
                eq(RabbitMQConfig.ROUTING_KEY),
                captor.capture()
        );

        BookingConfirmedEvent sentEvent = captor.getValue();

        Assertions.assertEquals(booking.getId(), sentEvent.getBookingId());
        Assertions.assertEquals(user.getName(), sentEvent.getHolderName());
        Assertions.assertEquals(user.getEmail(), sentEvent.getEmail());
        Assertions.assertEquals(eventDetails.getEventName(), sentEvent.getEventName());
        Assertions.assertEquals(eventDetails.getDate(), sentEvent.getEventDate());
        Assertions.assertEquals(ticketNumbers, sentEvent.getTicketNumber());
        Assertions.assertEquals(totalAmount, sentEvent.getAmount());
        Assertions.assertNotNull(sentEvent.getConfirmedAt());
    }
}
