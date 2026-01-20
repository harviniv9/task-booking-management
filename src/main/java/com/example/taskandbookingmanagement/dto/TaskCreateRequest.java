package com.example.taskandbookingmanagement.dto;

import com.example.taskandbookingmanagement.model.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class TaskCreateRequest {

    @NotBlank
    private String title;

    private String description;

    @NotNull
    private LocalDateTime taskDateTime;

    @NotNull
    private Priority priority;

    @NotNull
    private Long assignedUserId;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getTaskDateTime() { return taskDateTime; }
    public void setTaskDateTime(LocalDateTime taskDateTime) { this.taskDateTime = taskDateTime; }

    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }

    public Long getAssignedUserId() { return assignedUserId; }
    public void setAssignedUserId(Long assignedUserId) { this.assignedUserId = assignedUserId; }
}
