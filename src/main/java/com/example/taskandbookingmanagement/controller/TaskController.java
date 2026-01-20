package com.example.taskandbookingmanagement.controller;

import com.example.taskandbookingmanagement.dto.TaskCreateRequest;
import com.example.taskandbookingmanagement.dto.TaskDecisionRequest;
import com.example.taskandbookingmanagement.dto.TaskResponse;
import com.example.taskandbookingmanagement.model.TaskStatus;
import com.example.taskandbookingmanagement.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    // POST /api/tasks – Create task
    @PostMapping
    public TaskResponse create(@Valid @RequestBody TaskCreateRequest req) {
        return taskService.createTask(req);
    }

    // GET /api/tasks – List tasks (filter + sort)
    @GetMapping
    public List<TaskResponse> list(
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDir
    ) {
        return taskService.listTasks(status, sortBy, sortDir);
    }

    // PUT /api/tasks/{id}/approve – Approve/reject task (Manager OR Admin)
    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public TaskResponse approveOrReject(
            @PathVariable Long id,
            @Valid @RequestBody TaskDecisionRequest req
    ) {
        return taskService.decide(id, req.getDecision());
    }

    // ✅ CSV Export
    // GET /api/tasks/export?status=PENDING&sortBy=taskDateTime&sortDir=asc
    @GetMapping(value = "/export", produces = "text/csv")
    public ResponseEntity<byte[]> exportCsv(
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDir
    ) {
        List<TaskResponse> tasks = taskService.listTasks(status, sortBy, sortDir);

        String csv = toCsv(tasks);

        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        String filename = "tasks-" + ts + ".csv";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(csv.getBytes(StandardCharsets.UTF_8));
    }

    private String toCsv(List<TaskResponse> tasks) {
        StringBuilder sb = new StringBuilder();

        // Header
        sb.append("id,title,description,status,priority,taskDateTime,assignedUsername,createdByUsername,decisionByUsername,decisionAt,createdAt,updatedAt\n");

        for (TaskResponse t : tasks) {
            sb.append(csv(t.getId()))
                    .append(',').append(csv(t.getTitle()))
                    .append(',').append(csv(t.getDescription()))
                    .append(',').append(csv(t.getStatus()))
                    .append(',').append(csv(t.getPriority()))
                    .append(',').append(csv(t.getTaskDateTime()))
                    .append(',').append(csv(t.getAssignedUsername()))
                    .append(',').append(csv(t.getCreatedByUsername()))
                    .append(',').append(csv(t.getDecisionByUsername()))
                    .append(',').append(csv(t.getDecisionAt()))
                    .append(',').append(csv(t.getCreatedAt()))
                    .append(',').append(csv(t.getUpdatedAt()))
                    .append('\n');
        }

        return sb.toString();
    }

    // CSV escaping: wrap in quotes if needed, escape quotes by doubling them
    private String csv(Object value) {
        if (value == null) return "";
        String s = String.valueOf(value);
        boolean needsQuotes = s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r");
        if (s.contains("\"")) s = s.replace("\"", "\"\"");
        return needsQuotes ? "\"" + s + "\"" : s;
    }
}
