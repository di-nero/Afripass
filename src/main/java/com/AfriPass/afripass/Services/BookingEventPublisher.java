package com.AfriPass.afripass.Services;


import com.AfriPass.afripass.Config.RabbitMQConfig;
import com.AfriPass.afripass.DTOs.BookingConfirmedEvent;
import com.AfriPass.afripass.DTOs.EventDetails;
import com.AfriPass.afripass.Model.Booking;
import com.AfriPass.afripass.Model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookingEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Async("taskExecutor")
    public void publishBookingConfirmed(Booking booking, User user, EventDetails eventDetails, List<String> ticketNumbers, BigDecimal totalAmount) {

        BookingConfirmedEvent event = new BookingConfirmedEvent();
        event.setBookingId(booking.getId());
        event.setTicketNumber(ticketNumbers);
        event.setEventName(eventDetails.getEventName());
        event.setAmount(totalAmount);
        event.setHolderName(user.getName());
        event.setEmail(user.getEmail());
        event.setConfirmedAt(LocalDateTime.now());
        event.setEventDate(eventDetails.getDate());

        log.info("Event = {}", event);

        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE,
                    RabbitMQConfig.ROUTING_KEY,
                    event
            );

            log.info("Sent to RabbitMQ");

        } catch (Exception e) {
            log.error("Failed to send RabbitMQ message", e);
        }
    }
}
