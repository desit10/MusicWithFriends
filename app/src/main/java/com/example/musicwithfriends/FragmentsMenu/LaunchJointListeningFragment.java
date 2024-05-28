package com.example.musicwithfriends.FragmentsMenu;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.transition.TransitionInflater;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.fragment.app.Fragment;
import androidx.media3.common.util.UnstableApi;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musicwithfriends.Adapters.SelectSongsAdapter;
import com.example.musicwithfriends.Helpers.FirebaseHelper;
import com.example.musicwithfriends.Helpers.NetworkHelper;
import com.example.musicwithfriends.Models.Message;
import com.example.musicwithfriends.Models.Room;
import com.example.musicwithfriends.Models.Song;
import com.example.musicwithfriends.R;
import com.example.musicwithfriends.HostActivity;
import com.google.firebase.database.DatabaseReference;


import java.util.ArrayList;

@UnstableApi
public class LaunchJointListeningFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TransitionInflater inflater = TransitionInflater.from(requireContext());
        this.setEnterTransition(inflater.inflateTransition(R.transition.slide_left));
        this.setExitTransition(inflater.inflateTransition(R.transition.slide_right));
    }

    private EditText editTextNickname;
    private ImageButton btnImageAddAvatar;
    private DatabaseReference roomId;
    private Room room;
    private String nickname;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_launch_joint_listening, container, false);

        NetworkHelper networkHelper = new NetworkHelper(getContext());

        Button btnStartJointListening = view.findViewById(R.id.btnStartJointListening);
        btnImageAddAvatar = view.findViewById(R.id.btnImageAddAvatar);
        editTextNickname = view.findViewById(R.id.editTextNickname);

        RecyclerView songsRecycler = view.findViewById(R.id.recyclerSongs);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        SelectSongsAdapter selectSongsAdapter = new SelectSongsAdapter(getContext());

        songsRecycler.setLayoutManager(layoutManager);
        songsRecycler.setAdapter(selectSongsAdapter);

        btnStartJointListening.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(networkHelper.checkNetworkConnection()){
                    nickname = editTextNickname.getText().toString().trim();
                    if(!TextUtils.isEmpty(nickname)){
                        FirebaseHelper firebaseHelper = new FirebaseHelper();
                        DatabaseReference rooms = firebaseHelper.Request("rooms");

                        if(selectSongsAdapter.getItemCount() != 0) {
                            createRoomJointListening(selectSongsAdapter, rooms);
                        } else {
                            room = new Room(new ArrayList<Song>(), 0, false, "", new ArrayList<Message>());

                            roomId = rooms.push();
                            roomId.setValue(room);

                            startActivityHost(roomId.getKey(), room, nickname);
                        }
                    }
                }
            }
        });

        return view;
    }

    private void createRoomJointListening(SelectSongsAdapter selectSongsAdapter, DatabaseReference rooms){
        ArrayList<Song> roomPlaylist = selectSongsAdapter.getRoomSongs();

        if(roomPlaylist.size() > 0){
            room = new Room(roomPlaylist, 0, false, roomPlaylist.get(0).getSongName(), new ArrayList<Message>());
        } else {
            room = new Room(new ArrayList<Song>(), 0, false, "", new ArrayList<Message>());
        }

        roomId = rooms.push();
        roomId.setValue(room);

        startActivityHost(roomId.getKey(), room, nickname);
    }
    private void startActivityHost(String roomId, Room room, String nickname){
        Intent roomIntent = new Intent(getContext(), HostActivity.class);
        roomIntent.putExtra("ROOM_ID", roomId);
        roomIntent.putExtra("ROOM", room);
        roomIntent.putExtra("NICKNAME", nickname);

        editTextNickname.setText("");

        startActivity(roomIntent);
    }
}