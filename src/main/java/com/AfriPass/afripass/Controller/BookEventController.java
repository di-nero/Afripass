package com.AfriPass.afripass.Controller;


import com.AfriPass.afripass.DTOs.BookingRequest;
import com.AfriPass.afripass.DTOs.BookingResponse;
import com.AfriPass.afripass.Services.BookingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bookings")
public class BookEventController {

    @Autowired
    private BookingService bookingService;

    @PostMapping
    public ResponseEntity<BookingResponse> bookEvent(@Valid @RequestBody BookingRequest request){

        return ResponseEntity.ok(bookingService.bookEvent(request));

    }

}
