package com.example.musicwithfriends.Models;

import java.io.Serializable;
import java.util.ArrayList;

public class Room implements Serializable {

    ArrayList<Song> roomPlaylist = new ArrayList<>();
    ArrayList<Message> roomChat = new ArrayList<>();
    String currentSong;
    int progressSong;
    Boolean stateSong;

    public Room() {}
    public Room(ArrayList<Song> songs, int progressSong, Boolean stateSong, String currentSong, ArrayList<Message> roomChat) {
        this.progressSong = progressSong;
        this.stateSong = stateSong;
        this.currentSong = currentSong;
        this.roomChat = roomChat;

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

    public ArrayList<Message> getRoomChat() {
        return roomChat;
    }
    public void setRoomChat(ArrayList<Message> roomChat) {
        this.roomChat = roomChat;
    }
}
