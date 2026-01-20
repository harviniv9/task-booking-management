package com.example.taskandbookingmanagement.service;

import com.example.taskandbookingmanagement.dto.TaskCreateRequest;
import com.example.taskandbookingmanagement.dto.TaskDecisionRequest;
import com.example.taskandbookingmanagement.dto.TaskResponse;
import com.example.taskandbookingmanagement.model.*;
import com.example.taskandbookingmanagement.repository.TaskRepository;
import com.example.taskandbookingmanagement.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock private TaskRepository taskRepository;
    @Mock private UserRepository userRepository;
    @Mock private NotificationService notificationService;

    private TaskService taskService;

    @BeforeEach
    void setUp() {
        taskService = new TaskService(taskRepository, userRepository, notificationService);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // -----------------------
    // Helpers
    // -----------------------
    private void mockLoggedInUsername(String username) {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn(username);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);

        SecurityContextHolder.setContext(securityContext);
    }

    private User user(Long id, String username, Role role) {
        User u = new User();
        // ✅ no setId() needed
        ReflectionTestUtils.setField(u, "id", id);
        u.setUsername(username);
        u.setRole(role);
        return u;
    }

    private Task taskWithId(Long id) {
        Task t = new Task();
        // ✅ no setId() needed
        ReflectionTestUtils.setField(t, "id", id);
        return t;
    }

    // -----------------------
    // createTask tests
    // -----------------------
    @Test
    void createTask_shouldCreatePendingTask_notifyAssigned_andReturnResponse() {
        // Arrange
        mockLoggedInUsername("creator");
        User creator = user(10L, "creator", Role.USER);
        User assigned = user(20L, "assignee", Role.USER);

        when(userRepository.findByUsername("creator")).thenReturn(Optional.of(creator));
        when(userRepository.findById(20L)).thenReturn(Optional.of(assigned));

        TaskCreateRequest req = new TaskCreateRequest();
        req.setTitle("T1");
        req.setDescription("D1");
        req.setTaskDateTime(LocalDateTime.of(2026, 1, 21, 9, 30));
        req.setPriority(Priority.HIGH); // change if your enum differs
        req.setAssignedUserId(20L);

        ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);

        Task saved = new Task();
        ReflectionTestUtils.setField(saved, "id", 99L);
        saved.setTitle(req.getTitle());
        saved.setDescription(req.getDescription());
        saved.setTaskDateTime(req.getTaskDateTime());
        saved.setPriority(req.getPriority());
        saved.setAssignedUser(assigned);
        saved.setCreatedBy(creator);
        saved.setStatus(TaskStatus.PENDING);

        when(taskRepository.save(any(Task.class))).thenReturn(saved);

        // Act
        TaskResponse resp = taskService.createTask(req);

        // Assert
        verify(taskRepository).save(taskCaptor.capture());
        Task toSave = taskCaptor.getValue();

        assertThat(toSave.getTitle()).isEqualTo("T1");
        assertThat(toSave.getAssignedUser().getUsername()).isEqualTo("assignee");
        assertThat(toSave.getCreatedBy().getUsername()).isEqualTo("creator");
        assertThat(toSave.getStatus()).isEqualTo(TaskStatus.PENDING);

        verify(notificationService).notifyTaskCreated(saved);

        assertThat(resp.getId()).isEqualTo(99L);
        assertThat(resp.getStatus()).isEqualTo(TaskStatus.PENDING);
        assertThat(resp.getAssignedUserId()).isEqualTo(20L);
        assertThat(resp.getAssignedUsername()).isEqualTo("assignee");
        assertThat(resp.getCreatedByUserId()).isEqualTo(10L);
        assertThat(resp.getCreatedByUsername()).isEqualTo("creator");
    }

    @Test
    void createTask_shouldThrowIfAssignedUserNotFound() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        TaskCreateRequest req = new TaskCreateRequest();
        req.setAssignedUserId(999L);
        req.setTitle("X");

        // Act + Assert
        assertThatThrownBy(() -> taskService.createTask(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Assigned user not found");

        verify(userRepository).findById(999L);
        verify(taskRepository, never()).save(any());
        verify(notificationService, never()).notifyTaskCreated(any());
    }



    @Test
    void createTask_shouldThrowIfLoggedInUserNotInDb() {
        // Arrange
        mockLoggedInUsername("ghost");
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user(1L, "assignee", Role.USER)));

        TaskCreateRequest req = new TaskCreateRequest();
        req.setAssignedUserId(1L);
        req.setTitle("T");

        // Act + Assert
        assertThatThrownBy(() -> taskService.createTask(req))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Logged-in user not found in DB");

        verify(taskRepository, never()).save(any());
        verify(notificationService, never()).notifyTaskCreated(any());
    }

    // -----------------------
    // listTasks tests
    // -----------------------
    @Test
    void listTasks_whenStatusNull_shouldFindAll_withDefaultSort() {
        // Arrange
        Task t1 = taskWithId(1L);
        t1.setTitle("A");
        t1.setStatus(TaskStatus.PENDING);
        t1.setPriority(Priority.LOW); // change if your enum differs
        t1.setTaskDateTime(LocalDateTime.of(2026, 1, 21, 10, 0));
        t1.setAssignedUser(user(2L, "assignee", Role.USER));
        t1.setCreatedBy(user(3L, "creator", Role.USER));

        when(taskRepository.findAll(any(Sort.class))).thenReturn(List.of(t1));

        // Act
        List<TaskResponse> res = taskService.listTasks(null, null, null);

        // Assert
        ArgumentCaptor<Sort> sortCaptor = ArgumentCaptor.forClass(Sort.class);
        verify(taskRepository).findAll(sortCaptor.capture());

        Sort sort = sortCaptor.getValue();
        Sort.Order order = sort.getOrderFor("taskDateTime");

        assertThat(order).isNotNull();
        assertThat(order.getDirection()).isEqualTo(Sort.Direction.ASC);

        assertThat(res).hasSize(1);
        assertThat(res.get(0).getId()).isEqualTo(1L);
    }

    @Test
    void listTasks_whenStatusProvided_shouldFindByStatus_withCustomSort() {
        // Arrange
        when(taskRepository.findByStatus(eq(TaskStatus.PENDING), any(Sort.class)))
                .thenReturn(List.of());

        // Act
        taskService.listTasks(TaskStatus.PENDING, "priority", "desc");

        // Assert
        ArgumentCaptor<Sort> sortCaptor = ArgumentCaptor.forClass(Sort.class);
        verify(taskRepository).findByStatus(eq(TaskStatus.PENDING), sortCaptor.capture());

        Sort sort = sortCaptor.getValue();
        Sort.Order order = sort.getOrderFor("priority");

        assertThat(order).isNotNull();
        assertThat(order.getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    // -----------------------
    // decide tests
    // -----------------------
    @Test
    void decide_whenApprove_shouldSetApproved_decisionFields_andNotify() {
        // Arrange
        mockLoggedInUsername("manager");
        User manager = user(1L, "manager", Role.MANAGER);
        when(userRepository.findByUsername("manager")).thenReturn(Optional.of(manager));

        User assignee = user(2L, "assignee", Role.USER);
        User creator = user(3L, "creator", Role.USER);

        Task t = new Task();
        ReflectionTestUtils.setField(t, "id", 50L);
        t.setTitle("T");
        t.setStatus(TaskStatus.PENDING);
        t.setAssignedUser(assignee);
        t.setCreatedBy(creator);

        when(taskRepository.findById(50L)).thenReturn(Optional.of(t));
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        TaskResponse resp = taskService.decide(50L, TaskDecisionRequest.Decision.APPROVE);

        // Assert
        assertThat(t.getStatus()).isEqualTo(TaskStatus.APPROVED);
        assertThat(t.getDecisionBy()).isEqualTo(manager);
        assertThat(t.getDecisionAt()).isNotNull();

        verify(notificationService).notifyTaskApproved(t, manager);

        assertThat(resp.getStatus()).isEqualTo(TaskStatus.APPROVED);
        assertThat(resp.getDecisionByUsername()).isEqualTo("manager");
    }

    @Test
    void decide_whenReject_shouldSetRejected_andNotify() {
        // Arrange
        mockLoggedInUsername("admin");
        User admin = user(1L, "admin", Role.ADMIN);
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));

        Task t = new Task();
        ReflectionTestUtils.setField(t, "id", 60L);
        t.setStatus(TaskStatus.PENDING);
        t.setAssignedUser(user(2L, "assignee", Role.USER));
        t.setCreatedBy(user(3L, "creator", Role.USER));

        when(taskRepository.findById(60L)).thenReturn(Optional.of(t));
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        TaskResponse resp = taskService.decide(60L, TaskDecisionRequest.Decision.REJECT);

        // Assert
        assertThat(t.getStatus()).isEqualTo(TaskStatus.REJECTED);
        assertThat(t.getDecisionBy()).isEqualTo(admin);
        assertThat(t.getDecisionAt()).isNotNull();

        verify(notificationService).notifyTaskRejected(t, admin);

        assertThat(resp.getStatus()).isEqualTo(TaskStatus.REJECTED);
        assertThat(resp.getDecisionByUsername()).isEqualTo("admin");
    }

    @Test
    void decide_shouldThrowSecurityException_ifActorNotManagerOrAdmin() {
        // Arrange
        mockLoggedInUsername("user1");
        User normal = user(1L, "user1", Role.USER);
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(normal));

        // Act + Assert
        assertThatThrownBy(() -> taskService.decide(1L, TaskDecisionRequest.Decision.APPROVE))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("Only MANAGER or ADMIN");

        verify(taskRepository, never()).findById(anyLong());
        verify(notificationService, never()).notifyTaskApproved(any(), any());
        verify(notificationService, never()).notifyTaskRejected(any(), any());
    }

    @Test
    void decide_shouldThrowIllegalArgumentException_ifTaskNotFound() {
        // Arrange
        mockLoggedInUsername("manager");
        User manager = user(1L, "manager", Role.MANAGER);
        when(userRepository.findByUsername("manager")).thenReturn(Optional.of(manager));

        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> taskService.decide(999L, TaskDecisionRequest.Decision.APPROVE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Task not found");

        verify(notificationService, never()).notifyTaskApproved(any(), any());
        verify(notificationService, never()).notifyTaskRejected(any(), any());
    }

    @Test
    void decide_shouldThrowIllegalStateException_ifTaskNotPending() {
        // Arrange
        mockLoggedInUsername("manager");
        User manager = user(1L, "manager", Role.MANAGER);
        when(userRepository.findByUsername("manager")).thenReturn(Optional.of(manager));

        Task t = new Task();
        ReflectionTestUtils.setField(t, "id", 70L);
        t.setStatus(TaskStatus.APPROVED);

        when(taskRepository.findById(70L)).thenReturn(Optional.of(t));

        // Act + Assert
        assertThatThrownBy(() -> taskService.decide(70L, TaskDecisionRequest.Decision.REJECT))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only PENDING tasks can be approved/rejected");

        verify(taskRepository, never()).save(any());
        verify(notificationService, never()).notifyTaskApproved(any(), any());
        verify(notificationService, never()).notifyTaskRejected(any(), any());
    }
}
