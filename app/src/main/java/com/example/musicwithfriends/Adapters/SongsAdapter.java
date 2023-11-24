package com.example.musicwithfriends.Adapters;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.fragment.app.FragmentActivity;
import androidx.media3.common.util.UnstableApi;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musicwithfriends.Helpers.SnapHelperOneByOne;
import com.example.musicwithfriends.Models.Playlist;
import com.example.musicwithfriends.Models.Song;
import com.example.musicwithfriends.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;

@UnstableApi
public class SongsAdapter extends RecyclerView.Adapter<SongsAdapter.ViewHolder>{

    public static final String APP_PREFERENCES = "mysettings";
    private SharedPreferences mSettings;
    private SharedPreferences.Editor editor;

    Context context;
    FragmentActivity mainActivity;
    ArrayList<Song> songs;
    CurrentSongsAdapter currentSongsAdapter;
    RecyclerView recyclerCurrentSong;
    SnapHelperOneByOne snapHelperOneByOne;
    Boolean stateAdapter;
    PlaylistsAdapter playlistsAdapter;
    int positionPlaylist;

    public PlaylistsAdapter getPlaylistsAdapter() {
        return playlistsAdapter;
    }

    public void setPlaylistsAdapter(PlaylistsAdapter playlistsAdapter) {
        this.playlistsAdapter = playlistsAdapter;
    }

    public int getPositionPlaylist() {
        return positionPlaylist;
    }

    public void setPositionPlaylist(int positionPlaylist) {
        this.positionPlaylist = positionPlaylist;
    }

    public SongsAdapter(Context context, FragmentActivity mainActivity, ArrayList<Song> songs, Boolean stateAdapter) {
        this.context = context;
        this.mainActivity = mainActivity;
        this.songs = songs;
        this.stateAdapter = stateAdapter;

        mSettings = context.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_song, parent, false);

