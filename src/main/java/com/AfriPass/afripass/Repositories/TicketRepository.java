package com.AfriPass.afripass.Repositories;

import com.AfriPass.afripass.Model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByBookingUserId(Long userId);
}
