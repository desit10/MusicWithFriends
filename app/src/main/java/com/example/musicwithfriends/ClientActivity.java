package com.example.musicwithfriends;

import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.MediaItem;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.musicwithfriends.Helpers.FirebaseHelper;
import com.example.musicwithfriends.Models.Room;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

@UnstableApi
public class ClientActivity extends AppCompatActivity {

    ImageView songAlbum;
    TextView songTitle, songArtist;
    ExoPlayer player;
    PlayerView playerView;
    Room room;
    String roomId;
    DatabaseReference rooms, roomUpdateListener;
    StorageReference storageReference;
    FirebaseHelper firebaseHelper = new FirebaseHelper();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);

        player = new ExoPlayer.Builder(ClientActivity.this).build();

        roomId = getIntent().getStringExtra("ROOM_ID");

        playerView = findViewById(R.id.playerSong);
        songAlbum = findViewById(R.id.songAlbum);
        songTitle = findViewById(R.id.songTitle);
        songArtist = findViewById(R.id.songArtist);

        downloadingSong();

        roomUpdateListener = firebaseHelper.Request("rooms/" + roomId);
        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                String key = dataSnapshot.getKey();
                if(key.equals("currentSong") ){
                    downloadingSong();
                }
                if(key.equals("stateSong")){
                    stateSong(dataSnapshot.getValue(Boolean.class));
                }
                if(key.equals("progressSong")){
                    player.seekTo(dataSnapshot.getValue(Integer.class));
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
        roomUpdateListener.addChildEventListener(childEventListener);
    }

    private void downloadingSong(){
        rooms = firebaseHelper.Request("rooms");

        rooms.orderByKey().equalTo(roomId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    room = snapshot.getValue(Room.class);

                    storageReference = FirebaseStorage.getInstance().getReference(roomId + "/");

                    storageReference.child(room.getCurrentSong()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            player.setMediaItem(MediaItem.fromUri(uri));
                            playerView.setPlayer(player);
                            stateSong(room.getStateSong());

                            uploadingSongData(uri);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
    private void uploadingSongData(Uri uriSong) {
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(String.valueOf(uriSong));
        byte[] data = mmr.getEmbeddedPicture();

        songTitle.setText(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE));
        songArtist.setText(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST));

        //Если обложки нет, то подставляем альтернативную абложку
        if(data == null){
            songAlbum.setImageResource(R.drawable.alternativ_song_album);
        } else {
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            songAlbum.setImageBitmap(bitmap);
        }
    }
    private void stateSong(Boolean state){
        if(state == true){
            player.prepare();
            player.play();
        }
        if(state == false){
            player.stop();
        }
    }
}