package com.AfriPass.afripass.DTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingConfirmedEvent implements Serializable {

    private Long bookingId;

    private List<String> ticketNumber;

    private String eventName;

    private LocalDateTime eventDate;

    private String holderName;

    private String email;

    private BigDecimal amount;

    private LocalDateTime confirmedAt;

}
