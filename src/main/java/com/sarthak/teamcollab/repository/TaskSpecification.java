package com.sarthak.teamcollab.repository;

import com.sarthak.teamcollab.model.Task;
import com.sarthak.teamcollab.model.TaskPriority;
import com.sarthak.teamcollab.model.TaskStatus;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class TaskSpecification {

    public static Specification<Task> filterTasks(String keyword, TaskStatus status, TaskPriority priority,
            Long assigneeId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("deleted"), false));

            if (keyword != null && !keyword.trim().isEmpty()) {
                String likePattern = "%" + keyword.toLowerCase() + "%";
                Predicate titleLike = cb.like(cb.lower(root.get("title")), likePattern);
                Predicate descLike = cb.like(cb.lower(root.get("description")), likePattern);
                predicates.add(cb.or(titleLike, descLike));
            }

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            if (priority != null) {
                predicates.add(cb.equal(root.get("priority"), priority));
            }

            if (assigneeId != null) {
                predicates.add(cb.equal(root.get("assignedUser").get("id"), assigneeId));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
