package com.example.taskandbookingmanagement.service;

import com.example.taskandbookingmanagement.model.*;
import org.junit.jupiter.api.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationServiceTest {

    private NotificationService notificationService;

    private PrintStream originalOut;
    private ByteArrayOutputStream outContent;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService();

        // Capture System.out
        originalOut = System.out;
        outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    // -----------------------
    // Helpers (no setId() usage)
    // -----------------------
    private User user(Long id, String username, Role role) {
        User u = new User();
        ReflectionTestUtils.setField(u, "id", id); // ✅ works even if no setId()
        u.setUsername(username);
        u.setRole(role);
        return u;
    }

    private Task task(
            Long id,
            String title,
            TaskStatus status,
            Priority priority,
            LocalDateTime dateTime,
            User assigned,
            User createdBy
    ) {
        Task t = new Task();
        ReflectionTestUtils.setField(t, "id", id); // ✅ works even if no setId()
        t.setTitle(title);
        t.setStatus(status);
        t.setPriority(priority);
        t.setTaskDateTime(dateTime);
        t.setAssignedUser(assigned);
        t.setCreatedBy(createdBy);
        return t;
    }

    // -----------------------
    // Tests
    // -----------------------
    @Test
    void notifyTaskCreated_shouldNotifyAssignedUserOnly() {
        // Arrange
        User assigned = user(2L, "assignee", Role.USER);
        User creator = user(3L, "creator", Role.USER);

        Task t = task(
                101L,
                "Task A",
                TaskStatus.PENDING,
                Priority.HIGH,
                LocalDateTime.of(2026, 1, 21, 10, 0),
                assigned,
                creator
        );

        // Act
        notificationService.notifyTaskCreated(t);

        // Assert
        String output = outContent.toString();

        assertThat(output).contains("[NOTIFICATION]");
        assertThat(output).contains("Recipient: assignee");
        assertThat(output).contains("Event: TASK_CREATED");
        assertThat(output).contains("- ID: 101");
        assertThat(output).contains("- Title: Task A");
        assertThat(output).contains("- Status: PENDING");
        assertThat(output).contains("- Priority: HIGH");
        assertThat(output).contains("Message: A task was created and assigned to you.");

        // Only one notification printed
        assertThat(countOccurrences(output, "[NOTIFICATION]")).isEqualTo(1);
    }

    @Test
    void notifyTaskApproved_shouldNotifyAssigned_andCreatorIfDifferent() {
        // Arrange
        User assigned = user(2L, "assignee", Role.USER);
        User creator = user(3L, "creator", Role.USER);
        User manager = user(9L, "manager", Role.MANAGER);

        Task t = task(
                202L,
                "Task B",
                TaskStatus.APPROVED,
                Priority.LOW,
                LocalDateTime.of(2026, 1, 22, 11, 0),
                assigned,
                creator
        );

        // Act
        notificationService.notifyTaskApproved(t, manager);

        // Assert
        String output = outContent.toString();

        // Two notifications expected: assigned + creator
        assertThat(countOccurrences(output, "[NOTIFICATION]")).isEqualTo(2);

        assertThat(output).contains("Event: TASK_APPROVED");
        assertThat(output).contains("Task approved by manager.");

        // Both recipients should appear
        assertThat(output).contains("Recipient: assignee");
        assertThat(output).contains("Recipient: creator");

        // Creator message
        assertThat(output).contains("Your task was approved by manager.");
    }

    @Test
    void notifyTaskApproved_shouldNotifyOnlyAssigned_ifCreatorSameAsAssigned() {
        // Arrange
        User sameUser = user(2L, "sameUser", Role.USER);
        User manager = user(9L, "manager", Role.MANAGER);

        Task t = task(
                303L,
                "Task C",
                TaskStatus.APPROVED,
                Priority.MEDIUM,
                LocalDateTime.of(2026, 1, 23, 12, 0),
                sameUser,
                sameUser
        );

        // Act
        notificationService.notifyTaskApproved(t, manager);

        // Assert
        String output = outContent.toString();

        // Only one notification expected
        assertThat(countOccurrences(output, "[NOTIFICATION]")).isEqualTo(1);
        assertThat(output).contains("Recipient: sameUser");
        assertThat(output).contains("Event: TASK_APPROVED");
    }

    @Test
    void notifyTaskRejected_shouldNotifyAssigned_andCreatorIfDifferent() {
        // Arrange
        User assigned = user(2L, "assignee", Role.USER);
        User creator = user(3L, "creator", Role.USER);
        User manager = user(9L, "manager", Role.MANAGER);

        Task t = task(
                404L,
                "Task D",
                TaskStatus.REJECTED,
                Priority.HIGH,
                LocalDateTime.of(2026, 1, 24, 13, 0),
                assigned,
                creator
        );

        // Act
        notificationService.notifyTaskRejected(t, manager);

        // Assert
        String output = outContent.toString();

        // Two notifications expected
        assertThat(countOccurrences(output, "[NOTIFICATION]")).isEqualTo(2);

        assertThat(output).contains("Event: TASK_REJECTED");
        assertThat(output).contains("Task rejected by manager.");
        assertThat(output).contains("Your task was rejected by manager.");

        assertThat(output).contains("Recipient: assignee");
        assertThat(output).contains("Recipient: creator");
    }

    @Test
    void notifyTaskApproved_shouldNotNotifyCreator_ifCreatedByNull() {
        // Arrange
        User assigned = user(2L, "assignee", Role.USER);
        User manager = user(9L, "manager", Role.MANAGER);

        Task t = task(
                505L,
                "Task E",
                TaskStatus.APPROVED,
                Priority.LOW,
                LocalDateTime.of(2026, 1, 25, 14, 0),
                assigned,
                null
        );

        // Act
        notificationService.notifyTaskApproved(t, manager);

        // Assert
        String output = outContent.toString();

        // Only assigned should get it
        assertThat(countOccurrences(output, "[NOTIFICATION]")).isEqualTo(1);
        assertThat(output).contains("Recipient: assignee");
        assertThat(output).contains("Event: TASK_APPROVED");
    }

    @Test
    void notifyTaskApproved_shouldThrowNullPointer_ifAssignedUserNull_currentImplementation() {
        // Arrange
        User creator = user(3L, "creator", Role.USER);
        User manager = user(9L, "manager", Role.MANAGER);

        Task t = task(
                606L,
                "Task F",
                TaskStatus.APPROVED,
                Priority.MEDIUM,
                LocalDateTime.of(2026, 1, 26, 15, 0),
                null,   // assigned user null
                creator
        );

        // Act + Assert
        Assertions.assertThrows(NullPointerException.class, () -> notificationService.notifyTaskApproved(t, manager));
    }

    // -----------------------
    // Utility
    // -----------------------
    private static int countOccurrences(String text, String token) {
        int count = 0;
        int idx = 0;
        while ((idx = text.indexOf(token, idx)) != -1) {
            count++;
            idx += token.length();
        }
        return count;
    }
}
