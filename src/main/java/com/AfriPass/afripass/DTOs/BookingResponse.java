package com.AfriPass.afripass.DTOs;


import com.AfriPass.afripass.Enums.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingResponse {

    private Long bookingId;

    private Long eventId;

    private BookingStatus bookingStatus;

    private LocalDateTime createdAt;

}
