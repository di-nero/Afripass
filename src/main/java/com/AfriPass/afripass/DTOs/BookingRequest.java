package com.AfriPass.afripass.DTOs;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingRequest {

    @NotNull(message = "Event ID is required")
    private Long eventId;

    @Min(value = 1, message = "Quantity must be at least 1")
    private int quantity;

}
