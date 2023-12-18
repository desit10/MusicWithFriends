package com.example.musicwithfriends.Models;

import java.io.Serializable;

public class Song implements Serializable {
    String id, path, title, artist, songName;

    public Song() {}

    public Song(String id, String path, String title, String artist) {
        this.id = id;
        this.path = path;
        this.title = title;
        this.artist = artist;
    }

    public String getId() {
        return id;
    }

    public String getPath() {
        return path;
    }
    public void setPath(String path) {
        this.path = path;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getSongName() {
        String[] separator = path.toString().split("/");

        return separator[separator.length - 1];
    }
}
