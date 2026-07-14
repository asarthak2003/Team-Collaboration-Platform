package com.sarthak.teamcollab.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.sarthak.teamcollab.model.Project;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByDeletedFalse();
    Optional<Project> findByIdAndDeletedFalse(Long id);
}
