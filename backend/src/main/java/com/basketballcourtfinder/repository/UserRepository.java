package com.basketballcourtfinder.repository;

import com.basketballcourtfinder.dto.UserProjection;
import com.basketballcourtfinder.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Optional<User> findByDisplayName(String displayName);

    @Query("SELECT u.id AS id, u.email AS email, u.displayName AS displayName FROM User u WHERE u.id = :userId")
    Optional<UserProjection> findProjectedById(Long userId);
}
