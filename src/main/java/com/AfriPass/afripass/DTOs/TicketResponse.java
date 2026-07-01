package com.AfriPass.afripass.DTOs;

import com.AfriPass.afripass.Model.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketResponse {

    private String ticketNumber;

    private String eventName;

    private LocalDateTime eventDate;

    private String holderName;

    private BigDecimal amount;

    private String bookingStatus;

    private LocalDateTime issuedAt;

}
