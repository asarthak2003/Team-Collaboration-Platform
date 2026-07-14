package com.sarthak.teamcollab.repository;

import com.sarthak.teamcollab.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByProjectIdAndDeletedFalse(Long projectId);

    List<Task> findByAssignedUserIdAndDeletedFalse(Long userId);

    Optional<Task> findByIdAndDeletedFalse(Long id);
}
