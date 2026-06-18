package com.example.model;

import java.sql.Timestamp;
import java.util.Date;

public class Contest {
    private int id;
    private String title;
    private String description;
    private String rules;
    private Timestamp startTime;
    private Timestamp endTime;
    private boolean isActive;
    private String reward;
    private String status;
    private int createdBy;
    private Timestamp createdAt;
    private int tasksCount;
    private int participantsCount;
    private boolean userJoined;
    private boolean userCompleted;
    private boolean userFinished;
    private int userPoints;
    private int userSolvedCount;

    public Contest() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getReward() { return reward; }
    public void setReward(String reward) { this.reward = reward; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getRules() { return rules; }
    public void setRules(String rules) { this.rules = rules; }

    public Timestamp getStartTime() { return startTime; }
    public void setStartTime(Timestamp startTime) { this.startTime = startTime; }

    public Timestamp getEndTime() { return endTime; }
    public void setEndTime(Timestamp endTime) { this.endTime = endTime; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public void setStatus(String status) { this.status = status; }

    public int getCreatedBy() { return createdBy; }
    public void setCreatedBy(int createdBy) { this.createdBy = createdBy; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public int getTasksCount() { return tasksCount; }
    public void setTasksCount(int tasksCount) { this.tasksCount = tasksCount; }

    public int getParticipantsCount() { return participantsCount; }
    public void setParticipantsCount(int participantsCount) { this.participantsCount = participantsCount; }

    public boolean isUserJoined() { return userJoined; }
    public void setUserJoined(boolean userJoined) { this.userJoined = userJoined; }

    public boolean isUserCompleted() { return userCompleted; }
    public void setUserCompleted(boolean userCompleted) { this.userCompleted = userCompleted; }

    public boolean isUserFinished() { return userFinished; }
    public void setUserFinished(boolean userFinished) { this.userFinished = userFinished; }

    public String getStatusForDisplay() {
        if ("COMPLETED".equals(status)) return "Завершено";
        return "Активно";
    }

    public int getUserPoints() { return userPoints; }
    public void setUserPoints(int userPoints) { this.userPoints = userPoints; }

    public int getUserSolvedCount() { return userSolvedCount; }
    public void setUserSolvedCount(int userSolvedCount) { this.userSolvedCount = userSolvedCount; }

    public String getStartTimeFormatted() {
        return startTime != null ? startTime.toString() : "";
    }

    public String getEndTimeFormatted() {
        return endTime != null ? endTime.toString() : "";
    }

    public String getStatus() {
        if (startTime == null || endTime == null) {
            return "unknown";
        }

        Date now = new Date();
        if (now.before(startTime)) {
            return "upcoming";
        } else if (now.after(endTime)) {
            return "finished";
        } else {
            return "active";
        }
    }

    public boolean isActuallyActive() {
        Date now = new Date();
        return startTime != null && endTime != null &&
                !now.before(startTime) && !now.after(endTime);
    }
}