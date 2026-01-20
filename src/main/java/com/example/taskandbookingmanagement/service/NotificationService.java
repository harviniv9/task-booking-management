package com.example.taskandbookingmanagement.service;

import com.example.taskandbookingmanagement.model.Task;
import com.example.taskandbookingmanagement.model.User;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    public void notifyTaskCreated(Task task) {
        // Assigned user always gets notification
        notifyUser(
                task.getAssignedUser(),
                "TASK_CREATED",
                task,
                "A task was created and assigned to you."
        );
    }

    public void notifyTaskApproved(Task task, User decidedBy) {
        // Assigned user always gets notification
        notifyUser(
                task.getAssignedUser(),
                "TASK_APPROVED",
                task,
                "Task approved by " + decidedBy.getUsername() + "."
        );

        // Creator also gets it if different from assigned user
        notifyCreatorIfDifferent(task, "TASK_APPROVED", "Your task was approved by " + decidedBy.getUsername() + ".");
    }

    public void notifyTaskRejected(Task task, User decidedBy) {
        // Assigned user always gets notification
        notifyUser(
                task.getAssignedUser(),
                "TASK_REJECTED",
                task,
                "Task rejected by " + decidedBy.getUsername() + "."
        );

        // Creator also gets it if different from assigned user
        notifyCreatorIfDifferent(task, "TASK_REJECTED", "Your task was rejected by " + decidedBy.getUsername() + ".");
    }

    private void notifyCreatorIfDifferent(Task task, String event, String message) {
        if (task.getCreatedBy() != null
                && task.getAssignedUser() != null
                && !task.getCreatedBy().getId().equals(task.getAssignedUser().getId())) {

            notifyUser(task.getCreatedBy(), event, task, message);
        }
    }

    private void notifyUser(User recipient, String eventType, Task task, String message) {
        System.out.println("""
            [NOTIFICATION]
            Recipient: %s
            Event: %s

            Task:
            - ID: %d
            - Title: %s
            - Status: %s
            - Priority: %s
            - Task Date/Time: %s
            - Assigned To: %s
            - Created By: %s

            Message: %s
            """.formatted(
                recipient.getUsername(),
                eventType,
                task.getId(),
                task.getTitle(),
                task.getStatus(),
                task.getPriority(),
                task.getTaskDateTime(),
                task.getAssignedUser() != null ? task.getAssignedUser().getUsername() : "",
                task.getCreatedBy() != null ? task.getCreatedBy().getUsername() : "",
                message
        ));
    }
}
