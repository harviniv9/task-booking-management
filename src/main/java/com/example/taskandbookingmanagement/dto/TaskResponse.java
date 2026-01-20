package com.example.taskandbookingmanagement.dto;

import com.example.taskandbookingmanagement.model.Priority;
import com.example.taskandbookingmanagement.model.TaskStatus;

import java.time.LocalDateTime;

public class TaskResponse {
    private Long id;
    private String title;
    private String description;
    private TaskStatus status;
    private Priority priority;
    private LocalDateTime taskDateTime;

    private Long assignedUserId;
    private String assignedUsername;

    private Long createdByUserId;
    private String createdByUsername;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private LocalDateTime decisionAt;
    private String decisionByUsername;

    // getters/setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public TaskStatus getStatus() { return status; }
    public void setStatus(TaskStatus status) { this.status = status; }

    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }

    public LocalDateTime getTaskDateTime() { return taskDateTime; }
    public void setTaskDateTime(LocalDateTime taskDateTime) { this.taskDateTime = taskDateTime; }

    public Long getAssignedUserId() { return assignedUserId; }
    public void setAssignedUserId(Long assignedUserId) { this.assignedUserId = assignedUserId; }

    public String getAssignedUsername() { return assignedUsername; }
    public void setAssignedUsername(String assignedUsername) { this.assignedUsername = assignedUsername; }

    public Long getCreatedByUserId() { return createdByUserId; }
    public void setCreatedByUserId(Long createdByUserId) { this.createdByUserId = createdByUserId; }

    public String getCreatedByUsername() { return createdByUsername; }
    public void setCreatedByUsername(String createdByUsername) { this.createdByUsername = createdByUsername; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public LocalDateTime getDecisionAt() { return decisionAt; }
    public void setDecisionAt(LocalDateTime decisionAt) { this.decisionAt = decisionAt; }

    public String getDecisionByUsername() { return decisionByUsername; }
    public void setDecisionByUsername(String decisionByUsername) { this.decisionByUsername = decisionByUsername; }
}
