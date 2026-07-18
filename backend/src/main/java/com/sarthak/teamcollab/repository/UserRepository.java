package com.sarthak.teamcollab.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import com.sarthak.teamcollab.model.User;

/*
Extends JpaRepository<User, Long> to handle CRUD operations on the users table.
Defines findByEmail(String email) to fetch user credentials during login.
Defines existsByEmail(String email) to verify that a new registration request is not using an email that is already registered.
*/

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
