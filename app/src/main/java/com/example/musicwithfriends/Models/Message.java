package com.example.musicwithfriends.Models;

import android.net.Uri;

public class Message {

    private Uri uriAvatar;
    private String sender;
    private String text;
    private String departureTime;

    public Message() {}
    public Message(Uri uriAvatar,String sender, String text, String departureTime) {
        this.uriAvatar = uriAvatar;
        this.sender = sender;
        this.text = text;
        this.departureTime = departureTime;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(String departureTime) {
        this.departureTime = departureTime;
    }
}
