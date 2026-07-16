package com.sarthak.teamcollab.repository;

import com.sarthak.teamcollab.model.User;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class UserSpecification {

    public static Specification<User> filterUsers(String keyword, String roleName) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (keyword != null && !keyword.trim().isEmpty()) {
                String likePattern = "%" + keyword.toLowerCase() + "%";
                Predicate nameLike = cb.like(cb.lower(root.get("name")), likePattern);
                Predicate emailLike = cb.like(cb.lower(root.get("email")), likePattern);
                predicates.add(cb.or(nameLike, emailLike));
            }

            if (roleName != null && !roleName.trim().isEmpty()) {
                String finalRoleName = roleName.toUpperCase();
                if (!finalRoleName.startsWith("ROLE_")) {
                    finalRoleName = "ROLE_" + finalRoleName;
                }
                predicates.add(cb.equal(root.get("role").get("name"), finalRoleName));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
