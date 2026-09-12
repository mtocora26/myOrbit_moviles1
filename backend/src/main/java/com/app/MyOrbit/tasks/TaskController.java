package com.app.MyOrbit.tasks;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.app.MyOrbit.users.AuthService;
import com.app.MyOrbit.users.User;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;
    private final AuthService authService;

    public TaskController(TaskService taskService, AuthService authService) {
        this.taskService = taskService;
        this.authService = authService;
    }

    @GetMapping
    public List<Task> list(@RequestHeader("Authorization") String authorization) {
        return taskService.listByUser(authService.requireUser(authorization).getId());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Task> getById(@PathVariable String id, @RequestHeader("Authorization") String authorization) {
        Task task = taskService.findById(id);
        if (!belongsToUser(task, authorization)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(task);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Task create(@RequestBody CreateTaskRequest request, @RequestHeader("Authorization") String authorization) {
        request.setUserId(authService.requireUser(authorization).getId());
        return taskService.create(request);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Task> updateStatus(@PathVariable String id, @RequestBody UpdateTaskStatusRequest request, @RequestHeader("Authorization") String authorization) {
        if (!belongsToUser(taskService.findById(id), authorization)) {
            return ResponseEntity.notFound().build();
        }
        Task task = taskService.updateStatus(id, request);
        if (task == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(task);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Task> update(@PathVariable String id, @RequestBody UpdateTaskRequest request, @RequestHeader("Authorization") String authorization) {
        if (!belongsToUser(taskService.findById(id), authorization)) {
            return ResponseEntity.notFound().build();
        }
        Task task = taskService.update(id, request);
        if (task == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(task);
    }

    @PostMapping("/{id}/subtasks")
    public ResponseEntity<Task> addSubtask(@PathVariable String id, @RequestBody CreateSubtaskRequest request, @RequestHeader("Authorization") String authorization) {
        if (!belongsToUser(taskService.findById(id), authorization)) return ResponseEntity.notFound().build();
        Task task = taskService.addSubtask(id, request);
        return task == null ? ResponseEntity.badRequest().build() : ResponseEntity.ok(task);
    }

    @PostMapping("/{id}/subtasks/{parentSubtaskId}")
    public ResponseEntity<Task> addNestedSubtask(@PathVariable String id, @PathVariable String parentSubtaskId, @RequestBody CreateSubtaskRequest request, @RequestHeader("Authorization") String authorization) {
        if (!belongsToUser(taskService.findById(id), authorization)) return ResponseEntity.notFound().build();
        Task task = taskService.addNestedSubtask(id, parentSubtaskId, request);
        return task == null ? ResponseEntity.badRequest().build() : ResponseEntity.ok(task);
    }

    @PutMapping("/{id}/subtasks/{subtaskId}")
    public ResponseEntity<Task> updateSubtask(@PathVariable String id, @PathVariable String subtaskId, @RequestBody UpdateSubtaskRequest request, @RequestHeader("Authorization") String authorization) {
        if (!belongsToUser(taskService.findById(id), authorization)) return ResponseEntity.notFound().build();
        Task task = taskService.updateSubtask(id, subtaskId, request);
        return task == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(task);
    }

    @PatchMapping("/{id}/subtasks/{subtaskId}/status")
    public ResponseEntity<Task> updateSubtaskStatus(@PathVariable String id, @PathVariable String subtaskId, @RequestBody UpdateSubtaskStatusRequest request, @RequestHeader("Authorization") String authorization) {
        if (!belongsToUser(taskService.findById(id), authorization)) return ResponseEntity.notFound().build();
        Task task = taskService.updateSubtaskStatus(id, subtaskId, request);
        return task == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(task);
    }

    @DeleteMapping("/{id}/subtasks/{subtaskId}")
    public ResponseEntity<Task> deleteSubtask(@PathVariable String id, @PathVariable String subtaskId, @RequestHeader("Authorization") String authorization) {
        if (!belongsToUser(taskService.findById(id), authorization)) return ResponseEntity.notFound().build();
        Task task = taskService.deleteSubtask(id, subtaskId);
        return task == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(task);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id, @RequestHeader("Authorization") String authorization) {
        if (!belongsToUser(taskService.findById(id), authorization)) {
            return ResponseEntity.notFound().build();
        }
        boolean removed = taskService.delete(id);
        if (!removed) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    private boolean belongsToUser(Task task, String authorization) {
        if (task == null) {
            return false;
        }
        User user = authService.requireUser(authorization);
        return task.getUserId().equals(user.getId());
    }
}
