package com.app.MyOrbit.tasks;

import java.util.ArrayList;
import java.util.List;

public class Subtask {
    private String id;
    private String title;
    private boolean done;
    private String due;
    private List<Subtask> subtasks = new ArrayList<>();

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public boolean isDone() { return done; }
    public void setDone(boolean done) { this.done = done; }
    public String getDue() { return due; }
    public void setDue(String due) { this.due = due; }
    public List<Subtask> getSubtasks() { return subtasks; }
    public void setSubtasks(List<Subtask> subtasks) { this.subtasks = subtasks == null ? new ArrayList<>() : subtasks; }
}