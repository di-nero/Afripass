package com.AfriPass.afripass.DTOs;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventDetails implements Serializable {

    private Long id;

    private String eventName;

    private LocalDateTime date;

    private String venue;

    private String city;

    private BigDecimal price;

    private int availableSeats;

}
