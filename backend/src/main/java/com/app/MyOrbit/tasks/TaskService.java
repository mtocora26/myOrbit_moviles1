package com.app.MyOrbit.tasks;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
        seed();
    }

    public List<Task> listByUser(String userId) {
        return taskRepository.findByUserIdOrderByIdAsc(userId);
    }

    public Task findById(String id) {
        return taskRepository.findById(id).orElse(null);
    }

    public Task create(CreateTaskRequest request) {
        Task task = new Task();
        task.setId(UUID.randomUUID().toString());
        task.setUserId(defaultUser(request.getUserId()));
        task.setTitle(request.getTitle());
        task.setDue(defaultText(request.getDue(), "Sin fecha"));
        task.setPriority(defaultText(request.getPriority(), "media"));
        task.setTag(defaultText(request.getTag(), "Personal"));
        task.setDone(false);
        return taskRepository.save(task);
    }

    public Task updateStatus(String id, UpdateTaskStatusRequest request) {
        Task task = findById(id);
        if (task == null) {
            return null;
        }
        task.setDone(request.isDone());
        if (!task.getSubtasks().isEmpty()) {
            task.getSubtasks().forEach(subtask -> markSubtaskTree(subtask, request.isDone()));
        }
        return taskRepository.save(task);
    }

    public Task addSubtask(String taskId, CreateSubtaskRequest request) {
        Task task = findById(taskId);
        if (task == null || request.title() == null || request.title().isBlank()) {
            return null;
        }
        Subtask subtask = new Subtask();
        subtask.setId(UUID.randomUUID().toString());
        subtask.setTitle(request.title().trim());
        subtask.setDone(false);
        subtask.setDue(blankToNull(request.due()));
        task.getSubtasks().add(subtask);
        task.setDone(false);
        return taskRepository.save(task);
    }

    public Task addNestedSubtask(String taskId, String parentSubtaskId, CreateSubtaskRequest request) {
        Task task = findById(taskId);
        if (task == null || request.title() == null || request.title().isBlank()) return null;
        Subtask parent = findSubtask(task.getSubtasks(), parentSubtaskId);
        if (parent == null) return null;
        Subtask subtask = new Subtask();
        subtask.setId(UUID.randomUUID().toString());
        subtask.setTitle(request.title().trim());
        subtask.setDone(false);
        subtask.setDue(blankToNull(request.due()));
        parent.getSubtasks().add(subtask);
        synchronizeTaskCompletion(task);
        return taskRepository.save(task);
    }

    public Task updateSubtask(String taskId, String subtaskId, UpdateSubtaskRequest request) {
        Task task = findById(taskId);
        if (task == null) return null;
        Subtask subtask = findSubtask(task.getSubtasks(), subtaskId);
        if (subtask == null) return null;
        if (request.title() != null && !request.title().isBlank()) {
            subtask.setTitle(request.title().trim());
        }
        subtask.setDue(blankToNull(request.due()));
        return taskRepository.save(task);
    }

    public Task updateSubtaskStatus(String taskId, String subtaskId, UpdateSubtaskStatusRequest request) {
        Task task = findById(taskId);
        if (task == null) return null;
        Subtask subtask = findSubtask(task.getSubtasks(), subtaskId);
        if (subtask == null) return null;
        markSubtaskTree(subtask, request.done());
        synchronizeTaskCompletion(task);
        return taskRepository.save(task);
    }

    public Task deleteSubtask(String taskId, String subtaskId) {
        Task task = findById(taskId);
        if (task == null || !removeSubtask(task.getSubtasks(), subtaskId)) {
            return null;
        }
        synchronizeTaskCompletion(task);
        return taskRepository.save(task);
    }

    public Task update(String id, UpdateTaskRequest request) {
        Task task = findById(id);
        if (task == null) {
            return null;
        }

        task.setTitle(defaultText(request.getTitle(), task.getTitle()));
        task.setDue(defaultText(request.getDue(), task.getDue()));
        task.setPriority(defaultPriority(request.getPriority(), task.getPriority()));
        task.setTag(defaultTag(request.getTag(), task.getTag()));
        return taskRepository.save(task);
    }

    public boolean delete(String id) {
        if (!taskRepository.existsById(id)) {
            return false;
        }
        taskRepository.deleteById(id);
        return true;
    }

    private void seed() {
        if (taskRepository.count() > 0) {
            return;
        }

        Task t1 = new Task();
        t1.setId("seed-1");
        t1.setUserId("demo-user");
        t1.setTitle("Entregar proyecto de Moviles");
        t1.setDue("Hoy 11:59 PM");
        t1.setPriority("alta");
        t1.setTag("Universidad");
        t1.setDone(false);

        Task t2 = new Task();
        t2.setId("seed-2");
        t2.setUserId("demo-user");
        t2.setTitle("Comprar materiales de cartelera");
        t2.setDue("Hoy 6:00 PM");
        t2.setPriority("media");
        t2.setTag("Proyecto");
        t2.setDone(false);

        taskRepository.saveAll(List.of(t1, t2));
    }

    private String defaultUser(String userId) {
        if (userId == null || userId.isBlank()) {
            return "demo-user";
        }
        return userId;
    }

    private Subtask findSubtask(List<Subtask> subtasks, String id) {
        for (Subtask subtask : subtasks) {
            if (subtask.getId().equals(id)) return subtask;
            Subtask found = findSubtask(subtask.getSubtasks(), id);
            if (found != null) return found;
        }
        return null;
    }

    private boolean removeSubtask(List<Subtask> subtasks, String id) {
        if (subtasks.removeIf(subtask -> subtask.getId().equals(id))) return true;
        for (Subtask subtask : subtasks) {
            if (removeSubtask(subtask.getSubtasks(), id)) return true;
        }
        return false;
    }

    private void markSubtaskTree(Subtask subtask, boolean done) {
        subtask.setDone(done);
        subtask.getSubtasks().forEach(child -> markSubtaskTree(child, done));
    }

    private boolean synchronizeSubtaskCompletion(Subtask subtask) {
        if (subtask.getSubtasks().isEmpty()) return subtask.isDone();
        boolean complete = subtask.getSubtasks().stream().map(this::synchronizeSubtaskCompletion).allMatch(Boolean::booleanValue);
        subtask.setDone(complete);
        return complete;
    }

    private void synchronizeTaskCompletion(Task task) {
        task.setDone(!task.getSubtasks().isEmpty() && task.getSubtasks().stream().map(this::synchronizeSubtaskCompletion).allMatch(Boolean::booleanValue));
    }

    private String defaultText(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value;
    }

    private String defaultPriority(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        String normalized = value.toLowerCase();
        if (normalized.equals("alta") || normalized.equals("media") || normalized.equals("baja")) {
            return normalized;
        }
        return fallback;
    }

    private String defaultTag(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value.trim();
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
