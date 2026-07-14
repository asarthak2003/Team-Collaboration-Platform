package com.sarthak.teamcollab.controller;

import org.springframework.web.bind.annotation.RestController;

import com.sarthak.teamcollab.repository.UserRepository;
import com.sarthak.teamcollab.service.TaskService;

@RestController
public class TaskController {
    private final TaskService taskService;
    private final UserRepository userRepository;

    public TaskController(TaskService taskService, UserRepository userRepository) {
        this.taskService = taskService;
        this.userRepository = userRepository;
    }

}
