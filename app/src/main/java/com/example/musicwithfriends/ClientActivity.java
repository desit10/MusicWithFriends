package com.example.musicwithfriends;

import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.MediaItem;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.musicwithfriends.Adapters.MessagesAdapter;
import com.example.musicwithfriends.Helpers.FirebaseHelper;
import com.example.musicwithfriends.Models.Message;
import com.example.musicwithfriends.Models.Room;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

@UnstableApi
public class ClientActivity extends AppCompatActivity {

    private PlayerView playerView;
    private ExoPlayer player;
    private ImageView songAlbum;
    private TextView songTitle, songArtist;
    private RecyclerView recyclerMessage;
    private EditText editTextMessage;
    private MessagesAdapter messagesAdapter;
    private FirebaseHelper firebaseHelper = new FirebaseHelper();
    private DatabaseReference rooms, roomUpdateListener, roomChatUpdateListener;
    private StorageReference storageReference;
    private Room room;
    private String roomId, nickname, avatar;
    private ArrayList<Message> roomChat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);

        player = new ExoPlayer.Builder(ClientActivity.this).build();

        roomId = getIntent().getStringExtra("ROOM_ID");
        nickname = getIntent().getStringExtra("NICKNAME");

        playerView = findViewById(R.id.playerSong);
        songAlbum = findViewById(R.id.songAlbum);
        songTitle = findViewById(R.id.songTitle);
        songArtist = findViewById(R.id.songArtist);
        recyclerMessage = findViewById(R.id.recyclerMessages);
        editTextMessage = findViewById(R.id.editTextMessage);

        editTextMessage.setEnabled(false);
        downloadingSong();

        roomChat = new ArrayList<>();
        messagesAdapter = new MessagesAdapter(this, roomChat, nickname);

        recyclerMessage.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerMessage.setAdapter(messagesAdapter);

        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        editTextMessage.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                editTextMessage.clearFocus();
                imm.hideSoftInputFromWindow(editTextMessage.getWindowToken(), 0);

                String message = editTextMessage.getText().toString().trim();
                if(!TextUtils.isEmpty(message)) {
                    Calendar calendar = Calendar.getInstance();
                    String departureTime =
                            String.format(
                                    "%s:%s",
                                    String.valueOf(calendar.get(Calendar.HOUR_OF_DAY)),
                                    String.valueOf(calendar.get(Calendar.MINUTE))
                            );

                    Message newMessage = new Message(null, nickname, message, departureTime);
                    createNewMessage(newMessage);
                    editTextMessage.setText("");
                    firebaseHelper.Request("rooms/" + roomId + "/roomChat/" + String.valueOf(roomChat.size() - 1)).setValue(newMessage);
                }
                return true;
            }
        });

        roomUpdateListener = firebaseHelper.Request("rooms/" + roomId);
        ChildEventListener childRoomEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {}

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
                player.stop();
                player.clearMediaItems();
                showDialogInfo();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {}

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };
        roomUpdateListener.addChildEventListener(childRoomEventListener);

        roomChatUpdateListener = firebaseHelper.Request("rooms/" + roomId + "/roomChat/");
        ChildEventListener childRoomChatEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                if(!dataSnapshot.getValue(Message.class).getSender().equals(nickname)){
                    createNewMessage(dataSnapshot.getValue(Message.class));
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {

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
        roomChatUpdateListener.addChildEventListener(childRoomChatEventListener);
    }

    private void showDialogInfo(){
        Dialog dialogInfo = new Dialog(this);
        dialogInfo.setContentView(R.layout.dialog_info);
        dialogInfo.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialogInfo.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialogInfo.getWindow().getAttributes().windowAnimations = R.style.AnimationClientDialogInfo;

        TextView textViewTitle = dialogInfo.findViewById(R.id.textViewTitle);
        TextView textViewDesc = dialogInfo.findViewById(R.id.textViewDesc);
        TextView textViewProgress = dialogInfo.findViewById(R.id.textViewProgress);

        textViewTitle.setText("Администратор завершил совместное прослушивание.");
        textViewDesc.setText("Создатель совместного прослушивания не назначил новых администраторов, поэтому совместное прослушивания прекращается.");
        textViewProgress.setEnabled(false);

        Button btnClose = dialogInfo.findViewById(R.id.btnClose);

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogInfo.dismiss();
                finish();
            }
        });

        dialogInfo.show();
    }
    private void createNewMessage(Message newMessage){
        roomChat.add(newMessage);
        messagesAdapter.notifyDataSetChanged();
        recyclerMessage.scrollToPosition(roomChat.size() - 1);
    }

    private void downloadingSong(){
        rooms = firebaseHelper.Request("rooms");

        rooms.orderByKey().equalTo(roomId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    room = snapshot.getValue(Room.class);

                    if(!room.getCurrentSong().equals("") & room.getCurrentSong() != null){
                        storageReference = FirebaseStorage.getInstance().getReference(roomId + "/");
                        storageReference.child(room.getCurrentSong()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                editTextMessage.setEnabled(true);

                                downloadingSongData(uri);

                                player.setMediaItem(MediaItem.fromUri(uri));
                                playerView.setPlayer(player);
                                stateSong(room.getStateSong());
                            }
                        });
                    } else {
                        editTextMessage.setEnabled(true);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
    private void downloadingSongData(Uri uriSong) {
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(String.valueOf(uriSong));
        byte[] data = mmr.getEmbeddedPicture();

        songTitle.setText(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE));
        songArtist.setText(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST));

        //Если обложки нет, то подставляем альтернативную абложку
        if(data == null){
            songAlbum.setImageResource(R.drawable.img_alternativ_song_album);
        } else {
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            songAlbum.setImageBitmap(bitmap);
        }

        try {
            mmr.release();
        } catch (IOException e) {
            throw new RuntimeException(e);
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