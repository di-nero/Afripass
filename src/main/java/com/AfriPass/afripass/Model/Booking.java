package com.AfriPass.afripass.Model;

import com.AfriPass.afripass.Enums.BookingStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "booking")
@EqualsAndHashCode(of = "id") //find meaning
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private Long eventId;

    private BookingStatus bookingStatus;

    @CreationTimestamp
    private LocalDateTime createsAt;

    private int quantity;

    private LocalDateTime expiresAt;

}

//one user can book for 5 people that why i introduced quantity
// since a user can book for multiple people it should generate multiple tickets too and
// reflect the amount of each ticket in the notification