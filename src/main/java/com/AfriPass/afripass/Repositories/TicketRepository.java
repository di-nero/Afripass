package com.AfriPass.afripass.Repositories;

import com.AfriPass.afripass.Model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket , Long> {
    List<Ticket> findByBookingUserId(Long userId);
}
