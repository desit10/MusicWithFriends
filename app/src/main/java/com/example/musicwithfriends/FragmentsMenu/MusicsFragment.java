package com.example.musicwithfriends.FragmentsMenu;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
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
        setExitTransition(inflater.inflateTransition(R.transition.slide_right));
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
    SnapHelperOneByOne snapHelperOneByOne;
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


        /*ArrayList<Song> songs2 = new ArrayList<Song>();
        songs2.add(songs.get(0));
        ArrayList<Song> songs3 = new ArrayList<Song>();
        songs3.add(songs.get(1));
        songs3.add(songs.get(2));
        ArrayList<Song> songs4 = new ArrayList<Song>();
        songs4.add(songs.get(3));
        songs4.add(songs.get(4));
        songs4.add(songs.get(5));
        ArrayList<Song> songs5 = new ArrayList<Song>();
        songs5.add(songs.get(6));
        songs5.add(songs.get(7));
        songs5.add(songs.get(8));
        songs5.add(songs.get(9));
        ArrayList<Song> songs6 = new ArrayList<Song>();
        songs6.add(songs.get(10));
        songs6.add(songs.get(11));
        songs6.add(songs.get(12));
        songs6.add(songs.get(1));
        songs6.add(songs.get(2));

        Playlist playlist = new Playlist("Все треки", songs);
        Playlist playlist2 = new Playlist("Треки2", songs2);
        Playlist playlist3 = new Playlist("Треки3", songs3);
        Playlist playlist4 = new Playlist("Треки4", songs4);
        Playlist playlist5 = new Playlist("Треки5", songs5);
        Playlist playlist6 = new Playlist("Треки6", songs6);

        ArrayList<Playlist> playlists = new ArrayList<Playlist>();
        playlists.add(playlist);
        playlists.add(playlist2);
        playlists.add(playlist3);
        playlists.add(playlist4);
        playlists.add(playlist5);
        playlists.add(playlist6);

        Gson gson = new Gson();
        String json = gson.toJson(playlists);
        editor = mSettings.edit();
        editor.putString("Playlists", json);
        editor.apply();

        Log.e("Playlists", json);*/

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
            Toast.makeText(getContext(), "Разрешите приложению в настройках брать песни из хранилища", Toast.LENGTH_SHORT).show();
        } else {
            ActivityCompat.requestPermissions(
                    getActivity(),
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    123);
        }

    }

    void showSongs(){
        String[] projection = {
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM
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
                    cursor.getString(3)
            );

            if(new File(song.getPath()).exists()){
                songs.add(song);
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

            Gson gson = new Gson();
            Type typeArrayPlaylist = new TypeToken<ArrayList<Playlist>>(){}.getType();
            String json = mSettings.getString("Playlists", "");
            ArrayList<Playlist> playlists = gson.fromJson(json, typeArrayPlaylist);

            playlistsAdapter =
                    new PlaylistsAdapter(getContext(), getActivity(), playlists, recyclerPlaylist, recyclerSongs, onClickListener);

            layoutPlaylistsRecycler.setTranslationX(-1000f);

            LinearLayoutManager linearLayoutManager =
                    new LinearLayoutManager(getContext(),  LinearLayoutManager.VERTICAL, false);

            recyclerPlaylist.setLayoutManager(linearLayoutManager);

            recyclerPlaylist.setAdapter(playlistsAdapter);

            layoutPlaylistsRecycler.animate().translationXBy(1000f).setDuration(500).setStartDelay(250).start();

        }
    }

    void changeRecyclerPlaylist(){

        ValueAnimator anim;
        int playlistsRecyclerHeight = (int) getResources().getDimension(R.dimen.get_playlists_recycler_height);

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