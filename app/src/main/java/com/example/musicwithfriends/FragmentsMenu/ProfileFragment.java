package com.example.musicwithfriends.FragmentsMenu;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.transition.TransitionInflater;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.media3.common.util.UnstableApi;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musicwithfriends.Adapters.DialogSongsAdapter;
import com.example.musicwithfriends.Helpers.FirebaseHelper;
import com.example.musicwithfriends.Models.Room;
import com.example.musicwithfriends.Models.Song;
import com.example.musicwithfriends.R;
import com.example.musicwithfriends.HostActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.ArrayList;

@UnstableApi
public class ProfileFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TransitionInflater inflater = TransitionInflater.from(requireContext());
        this.setEnterTransition(inflater.inflateTransition(R.transition.slide_left));
        this.setExitTransition(inflater.inflateTransition(R.transition.slide_right));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        Button addFriends = view.findViewById(R.id.addFriends);

        addFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseHelper firebaseHelper = new FirebaseHelper();
                DatabaseReference rooms = firebaseHelper.Request("rooms");

                DialogSongsAdapter dialogSongsAdapter = new DialogSongsAdapter(getContext());

                if(dialogSongsAdapter.getItemCount() != 0) {
                    Dialog dialog = new Dialog(getContext());
                    dialog.setContentView(R.layout.dialog_connection_in_room);
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
                    dialog.getWindow().getAttributes().windowAnimations = R.style.DialogPlaylistAnimation;

                    RecyclerView songsRecycler = dialog.findViewById(R.id.songsRecycler);
                    LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);

                    songsRecycler.setLayoutManager(layoutManager);
                    songsRecycler.setAdapter(dialogSongsAdapter);

                    Button btnClose = dialog.findViewById(R.id.btnClose);
                    Button btnSave = dialog.findViewById(R.id.btnSave);

                    btnClose.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });
                    btnSave.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            ArrayList<Song> roomPlaylist = dialogSongsAdapter.getRoomSong();

                            Room room = new Room(roomPlaylist, 0, false, roomPlaylist.get(0).getSongName());

                            DatabaseReference roomId = rooms.push();
                            roomId.setValue(room);

                            if(roomPlaylist.size() == 0){
                                dialog.dismiss();

                                Intent roomIntent = new Intent(getContext(), HostActivity.class);
                                roomIntent.putExtra("ROOM_ID", roomId.getKey());
                                startActivity(roomIntent);

                                Intent sendIntent = new Intent();
                                sendIntent.setAction(Intent.ACTION_SEND);
                                sendIntent.putExtra(Intent.EXTRA_TEXT, "http://musicwithfriends/" + roomId.getKey());
                                sendIntent.setType("text/plain");

                                Intent shareIntent = Intent.createChooser(sendIntent, null);
                                startActivity(shareIntent);
                            } else {
                                for(int i = 0; i < roomPlaylist.size(); i++){
                                    Uri uriFile = Uri.fromFile(new File(roomPlaylist.get(i).getPath()));

                                    StorageReference storageRef = FirebaseStorage.getInstance().getReference();
                                    StorageReference ref = storageRef.child(roomId.getKey() + "/" + roomPlaylist.get(i).getSongName());

                                    UploadTask uploadTask = ref.putFile(uriFile);
                                    int counter = i;
                                    uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                            uploadTask.cancel();
                                            if(counter == roomPlaylist.size() - 1){
                                                dialog.dismiss();

                                                Intent roomIntent = new Intent(getContext(), HostActivity.class);
                                                roomIntent.putExtra("ROOM_ID", roomId.getKey());
                                                startActivity(roomIntent);

                                                Intent sendIntent = new Intent();
                                                sendIntent.setAction(Intent.ACTION_SEND);
                                                sendIntent.putExtra(Intent.EXTRA_TEXT, "http://musicwithfriends/" + roomId.getKey());
                                                sendIntent.setType("text/plain");

                                                Intent shareIntent = Intent.createChooser(sendIntent, null);
                                                startActivity(shareIntent);
                                            }
                                        }
                                    });
                                }
                            }
                        }
                    });

                    dialog.show();
                } else {
                    Room room = new Room(null, 0, false, null);

                    DatabaseReference roomId = rooms.push();
                    roomId.setValue(room);

                    Intent roomIntent = new Intent(getContext(), HostActivity.class);
                    roomIntent.putExtra("ROOM_ID", roomId.getKey());
                    startActivity(roomIntent);

                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, "http://musicwithfriends/" + roomId.getKey());
                    sendIntent.setType("text/plain");

                    Intent shareIntent = Intent.createChooser(sendIntent, null);
                    startActivity(shareIntent);
                }

            }
        });

        return view;
    }
}