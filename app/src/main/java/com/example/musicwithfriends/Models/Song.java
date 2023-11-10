package com.example.musicwithfriends.Models;

import java.io.Serializable;

public class Song implements Serializable {
    String path, title, artist, album;

    public Song(String path, String title, String artist, String album) {
        this.path = path;
        this.title = title;
        this.artist = artist;
        this.album = album;
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
    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }
    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }
    public void setAlbum(String album) {
        this.album = album;
    }
}
