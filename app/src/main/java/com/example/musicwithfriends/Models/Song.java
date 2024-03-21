package com.example.musicwithfriends.Models;

import java.io.Serializable;

public class Song implements Serializable {
    String path, title, artist, songName;

    public Song() {}

    public Song(String path, String title, String artist) {
        this.path = path;
        this.title = title;
        this.artist = artist;

        String[] separator = path.toString().split("/");
        this.songName = separator[separator.length - 1];
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
        return songName;
    }
}
