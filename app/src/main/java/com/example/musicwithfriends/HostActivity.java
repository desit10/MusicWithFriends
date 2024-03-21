package com.example.musicwithfriends;

import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.util.UnstableApi;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.net.Uri;
import android.os.Bundle;

import com.example.musicwithfriends.Adapters.CurrentSongsWithFriendsAdapter;
import com.example.musicwithfriends.Adapters.SongsWithFriendsAdapter;
import com.example.musicwithfriends.Helpers.FirebaseHelper;
import com.example.musicwithfriends.Helpers.SnapHelperOneByOne;
import com.example.musicwithfriends.Models.Room;
import com.example.musicwithfriends.Models.Song;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

@UnstableApi
public class HostActivity extends AppCompatActivity {

    RecyclerView songsRecycler, currentSongsRecycler;
    SongsWithFriendsAdapter songsWithFriendsAdapter;
    CurrentSongsWithFriendsAdapter currentSongsWithFriendsAdapter;
    Room room;
    String roomId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host);

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

                    room = snapshot.getValue(Room.class);

                    ArrayList<Song> songs = new ArrayList<>();
                    StorageReference storageReference = FirebaseStorage.getInstance().getReference(roomId + "/");

                    for(Song song : room.getRoomPlaylist()){
                        storageReference.child(song.getSongName()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                song.setPath(uri.toString());
                                songs.add(song);

                                if(room.getRoomPlaylist().size() == songs.size()){
                                    room.setRoomPlaylist(songs);

                                    songsRecycler.setLayoutManager(new LinearLayoutManager(HostActivity.this, LinearLayoutManager.VERTICAL, false));
                                    songsWithFriendsAdapter =
                                            new SongsWithFriendsAdapter(HostActivity.this, currentSongsRecycler, room, roomId);
                                    songsRecycler.setAdapter(songsWithFriendsAdapter);


                                    currentSongsRecycler.setLayoutManager(new LinearLayoutManager(HostActivity.this, LinearLayoutManager.HORIZONTAL, false));
                                    currentSongsWithFriendsAdapter =
                                            new CurrentSongsWithFriendsAdapter(HostActivity.this, currentSongsRecycler, room, roomId);

                                    if(room.getHost()){
                                        currentSongsRecycler.setOnFlingListener(null);
                                        SnapHelperOneByOne snapHelperOneByOne = new SnapHelperOneByOne();
                                        snapHelperOneByOne.attachToRecyclerView(currentSongsRecycler);
                                    }

                                    currentSongsRecycler.setAdapter(currentSongsWithFriendsAdapter);

                                    currentSongsWithFriendsAdapter.setStateSong(room.getStateSong());
                                    currentSongsRecycler.scrollToPosition(0);
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
    }
}