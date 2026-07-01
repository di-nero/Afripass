package com.AfriPass.afripass.Repositories;

import com.AfriPass.afripass.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User , Long> {
    Optional<User> findByEmail(String email);
}
