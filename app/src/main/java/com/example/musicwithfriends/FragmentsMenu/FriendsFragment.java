package com.example.musicwithfriends.FragmentsMenu;

import android.os.Bundle;
import android.transition.TransitionInflater;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.media3.common.util.UnstableApi;

import com.example.musicwithfriends.R;

@UnstableApi
public class FriendsFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TransitionInflater inflater = TransitionInflater.from(requireContext());
        this.setEnterTransition(inflater.inflateTransition(R.transition.slide_left));
        this.setExitTransition(inflater.inflateTransition(R.transition.slide_right));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friends, container, false);

        /*PlayerView playerView = view.findViewById(R.id.player);

        ExoPlayer player = new ExoPlayer.Builder(getContext())
                .setSeekForwardIncrementMs(5000)
                .build();

        StorageReference storageReference = FirebaseStorage.getInstance().getReference("-NljjSp6WfQ5Kfg8xZv9/");
        storageReference.child("Imagine_Dragons_-_Bones_73949726.mp3").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                player.setMediaItem(MediaItem.fromUri(uri));
                player.prepare();
                playerView.setPlayer(player);
            }
        });*/

        return view;
    }

}