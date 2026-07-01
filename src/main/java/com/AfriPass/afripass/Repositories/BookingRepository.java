package com.AfriPass.afripass.Repositories;

import com.AfriPass.afripass.Model.Booking;
import com.AfriPass.afripass.Model.BookingStatus;
import com.AfriPass.afripass.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking , Long> {

    List<Booking> findByBookingStatusAndExpiresAtBefore(BookingStatus status, LocalDateTime time);
    Optional<Booking> findById(Long id);
}
