package com.example.musicwithfriends.Models;

import java.util.ArrayList;

public class Room {

    ArrayList<Song> roomPlaylist = new ArrayList<>();
    String currentSong;
    int progressSong;
    Boolean stateSong;

    public Room() {}

    public Room(ArrayList<Song> songs, int progressSong, Boolean stateSong, String currentSong) {
        this.progressSong = progressSong;
        this.stateSong = stateSong;
        this.currentSong = currentSong;

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

    public int getProgressSong() {
        return progressSong;
    }

    public boolean getStateSong() {
        return stateSong;
    }

    public String getCurrentSong(){
        return currentSong;
    }
}
