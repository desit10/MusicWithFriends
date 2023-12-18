package com.example.musicwithfriends.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.media3.common.util.UnstableApi;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musicwithfriends.Models.Playlist;
import com.example.musicwithfriends.Models.Song;
import com.example.musicwithfriends.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;

@UnstableApi
public class DialogSongsAdapter extends RecyclerView.Adapter<DialogSongsAdapter.ViewHolder>{

    public static final String APP_PREFERENCES = "mysettings";
    private SharedPreferences mSettings;
    Context context;
    ArrayList<Song> songs;
    ArrayList<Song> roomSong;

    public DialogSongsAdapter(Context context) {
        this.context = context;

        roomSong = new ArrayList<>();
        mSettings = context.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);

        Gson gson;
        Type typeArrayPlaylist;
        String json;
        ArrayList<Playlist> playlists;

        gson = new Gson();
        typeArrayPlaylist = new TypeToken<ArrayList<Playlist>>(){}.getType();
        json = mSettings.getString("Playlists", "");
        playlists = gson.fromJson(json, typeArrayPlaylist);

        songs = playlists.get(0).getSongs();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_song, parent, false);

        return new DialogSongsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {

        Song song = songs.get(position);

        //Создание обложки песни
        if(new File(song.getPath()).exists()){
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
        } else {
            holder.songAlbum.setImageResource(R.drawable.alternativ_song_album);
        }

        holder.songTitle.setText(song.getTitle());
        holder.songArtist.setText(song.getArtist());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(roomSong.remove(song)){
                    holder.frameSong.setBackgroundResource(R.color.smoky_white);
                } else {
                    holder.frameSong.setBackgroundResource(R.color.blue);
                    roomSong.add(song);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    public ArrayList<Song> getRoomSong() {
        return roomSong;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

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
