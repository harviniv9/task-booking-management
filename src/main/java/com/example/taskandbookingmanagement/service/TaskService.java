package com.example.taskandbookingmanagement.service;

import com.example.taskandbookingmanagement.dto.TaskCreateRequest;
import com.example.taskandbookingmanagement.dto.TaskDecisionRequest;
import com.example.taskandbookingmanagement.dto.TaskResponse;
import com.example.taskandbookingmanagement.model.*;
import com.example.taskandbookingmanagement.repository.TaskRepository;
import com.example.taskandbookingmanagement.repository.UserRepository;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public TaskService(TaskRepository taskRepository,
                       UserRepository userRepository,
                       NotificationService notificationService) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    public TaskResponse createTask(TaskCreateRequest req) {
        User assigned = userRepository.findById(req.getAssignedUserId())
                .orElseThrow(() -> new IllegalArgumentException("Assigned user not found"));

        User createdBy = getCurrentUserEntity();

        Task t = new Task();
        t.setTitle(req.getTitle());
        t.setDescription(req.getDescription());
        t.setTaskDateTime(req.getTaskDateTime());
        t.setPriority(req.getPriority());
        t.setAssignedUser(assigned);
        t.setCreatedBy(createdBy);
        t.setStatus(TaskStatus.PENDING);

        Task saved = taskRepository.save(t);

        // ✅ Notify assigned user that a task was created for them
        notificationService.notifyTaskCreated(saved);

        return toResponse(saved);
    }

    public List<TaskResponse> listTasks(TaskStatus status, String sortBy, String sortDir) {
        Sort sort = buildSort(sortBy, sortDir);

        List<Task> tasks;
        if (status != null) {
            tasks = taskRepository.findByStatus(status, sort);
        } else {
            tasks = taskRepository.findAll(sort);
        }

        return tasks.stream().map(this::toResponse).toList();
    }

    public TaskResponse decide(Long taskId, TaskDecisionRequest.Decision decision) {
        // ✅ Manager OR Admin can approve/reject
        User actor = getCurrentUserEntity();
        if (actor.getRole() != Role.MANAGER && actor.getRole() != Role.ADMIN) {
            throw new SecurityException("Only MANAGER or ADMIN can approve/reject tasks");
        }

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        if (task.getStatus() != TaskStatus.PENDING) {
            throw new IllegalStateException("Only PENDING tasks can be approved/rejected");
        }

        TaskStatus newStatus = (decision == TaskDecisionRequest.Decision.APPROVE)
                ? TaskStatus.APPROVED
                : TaskStatus.REJECTED;

        task.setStatus(newStatus);
        task.setDecisionBy(actor);
        task.setDecisionAt(LocalDateTime.now());

        Task saved = taskRepository.save(task);

        // ✅ Notify recipients (Assigned always, Creator if different)
        if (decision == TaskDecisionRequest.Decision.APPROVE) {
            notificationService.notifyTaskApproved(saved, actor);
        } else {
            notificationService.notifyTaskRejected(saved, actor);
        }

        return toResponse(saved);
    }

    private Sort buildSort(String sortBy, String sortDir) {
        String property = (sortBy == null || sortBy.isBlank()) ? "taskDateTime" : sortBy;
        Sort.Direction dir = "desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        return Sort.by(dir, property);
    }

    private User getCurrentUserEntity() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Logged-in user not found in DB: " + username));
    }

    private TaskResponse toResponse(Task t) {
        TaskResponse r = new TaskResponse();
        r.setId(t.getId());
        r.setTitle(t.getTitle());
        r.setDescription(t.getDescription());
        r.setStatus(t.getStatus());
        r.setPriority(t.getPriority());
        r.setTaskDateTime(t.getTaskDateTime());

        r.setAssignedUserId(t.getAssignedUser().getId());
        r.setAssignedUsername(t.getAssignedUser().getUsername());

        r.setCreatedByUserId(t.getCreatedBy().getId());
        r.setCreatedByUsername(t.getCreatedBy().getUsername());

        r.setCreatedAt(t.getCreatedAt());
        r.setUpdatedAt(t.getUpdatedAt());

        r.setDecisionAt(t.getDecisionAt());
        r.setDecisionByUsername(t.getDecisionBy() == null ? null : t.getDecisionBy().getUsername());

        return r;
    }
}
