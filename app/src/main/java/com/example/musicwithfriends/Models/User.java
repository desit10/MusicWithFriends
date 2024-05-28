package com.example.musicwithfriends.Models;

public class User {
    private String nickname, uriAvatar;

    public User() {}

    public User(String nickname, String uriAvatar) {
        this.nickname = nickname;
        this.uriAvatar = uriAvatar;
    }

    public String getNickname() {
        return nickname;
    }

    public String getUriAvatar() {
        return uriAvatar;
    }
}
