package com.AfriPass.afripass.Model;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "event_inventory")
@EqualsAndHashCode(of = "id")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EventInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private Long eventId;

    @Column(nullable = false)
    private int availableSeats;

}
