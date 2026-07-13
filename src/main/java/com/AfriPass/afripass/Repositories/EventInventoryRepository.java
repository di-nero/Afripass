package com.AfriPass.afripass.Repositories;

import com.AfriPass.afripass.Model.EventInventory;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;


public interface EventInventoryRepository extends JpaRepository<EventInventory, Long> {
    // Locks the inventory row to prevent concurrent seat updates.
    boolean existsByEventId(Long eventId);

    Optional<EventInventory> findByEventId(Long eventId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM EventInventory e WHERE e.eventId = :eventId")
    Optional<EventInventory> findByEventIdWithLock(@Param("eventId") Long eventId);
}
