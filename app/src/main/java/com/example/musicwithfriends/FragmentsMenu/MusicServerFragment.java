package com.example.musicwithfriends.FragmentsMenu;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.transition.TransitionInflater;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.media3.common.util.UnstableApi;

import com.example.musicwithfriends.R;

@UnstableApi
public class MusicServerFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TransitionInflater inflater = TransitionInflater.from(requireContext());
        this.setEnterTransition(inflater.inflateTransition(R.transition.slide_left));
        this.setExitTransition(inflater.inflateTransition(R.transition.slide_right));
    }

    @SuppressLint("NewApi")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_musics_server, container, false);

        return view;
    }
}