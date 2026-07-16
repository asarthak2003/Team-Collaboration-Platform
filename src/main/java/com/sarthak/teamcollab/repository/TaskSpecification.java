package com.sarthak.teamcollab.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.springframework.data.jpa.domain.Specification;

import com.sarthak.teamcollab.model.Task;
import com.sarthak.teamcollab.model.TaskPriority;
import com.sarthak.teamcollab.model.TaskStatus;

public class TaskSpecification {
    public static Specification<Task> filterTasks(String keyword, TaskStatus status, TaskPriority priority,
            Long assigneeId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
        }
    }
}}