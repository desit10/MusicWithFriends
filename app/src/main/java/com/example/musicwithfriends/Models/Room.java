package com.example.musicwithfriends.Models;

import java.util.ArrayList;

public class Room {

    ArrayList<Song> roomPlaylist = new ArrayList<>();
    int positionSong, progressSong;
    Boolean isHost, playPause;

    public Room() {}

    public Room(Boolean isHost,ArrayList<Song> songs, int positionSong, int progressSong, Boolean playPause) {
        this.isHost = isHost;
        this.positionSong = positionSong;
        this.progressSong = progressSong;
        this.playPause = playPause;

        if(songs != null){
            for (Song song : songs){
                this.roomPlaylist.add(song);
            }
        } else {
            this.roomPlaylist = null;
        }

    }

    public Boolean getHost() {
        return isHost;
    }

    public void setHost(Boolean host) {
        isHost = host;
    }

    public ArrayList<Song> getRoomPlaylist() {
        return roomPlaylist;
    }

    public void setRoomPlaylist(ArrayList<Song> songs) {
        this.roomPlaylist = songs;
    }


    public int getPositionSong() {
        return positionSong;
    }

    public void setPositionSong(int positionSong) {
        this.positionSong = positionSong;
    }

    public int getProgressSong() {
        return progressSong;
    }

    public void setProgressSong(int progressSong) {
        this.progressSong = progressSong;
    }

    public boolean getPlayPause() {
        return playPause;
    }

    public void setPlayPause(Boolean playPause) {
        this.playPause = playPause;
    }
}
