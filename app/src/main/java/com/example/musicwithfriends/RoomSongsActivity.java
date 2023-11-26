package com.example.musicwithfriends;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.musicwithfriends.Adapters.CurrentSongsWithFriendsAdapter;
import com.example.musicwithfriends.Adapters.SongsAdapter;
import com.example.musicwithfriends.Adapters.SongsWithFriendsAdapter;
import com.example.musicwithfriends.Helpers.FirebaseHelper;
import com.example.musicwithfriends.Helpers.SnapHelperOneByOne;
import com.example.musicwithfriends.Models.Room;
import com.example.musicwithfriends.Models.Song;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class RoomSongsActivity extends AppCompatActivity {

    RecyclerView songsRecycler, currentSongsRecycler;
    SongsWithFriendsAdapter songsWithFriendsAdapter;
    CurrentSongsWithFriendsAdapter currentSongsWithFriendsAdapter;
    DatabaseReference roomUpdate;
    Room room;
    String roomId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_songs);

        roomId = getIntent().getStringExtra("ROOM_ID");

        songsRecycler = findViewById(R.id.songsRecycler);
        currentSongsRecycler = findViewById(R.id.recyclerCurrentSong);

        FirebaseHelper firebaseHelper = new FirebaseHelper();
        DatabaseReference rooms = firebaseHelper.Request("rooms");
        rooms.orderByKey().equalTo(roomId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //Перебор данных
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    //Создание объекта "Рецепт" и передаём в него данные
                    room = snapshot.getValue(Room.class);

                    ArrayList<Song> songs = new ArrayList<>();
                    StorageReference storageReference = FirebaseStorage.getInstance().getReference(roomId + "/");

                    for(Song song : room.getRoomPlaylist()){
/*
                        storageReference.child(song.getSongName()).getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Uri> task) {
                                        if(task.isComplete()){
                                            song.setPath(task.getResult().toString());
                                            songs.add(song);
                                        }
                                        if(room.getRoomPlaylist().size() == songs.size()){
                                            room.setRoomPlaylist(songs);

                                            songsRecycler.setLayoutManager(new LinearLayoutManager(RoomSongsActivity.this, LinearLayoutManager.VERTICAL, false));
                                            songsWithFriendsAdapter =
                                                    new SongsWithFriendsAdapter(RoomSongsActivity.this, currentSongsRecycler, room, roomId);
                                            songsRecycler.setAdapter(songsWithFriendsAdapter);

                                            currentSongsRecycler.setLayoutManager(new LinearLayoutManager(RoomSongsActivity.this, LinearLayoutManager.HORIZONTAL, false));
                                            currentSongsWithFriendsAdapter =
                                                    new CurrentSongsWithFriendsAdapter(RoomSongsActivity.this, currentSongsRecycler, room, roomId);

                                            currentSongsRecycler.setAdapter(currentSongsWithFriendsAdapter);

                                            currentSongsWithFriendsAdapter.setStateSong(room.getPlayPause());
                                            currentSongsRecycler.scrollToPosition(room.getPositionSong());
                                        }
                                    }
                                });
*/
                        storageReference.child(song.getSongName()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                song.setPath(uri.toString());
                                songs.add(song);

                                if(room.getRoomPlaylist().size() == songs.size()){
                                    room.setRoomPlaylist(songs);

                                    songsRecycler.setLayoutManager(new LinearLayoutManager(RoomSongsActivity.this, LinearLayoutManager.VERTICAL, false));
                                    songsWithFriendsAdapter =
                                            new SongsWithFriendsAdapter(RoomSongsActivity.this, currentSongsRecycler, room, roomId);
                                    songsRecycler.setAdapter(songsWithFriendsAdapter);


                                    currentSongsRecycler.setLayoutManager(new LinearLayoutManager(RoomSongsActivity.this, LinearLayoutManager.HORIZONTAL, false));
                                    currentSongsWithFriendsAdapter =
                                            new CurrentSongsWithFriendsAdapter(RoomSongsActivity.this, currentSongsRecycler, room, roomId);

                                    currentSongsRecycler.setOnFlingListener(null);
                                    SnapHelperOneByOne snapHelperOneByOne = new SnapHelperOneByOne();
                                    snapHelperOneByOne.attachToRecyclerView(currentSongsRecycler);

                                    currentSongsRecycler.setAdapter(currentSongsWithFriendsAdapter);

                                    currentSongsWithFriendsAdapter.setStateSong(room.getPlayPause());
                                    currentSongsRecycler.scrollToPosition(room.getPositionSong());
                                }
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });


        roomUpdate = firebaseHelper.Request("rooms/" + roomId);
        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                String key = dataSnapshot.getKey();
                if(key.equals("positionSong") ){
                    currentSongsRecycler.scrollToPosition(dataSnapshot.getValue(Integer.class));
                }
                if(key.equals("playPause")){
                    if(!room.getHost()){
                        currentSongsWithFriendsAdapter.setStateSong(!dataSnapshot.getValue(Boolean.class));
                        currentSongsWithFriendsAdapter.PlayPause(currentSongsWithFriendsAdapter.getViewHolder());
                    }
                }
                if(key.equals("roomPlaylist")){
                    /*songsWithFriendsAdapter.notifyDataSetChanged();
                    currentSongsWithFriendsAdapter.notifyDataSetChanged();*/
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        roomUpdate.addChildEventListener(childEventListener);

    }

}