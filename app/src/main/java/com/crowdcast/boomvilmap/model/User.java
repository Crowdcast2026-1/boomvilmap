package com.crowdcast.boomvilmap.model;

public class User {
    private String email;
    private String nickname;
    private long createdAt;

    // firebase용 빈 생성자
    public User() {
    }

    public User(String email, String nickname, long createdAt) {
        this.email = email;
        this.nickname = nickname;
        this.createdAt = createdAt;
    }

    // Getters
    public String getEmail() { return email; }
    public String getNickname() { return nickname; }
    public long getCreatedAt() { return createdAt; }
}