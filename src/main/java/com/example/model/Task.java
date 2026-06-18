package com.example.model;

import java.sql.Timestamp;

public class Task {
    private int id;
    private int contestId;
    private String contestTitle;
    private String title;
    private String description;
    private int points;
    private String flag;
    private String hint;
    private boolean isActive;
    private int solvesCount;
    private Timestamp createdAt;
    private boolean solvedByUser;
    private int basePoints;
    private int minPoints;

    public Task() {}

    // Геттеры и сеттеры
    public int getBasePoints() { return basePoints; }
    public void setBasePoints(int basePoints) { this.basePoints = basePoints; }

    public int getMinPoints() { return minPoints; }
    public void setMinPoints(int minPoints) { this.minPoints = minPoints; }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getContestId() { return contestId; }
    public void setContestId(int contestId) { this.contestId = contestId; }

    public String getContestTitle() { return contestTitle; }
    public void setContestTitle(String contestTitle) { this.contestTitle = contestTitle; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }

    public String getFlag() { return flag; }
    public void setFlag(String flag) { this.flag = flag; }

    public String getHint() { return hint; }
    public void setHint(String hint) { this.hint = hint; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public int getSolvesCount() { return solvesCount; }
    public void setSolvesCount(int solvesCount) { this.solvesCount = solvesCount; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public boolean isSolvedByUser() { return solvedByUser; }
    public void setSolvedByUser(boolean solvedByUser) { this.solvedByUser = solvedByUser; }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", points=" + points +
                ", basePoints=" + basePoints +
                ", minPoints=" + minPoints +
                ", solvesCount=" + solvesCount +
                '}';
    }
}