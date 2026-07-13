package com.sarthak.teamcollab.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.sarthak.teamcollab.model.Role;
import org.springframework.stereotype.Repository;

/* 
Extends JpaRepository<Role, Long> to handle CRUD operations on the roles table.
Defines a custom query findByName(String name) to fetch a role entity from its string representation (like "ROLE_MEMBER").
*/

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);
}
