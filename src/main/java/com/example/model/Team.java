package com.example.model;

import java.sql.Timestamp;

public class Team {
    private int id;
    private String name;
    private String description;
    private int captainId;
    private Timestamp createdAt;
    private int membersCount;
    private boolean isCaptain;
    private int totalPoints;

    public Team() {}

    // Геттеры и сеттеры
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getCaptainId() { return captainId; }
    public void setCaptainId(int captainId) { this.captainId = captainId; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public int getMembersCount() { return membersCount; }
    public void setMembersCount(int membersCount) { this.membersCount = membersCount; }

    public boolean isCaptain() { return isCaptain; }
    public void setCaptain(boolean captain) { isCaptain = captain; }

    // <--- ДОБАВЬТЕ ЭТИ МЕТОДЫ
    public int getTotalPoints() { return totalPoints; }
    public void setTotalPoints(int totalPoints) { this.totalPoints = totalPoints; }
}