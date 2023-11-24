package com.example.musicwithfriends.FragmentsMenu;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.media3.common.util.UnstableApi;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musicwithfriends.Adapters.PlaylistsAdapter;
import com.example.musicwithfriends.Adapters.SongsAdapter;
import com.example.musicwithfriends.Helpers.SnapHelperOneByOne;
import com.example.musicwithfriends.Models.Playlist;
import com.example.musicwithfriends.Models.Song;
import com.example.musicwithfriends.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;

@UnstableApi public class MusicsFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public MusicsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment NewsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MusicsFragment newInstance(String param1, String param2) {
        MusicsFragment fragment = new MusicsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        TransitionInflater inflater = TransitionInflater.from(requireContext());
        this.setEnterTransition(inflater.inflateTransition(R.transition.slide_left));
        this.setExitTransition(inflater.inflateTransition(R.transition.slide_right));
    }


    public static final String APP_PREFERENCES = "mysettings";
    private SharedPreferences mSettings;
    private SharedPreferences.Editor editor;
    RecyclerView recyclerSongs, recyclerPlaylist;
    PlaylistsAdapter playlistsAdapter;
    ConstraintLayout layoutPlaylistsRecycler;
    LinearLayout layoutPlaylistsManagement;
    TextView textPlaylistsManagement;
    ImageButton playlistsManagement;
    ArrayList<Song> songs;
    Boolean statePlaylistRecycler = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_musics, container, false);

        mSettings = getContext().getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);

        songs = new ArrayList<Song>();
        recyclerSongs = view.findViewById(R.id.songsRecycler);
        recyclerPlaylist = view.findViewById(R.id.playlistsRecycler);
        layoutPlaylistsRecycler = view.findViewById(R.id.layoutPlaylistsRecycler);
        layoutPlaylistsManagement = view.findViewById(R.id.layoutPlaylistsManagement);
        textPlaylistsManagement = view.findViewById(R.id.textPlaylistsManagement);
        playlistsManagement = view.findViewById(R.id.playlistsManagement);

        //Проверка на допуск к хранилищу
        if (checkPermission() == false){
            requestPermission();
            return view;
        }

        /*getActivity().getSupportFragmentManager()
                .beginTransaction()
                .detach(this)
                .attach(this)
                .commit();*/


        //Вывод песен из хранилища
        showSongs();

        layoutPlaylistsManagement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeRecyclerPlaylist();
            }
        });
        playlistsManagement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeRecyclerPlaylist();
            }
        });

        return view;
    }

    boolean checkPermission(){

        int result = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE);

        if(result == PackageManager.PERMISSION_GRANTED){
            return true;
        } else {
            return false;
        }

    }

    void requestPermission(){
        if(ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)){
            Toast.makeText(getContext(), "Разрешите в настройках приложению доступ к файлам", Toast.LENGTH_SHORT).show();
        } else {
            ActivityCompat.requestPermissions(
                    getActivity(),
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    123);
        }
    }

    void showSongs(){

        String[] projection;

        if(android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.Q){
            projection = new String[]{
                    MediaStore.Audio.Media._ID,
                    MediaStore.Audio.Media.DATA,
                    MediaStore.Audio.Media.TITLE,
                    MediaStore.Audio.Media.ARTIST,
                    MediaStore.Audio.Media.GENRE
            };

            String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";

            Cursor cursor = getContext().getContentResolver().query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    projection,
                    selection,
                    null,
                    null);

            while (cursor.moveToNext()){

                Song song = new Song(
                        cursor.getString(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getString(4)
                );

                if(new File(song.getPath()).exists()){
                    songs.add(song);
                }

            }
        } else{
            projection = new String[]{
                    MediaStore.Audio.Media._ID,
                    MediaStore.Audio.Media.DATA,
                    MediaStore.Audio.Media.TITLE,
                    MediaStore.Audio.Media.ARTIST
            };

            String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";

            Cursor cursor = getContext().getContentResolver().query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    projection,
                    selection,
                    null,
                    null);

            while (cursor.moveToNext()){

                Song song = new Song(
                        cursor.getString(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        ""
                );

                if(new File(song.getPath()).exists()){
                    songs.add(song);
                }

            }
        }

        if(songs.size() == 0){
            Toast.makeText(getContext(), "Cписок музыки пуст", Toast.LENGTH_SHORT).show();
        } else {
            View.OnClickListener onClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    hideRecyclerPlaylist();
                }
            };

            Gson gson;
            Type typeArrayPlaylist;
            String json;
            ArrayList<Playlist> playlists;

            gson = new Gson();
            typeArrayPlaylist = new TypeToken<ArrayList<Playlist>>(){}.getType();
            json = mSettings.getString("Playlists", "");
            playlists = gson.fromJson(json, typeArrayPlaylist);

            if(playlists == null){
                Playlist playlist = new Playlist("Все треки", songs);
                ArrayList<Playlist> playlistsOld = new ArrayList<Playlist>();
                playlistsOld.add(playlist);

                Gson gsonOld = new Gson();
                String jsonOld = gsonOld.toJson(playlistsOld);
                editor = mSettings.edit();
                editor.putString("Playlists", jsonOld);
                editor.apply();

                gson = new Gson();
                typeArrayPlaylist = new TypeToken<ArrayList<Playlist>>(){}.getType();
                json = mSettings.getString("Playlists", "");
                playlists = gson.fromJson(json, typeArrayPlaylist);
            }

            playlists.get(0).setSongs(songs);

            playlistsAdapter =
                    new PlaylistsAdapter(getContext(), getActivity(), playlists, recyclerPlaylist, recyclerSongs, onClickListener);

            LinearLayoutManager linearLayoutManager =
                    new LinearLayoutManager(getContext(),  LinearLayoutManager.VERTICAL, false);

            recyclerPlaylist.setLayoutManager(linearLayoutManager);

            recyclerPlaylist.setAdapter(playlistsAdapter);

            recyclerPlaylist.suppressLayout(true);
        }
    }

    void changeRecyclerPlaylist(){

        ValueAnimator anim;
        int playlistsRecyclerHeight = (int) getResources().getDimension(R.dimen.get_playlists_recycler_height);
        recyclerPlaylist.suppressLayout(false);

        if(!statePlaylistRecycler){
            anim = ValueAnimator.ofInt( playlistsRecyclerHeight, 700);
            textPlaylistsManagement.setText("Скрыть плейлисты");
            playlistsManagement.setImageResource(R.drawable.up_arrow);
            statePlaylistRecycler = true;
        } else {
            anim = ValueAnimator.ofInt(recyclerPlaylist.getMeasuredHeight(), playlistsRecyclerHeight);
            textPlaylistsManagement.setText("Показать плейлисты");
            playlistsManagement.setImageResource(R.drawable.down_arrow);
            statePlaylistRecycler = false;
        }

        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(@NonNull ValueAnimator valueAnimator) {
                int value = (Integer) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = recyclerPlaylist.getLayoutParams();
                layoutParams.height = value;
                recyclerPlaylist.requestLayout();
            }
        });

        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                if(!statePlaylistRecycler && playlistsAdapter.getPositionItem() != 0){
                    recyclerPlaylist.scrollToPosition(playlistsAdapter.getPositionItem());
                }

                recyclerPlaylist.suppressLayout(true);
            }
        });

        anim.setDuration(250);
        anim.start();
    }
    void hideRecyclerPlaylist(){
        textPlaylistsManagement.setText("Показать плейлисты");
        playlistsManagement.setImageResource(R.drawable.down_arrow);
        statePlaylistRecycler = false;
    }

}