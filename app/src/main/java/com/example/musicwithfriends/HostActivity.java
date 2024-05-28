package com.example.musicwithfriends;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.FileProvider;
import androidx.media3.common.util.UnstableApi;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.musicwithfriends.Adapters.CurrentSongsWithFriendsAdapter;
import com.example.musicwithfriends.Adapters.MessagesAdapter;
import com.example.musicwithfriends.Adapters.SongsWithFriendsAdapter;
import com.example.musicwithfriends.Helpers.FirebaseHelper;
import com.example.musicwithfriends.Helpers.MediaHelpers;
import com.example.musicwithfriends.Helpers.SnapHelperOneByOne;
import com.example.musicwithfriends.Models.Message;
import com.example.musicwithfriends.Models.Room;
import com.example.musicwithfriends.Models.Song;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

@UnstableApi
public class HostActivity extends AppCompatActivity {

    private TextView textViewProgressHeader;
    private RecyclerView songsRecycler, currentSongsRecycler, recyclerMessage;
    private SongsWithFriendsAdapter songsWithFriendsAdapter;
    private CurrentSongsWithFriendsAdapter currentSongsWithFriendsAdapter;
    private FloatingActionButton fabBtnAddFriends;
    private EditText editTextMessage;
    private MessagesAdapter messagesAdapter;

    private FirebaseHelper firebaseHelper = new FirebaseHelper();
    private StorageReference storageRef;
    private DatabaseReference roomChatUpdateListener;

    private Room room;
    private String roomId, nickname;
    private ArrayList<Song> roomPlaylist;
    private ArrayList<Message> roomChat;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host);

        textViewProgressHeader = findViewById(R.id.textViewProgressHeader);
        fabBtnAddFriends = findViewById(R.id.fabBtnAddFriends);
        recyclerMessage = findViewById(R.id.recyclerMessages);
        editTextMessage = findViewById(R.id.editTextMessage);

        roomId = getIntent().getStringExtra("ROOM_ID");
        room = (Room) getIntent().getSerializableExtra("ROOM");
        nickname = getIntent().getStringExtra("NICKNAME");

        storageRef = FirebaseStorage.getInstance().getReference().child(roomId);

        roomChat = new ArrayList<>();
        roomPlaylist = room.getRoomPlaylist();

        if(roomPlaylist.size() > 0) {
            fabBtnAddFriends.setEnabled(false);

            uploadingSongsToTheStorage();

            songsRecycler = findViewById(R.id.songsRecycler);
            currentSongsRecycler = findViewById(R.id.recyclerCurrentSong);

            songsRecycler.setLayoutManager(
                    new LinearLayoutManager(HostActivity.this, LinearLayoutManager.VERTICAL, false));
            songsWithFriendsAdapter =
                    new SongsWithFriendsAdapter(HostActivity.this, currentSongsRecycler, room, roomId);
            songsRecycler.setAdapter(songsWithFriendsAdapter);


            currentSongsRecycler.setLayoutManager(
                    new LinearLayoutManager(HostActivity.this, LinearLayoutManager.HORIZONTAL, false));
            currentSongsWithFriendsAdapter =
                    new CurrentSongsWithFriendsAdapter(HostActivity.this, currentSongsRecycler, room, roomId);

            currentSongsRecycler.setOnFlingListener(null);
            SnapHelperOneByOne snapHelperOneByOne = new SnapHelperOneByOne();
            snapHelperOneByOne.attachToRecyclerView(currentSongsRecycler);


            currentSongsRecycler.setAdapter(currentSongsWithFriendsAdapter);

            currentSongsWithFriendsAdapter.setStateSong(room.getStateSong());
            currentSongsRecycler.scrollToPosition(0);
        }

        messagesAdapter = new MessagesAdapter(this, roomChat, nickname);

        recyclerMessage.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerMessage.setAdapter(messagesAdapter);

        fabBtnAddFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startShareIntentLink();
            }
        });

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

        roomChatUpdateListener = firebaseHelper.Request("rooms/" + roomId + "/roomChat/");
        ChildEventListener childEventListener = new ChildEventListener() {
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
        roomChatUpdateListener.addChildEventListener(childEventListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        firebaseHelper.Request("rooms").orderByChild(roomId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    if(dataSnapshot.getKey().equals(roomId)){
                        dataSnapshot.getRef().removeValue();
                        return;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        if(roomPlaylist != null){
            for(Song song : roomPlaylist){
                StorageReference songRef = storageRef.child(song.getSongName());
                songRef.delete();
            }
        }

    }

    private void createNewMessage(Message newMessage){
        roomChat.add(newMessage);
        messagesAdapter.notifyDataSetChanged();
        recyclerMessage.scrollToPosition(roomChat.size() - 1);
    }

    private Dialog dialogInfo;
    private TextView dialogInfoTextViewProgress;
    private void showDialogSongsUploadProgress(){
        dialogInfo = new Dialog(this);
        dialogInfo.setContentView(R.layout.dialog_info);
        dialogInfo.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialogInfo.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialogInfo.getWindow().getAttributes().windowAnimations = R.style.AnimationDialog;

        dialogInfoTextViewProgress = dialogInfo.findViewById(R.id.textViewProgress);
        Button btnClose = dialogInfo.findViewById(R.id.btnClose);

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogInfo.dismiss();
                textViewProgressHeader.setVisibility(View.VISIBLE);
            }
        });

        dialogInfo.show();
    }

    private void uploadingSongsToTheStorage(){
        showDialogSongsUploadProgress();

        for(int i = 0; i < roomPlaylist.size(); i++){
            Uri uriSong = Uri.fromFile(new File(roomPlaylist.get(i).getPath()));

            StorageReference songRef = storageRef.child(roomPlaylist.get(i).getSongName());

            int counter = i;

            UploadTask uploadSong = songRef.putFile(uriSong);
            uploadSong.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                    double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                    String progressStr = String.format(
                            "Загрузка песни \"%s - %s\"  %.2f%%...",
                            roomPlaylist.get(counter).getArtist(),
                            roomPlaylist.get(counter).getTitle(),
                            progress
                    );
                    if(dialogInfo.isShowing()){
                        dialogInfoTextViewProgress.setText(progressStr);
                    } else {
                        textViewProgressHeader.setText(progressStr);
                    }
                }
            }).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    uploadSong.cancel();

                    if(counter == roomPlaylist.size() - 1){
                        fabBtnAddFriends.setEnabled(true);

                        if(dialogInfo.isShowing()){
                            dialogInfo.dismiss();
                        }

                        if(textViewProgressHeader.getVisibility() == View.VISIBLE){
                            textViewProgressHeader.setVisibility(View.GONE);
                        }

                        startShareIntentLink();
                    }
                }
            });
        }
    }

    private void startShareIntentLink(){
        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.setType("text/plain");

        sendIntent.putExtra(Intent.EXTRA_TITLE, "Music with friends");
        sendIntent.putExtra(
                Intent.EXTRA_TEXT,
                "https://firebasestorage.googleapis.com/v0/b/musicdb2.appspot.com/o/Share%20image.jpg?alt=media&token=72739745-1908-4646-a42e-f3992703c4b5\n\n\n" +
                    "Music with friends\n" +
                    "Приглашаем насладиться музыкой вместе с друзьями!!!\n" +
                    "Перейдите по данной ссылке: " +
                    "https://musicwithfriends.com/" + roomId
                );

        Intent shareIntent = Intent.createChooser(sendIntent, null);
        startActivity(shareIntent);
    }
}