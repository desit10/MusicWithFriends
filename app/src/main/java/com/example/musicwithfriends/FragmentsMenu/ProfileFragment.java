package com.example.musicwithfriends.FragmentsMenu;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.transition.TransitionInflater;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musicwithfriends.Adapters.DialogSongsAdapter;
import com.example.musicwithfriends.Helpers.FirebaseHelper;
import com.example.musicwithfriends.Models.Room;
import com.example.musicwithfriends.Models.Song;
import com.example.musicwithfriends.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //if (getArguments() != null) {
        //    mParam1 = getArguments().getString(ARG_PARAM1);
        //    mParam2 = getArguments().getString(ARG_PARAM2);
        //}
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

                Dialog dialog = new Dialog(getContext());
                dialog.setContentView(R.layout.dialog_connection_in_room);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
                dialog.getWindow().getAttributes().windowAnimations = R.style.DialogPlaylistAnimation;

                RecyclerView songsRecycler = dialog.findViewById(R.id.songsRecycler);
                LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);

                DialogSongsAdapter dialogSongsAdapter = new DialogSongsAdapter(getContext());

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

                        Room room = new Room(roomPlaylist, 0, 0, 0);

                        DatabaseReference roomId = rooms.push();
                        roomId.setValue(room);

                        for(int i = 0; i < roomPlaylist.size(); i++){
                            Uri uriFile = Uri.fromFile(new File(roomPlaylist.get(i).getPath()));

                            StorageReference storageRef = FirebaseStorage.getInstance().getReference();
                            StorageReference ref = storageRef.child(roomId.getKey() + "/" + roomPlaylist.get(i).getSongName());

                            ref.putFile(uriFile);
                        }

                        dialog.dismiss();

                        Intent sendIntent = new Intent();
                        sendIntent.setAction(Intent.ACTION_SEND);
                        sendIntent.putExtra(Intent.EXTRA_TEXT, "http://musicwithfriends/" + roomId.getKey());
                        sendIntent.setType("text/plain");

                        Intent shareIntent = Intent.createChooser(sendIntent, null);
                        startActivity(shareIntent);

                    }
                });

                dialog.show();
            }
        });

        return view;
    }
}