        return new SongsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {

        Song song = songs.get(position);

        //Создание обложки песни
        android.media.MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(song.getPath());
        byte[] data = mmr.getEmbeddedPicture();

        //Если обложки нет, то подставляем альтернативную абложку
        if(data == null){
            holder.songAlbum.setImageResource(R.drawable.alternativ_song_album);
        } else {
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            holder.songAlbum.setImageBitmap(bitmap);
        }

        holder.songTitle.setText(song.getTitle());
        holder.songArtist.setText(song.getArtist());

        //При нажатии на песню в списке передаём позицию в список текущих песен
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentSongsAdapter = null;

                currentSongsAdapter = new CurrentSongsAdapter(context, recyclerCurrentSong, songs);
                recyclerCurrentSong.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));

                recyclerCurrentSong.setOnFlingListener(null);
                snapHelperOneByOne = new SnapHelperOneByOne();
                snapHelperOneByOne.attachToRecyclerView(recyclerCurrentSong);

                recyclerCurrentSong.setAdapter(currentSongsAdapter);

                currentSongsAdapter.setStateSong(true);
                recyclerCurrentSong.scrollToPosition(position);
            }
        });

        holder.contextMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBottomDialog(getPositionPlaylist(), position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    //При появлении списка на экране создаём список текущих песен
    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

        recyclerCurrentSong = mainActivity.findViewById(R.id.recyclerCurrentSong);
        currentSongsAdapter = new CurrentSongsAdapter(context, recyclerCurrentSong, songs);
        if(stateAdapter){
            recyclerCurrentSong.setTranslationX(-1000f);

            recyclerCurrentSong.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));

            recyclerCurrentSong.setOnFlingListener(null);
            snapHelperOneByOne = new SnapHelperOneByOne();
            snapHelperOneByOne.attachToRecyclerView(recyclerCurrentSong);

            recyclerCurrentSong.setAdapter(currentSongsAdapter);

            recyclerCurrentSong.animate().translationXBy(1000f).setDuration(500).setStartDelay(250).start();

        }
    }

    public void showBottomDialog(int positionPlaylist, int positionSong) {
        Dialog mainDialog = new Dialog(context);
        mainDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mainDialog.setContentView(R.layout.bottom_sheet_layout_song);

        mainDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        mainDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mainDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        mainDialog.getWindow().setGravity(Gravity.BOTTOM);

        LinearLayout layoutAddSongPlaylist = mainDialog.findViewById(R.id.layoutAddSongPlaylist);
        LinearLayout layoutShare = mainDialog.findViewById(R.id.layoutShare);
        LinearLayout layoutDeleteSongPlaylist = mainDialog.findViewById(R.id.layoutDeleteSongPlaylist);
        LinearLayout layoutDeleteSong = mainDialog.findViewById(R.id.layoutDeleteSong);

        ImageView cancelButton = mainDialog.findViewById(R.id.cancelButton);

        View.OnClickListener onClickListener = new View.OnClickListener() {
            DialogPlaylistsAdapter dialogPlaylistsAdapter;
            ArrayList<Playlist> playlists;
            @Override
            public void onClick(View v) {
                Song song = songs.get(positionSong);

                if(v.getId() == R.id.layoutAddSongPlaylist){

                    Gson gson = new Gson();
                    Type typeArrayPlaylist = new TypeToken<ArrayList<Playlist>>(){}.getType();
                    String json = mSettings.getString("Playlists", "");
                    playlists = gson.fromJson(json, typeArrayPlaylist);

                    Dialog dialog = new Dialog(context);
                    dialog.setContentView(R.layout.dialog_add_song_in_playlist);
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
                    dialog.getWindow().getAttributes().windowAnimations = R.style.DialogPlaylistAnimation;

                    RecyclerView playlistsRecycler = dialog.findViewById(R.id.playlistsRecycler);
                    LinearLayoutManager layoutManager = new LinearLayoutManager(context, RecyclerView.VERTICAL, false);
                    dialogPlaylistsAdapter = new DialogPlaylistsAdapter(context, playlists, getPlaylistsAdapter(),
                            getPositionPlaylist(), dialog, mainDialog);
                    dialogPlaylistsAdapter.setSong(song);

                    playlistsRecycler.setLayoutManager(layoutManager);
                    playlistsRecycler.setAdapter(dialogPlaylistsAdapter);

                    TextInputLayout layoutNamePlaylist = dialog.findViewById(R.id.layoutNamePlaylist);
                    TextInputEditText namePlaylist = dialog.findViewById(R.id.namePlaylist);

                    Button btnClose = dialog.findViewById(R.id.btnClose);
                    Button btnAdd = dialog.findViewById(R.id.btnAdd);

                    btnClose.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                            mainDialog.dismiss();
                        }
                    });
                    btnAdd.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            if(namePlaylist.getText().toString().length() > 0){

                                ArrayList<Song> newSongs = new ArrayList<>();
                                newSongs.add(song);
                                Playlist newPlaylist = new Playlist(namePlaylist.getText().toString(), newSongs);

                                namePlaylist.setText("");

                                playlists.add(newPlaylist);

                                Gson gsonOld = new Gson();
                                String jsonOld = gsonOld.toJson(playlists);
                                editor = mSettings.edit();
                                editor.putString("Playlists", jsonOld);
                                editor.apply();

                                getPlaylistsAdapter().setPlaylists(playlists);
                                getPlaylistsAdapter().notifyDataSetChanged();

                                dialog.dismiss();
                                mainDialog.dismiss();
                            } else {
                                layoutNamePlaylist.setErrorEnabled(true);
                                layoutNamePlaylist.setError("Введите название плейлиста!");

                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        layoutNamePlaylist.setErrorEnabled(false);
                                    }
                                }, 2000);
                            }

                        }
                    });

                    dialog.show();

                }
                if(v.getId() == R.id.layoutShare){

                    Uri uriSong = FileProvider.getUriForFile(context, "com.example.musicwithfriends.provider", new File(song.getPath()));
                    Intent share = new Intent(Intent.ACTION_SEND);
                    share.setType("audio/*");
                    share.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    share.putExtra(Intent.EXTRA_STREAM, uriSong);
                    context.startActivity(Intent.createChooser(share, null));

                    mainDialog.dismiss();

                }
                if(v.getId() == R.id.layoutDeleteSongPlaylist){

                    Gson gson = new Gson();
                    Type typeArrayPlaylist = new TypeToken<ArrayList<Playlist>>(){}.getType();
                    String json = mSettings.getString("Playlists", "");
                    playlists = gson.fromJson(json, typeArrayPlaylist);

                    playlists.get(positionPlaylist).getSongs().remove(positionSong);

                    Gson gsonOld = new Gson();
                    String jsonOld = gsonOld.toJson(playlists);
                    editor = mSettings.edit();
                    editor.putString("Playlists", jsonOld);
                    editor.apply();

                    getPlaylistsAdapter().setPlaylists(playlists);
                    getPlaylistsAdapter().notifyDataSetChanged();
                    songs = playlists.get(positionPlaylist).getSongs();
                    notifyItemRemoved(positionSong);

                    mainDialog.dismiss();
                }
                if(v.getId() == R.id.layoutDeleteSong){

                    Gson gson = new Gson();
                    Type typeArrayPlaylist = new TypeToken<ArrayList<Playlist>>(){}.getType();
                    String json = mSettings.getString("Playlists", "");
                    playlists = gson.fromJson(json, typeArrayPlaylist);

                    for (int i = 0; i < playlists.size(); i++){

                        for (int j = 0; j < playlists.get(i).getSongs().size(); j++){

                            if(song.getId().equals(playlists.get(i).getSongs().get(j).getId())){
                               playlists.get(i).getSongs().remove(j);
                            }

                        }

                    }

                    Gson gsonOld = new Gson();
                    String jsonOld = gsonOld.toJson(playlists);
                    editor = mSettings.edit();
                    editor.putString("Playlists", jsonOld);
                    editor.apply();

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        if(Environment.isExternalStorageManager(new File(song.getPath()))){
                            File deleteSongFile = new File(song.getPath());
                            deleteSongFile.delete();

                            songs = playlists.get(positionPlaylist).getSongs();
                            notifyItemRemoved(positionSong);
                            getPlaylistsAdapter().setPlaylists(playlists);
                            getPlaylistsAdapter().notifyDataSetChanged();
                        } else {
                            Uri uri = Uri.parse("package:com.example.musicwithfriends");
                            mainActivity.startActivity(new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, uri));
                        }
                    } else {
                        File deleteSongFile = new File(song.getPath());
                        if(!deleteSongFile.delete()){
                            Toast.makeText(context, "Разрешите в настройках приложению управлять файлами устройства", Toast.LENGTH_SHORT).show();
                        } else {
                            songs = playlists.get(positionPlaylist).getSongs();
                            notifyItemRemoved(positionSong);
                            getPlaylistsAdapter().setPlaylists(playlists);
                            getPlaylistsAdapter().notifyDataSetChanged();
                        }
                    }
                    mainDialog.dismiss();

                }
                if(v.getId() == R.id.cancelButton){
                    mainDialog.dismiss();
                }
            }
        };
        layoutAddSongPlaylist.setOnClickListener(onClickListener);
        layoutShare.setOnClickListener(onClickListener);
        layoutDeleteSongPlaylist.setOnClickListener(onClickListener);
        layoutDeleteSong.setOnClickListener(onClickListener);
        cancelButton.setOnClickListener(onClickListener);

        mainDialog.show();
    }

    public long getFilePathToMediaID(String songPath, Context context)
    {
        long id = 0;
        ContentResolver cr = context.getContentResolver();

        Uri uri = MediaStore.Files.getContentUri("external");
        String selection = MediaStore.Audio.Media.DATA;
        String[] selectionArgs = {songPath};
        String[] projection = {MediaStore.Audio.Media._ID};

        Cursor cursor = cr.query(uri, projection, selection + "=?", selectionArgs, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                int idIndex = cursor.getColumnIndex(MediaStore.Audio.Media._ID);
                id = Long.parseLong(cursor.getString(idIndex));
            }
        }

        return id;
    }

    public  class ViewHolder extends RecyclerView.ViewHolder{

        FrameLayout frameSong;
        ImageView songAlbum;
        ImageButton contextMenu;
        TextView songTitle, songArtist;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            frameSong = itemView.findViewById(R.id.frameSong);

            songAlbum = itemView.findViewById(R.id.songAlbum);

            contextMenu = itemView.findViewById(R.id.contextMenu);

            songTitle = itemView.findViewById(R.id.songTitle);
            songArtist = itemView.findViewById(R.id.songArtist);
        }
    }
}
