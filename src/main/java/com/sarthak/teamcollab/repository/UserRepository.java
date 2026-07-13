package com.sarthak.teamcollab.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.sarthak.teamcollab.model.User;
import org.springframework.stereotype.Repository;

/*
Extends JpaRepository<User, Long> to handle CRUD operations on the users table.
Defines findByEmail(String email) to fetch user credentials during login.
Defines existsByEmail(String email) to verify that a new registration request is not using an email that is already registered.
*/

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
