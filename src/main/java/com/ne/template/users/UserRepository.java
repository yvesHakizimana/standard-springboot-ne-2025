package com.ne.template.users;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    boolean existsByEmailOrPhoneNumberOrNationalId(String email, String phoneNumber, String nationalId);
    Optional<User> findByEmail(String email);

    @Override
    Optional<User> findById(UUID userId);
}
