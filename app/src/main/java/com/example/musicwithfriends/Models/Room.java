package com.example.musicwithfriends.Models;

import java.util.ArrayList;

public class Room {

    ArrayList<Song> roomPlaylist = new ArrayList<>();
    int lastSong, nextSong, playPause;

    public Room() {}

    public Room(ArrayList<Song> songs, int lastSong, int nextSong, int playPause) {
        this.lastSong = lastSong;
        this.nextSong = nextSong;
        this.playPause = playPause;

        if(songs != null){
            for (Song song : songs){
                this.roomPlaylist.add(song);
            }
        } else {
            this.roomPlaylist = null;
        }

    }

    public ArrayList<Song> getRoomPlaylist() {
        return roomPlaylist;
    }

    public void setRoomPlaylist(ArrayList<Song> songs) {
        this.roomPlaylist = songs;
    }

    public int getLastSong() {
        return lastSong;
    }

    public void setLastSong(int lastSong) {
        this.lastSong = lastSong;
    }

    public int getNextSong() {
        return nextSong;
    }

    public void setNextSong(int nextSong) {
        this.nextSong = nextSong;
    }

    public int getPlayPause() {
        return playPause;
    }

    public void setPlayPause(int playPause) {
        this.playPause = playPause;
    }
}
