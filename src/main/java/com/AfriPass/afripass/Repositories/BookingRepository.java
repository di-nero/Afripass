package com.AfriPass.afripass.Repositories;

import com.AfriPass.afripass.Enums.BookingStatus;
import com.AfriPass.afripass.Model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;


public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByBookingStatusAndExpiresAtBefore(BookingStatus status, LocalDateTime time);

}
