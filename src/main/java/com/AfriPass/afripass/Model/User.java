package com.AfriPass.afripass.Model;


import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@EqualsAndHashCode(of = "id")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Email
    @Column(unique = true)
    private String email;


    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    @CreationTimestamp
    private LocalDateTime createdAt;

}
