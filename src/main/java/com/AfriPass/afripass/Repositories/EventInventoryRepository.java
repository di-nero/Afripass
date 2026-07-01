package com.AfriPass.afripass.Repositories;

import com.AfriPass.afripass.Model.EventInventory;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EventInventoryRepository extends JpaRepository<EventInventory , Long> {
    //pessimistic lock comes here
    boolean existsByEventId(Long eventId);
    Optional<EventInventory> findByEventId(Long eventId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM EventInventory e WHERE e.id = :eventId")
    EventInventory findByEventIdWithLock(@Param("eventId") Long id);
}
