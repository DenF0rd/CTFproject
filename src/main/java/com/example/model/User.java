package com.example.model;

import java.util.Date;

public class User {
    private int id;
    private String username;
    private String email;
    private String password;
    private String verificationCode;
    private boolean verified;
    private boolean isAdmin;
    private int score;
    private int solvedCount;
    private Date registrationDate;
    private Date lastLogin;
    private boolean isActive;

    // ПОЛЯ ДЛЯ ПРОФИЛЯ
    private String bio;
    private int age;
    private String city;
    private String avatarPath;
    private boolean hideEmail;
    private boolean hideAge;
    private boolean hideCity;
    private boolean isPrivate;

    // Конструктор по умолчанию
    public User() {}

    // ========== ГЕТТЕРЫ И СЕТТЕРЫ ==========

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getVerificationCode() { return verificationCode; }
    public void setVerificationCode(String verificationCode) { this.verificationCode = verificationCode; }

    public boolean isVerified() { return verified; }
    public void setVerified(boolean verified) { this.verified = verified; }

    public boolean isAdmin() { return isAdmin; }
    public void setAdmin(boolean isAdmin) { this.isAdmin = isAdmin; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public int getSolvedCount() { return solvedCount; }
    public void setSolvedCount(int solvedCount) { this.solvedCount = solvedCount; }

    public Date getRegistrationDate() { return registrationDate; }
    public void setRegistrationDate(Date registrationDate) { this.registrationDate = registrationDate; }

    public Date getLastLogin() { return lastLogin; }
    public void setLastLogin(Date lastLogin) { this.lastLogin = lastLogin; }

    // ========== ПОЛЯ ПРОФИЛЯ ==========

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getAvatarPath() { return avatarPath; }
    public void setAvatarPath(String avatarPath) { this.avatarPath = avatarPath; }

    public boolean isHideEmail() { return hideEmail; }
    public void setHideEmail(boolean hideEmail) { this.hideEmail = hideEmail; }

    public boolean isHideAge() { return hideAge; }
    public void setHideAge(boolean hideAge) { this.hideAge = hideAge; }

    public boolean isHideCity() { return hideCity; }
    public void setHideCity(boolean hideCity) { this.hideCity = hideCity; }

    public boolean isPrivate() { return isPrivate; }
    public void setPrivate(boolean isPrivate) { this.isPrivate = isPrivate; }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", verified=" + verified +
                ", isAdmin=" + isAdmin +
                ", score=" + score +
                ", solvedCount=" + solvedCount +
                '}';
    }
}