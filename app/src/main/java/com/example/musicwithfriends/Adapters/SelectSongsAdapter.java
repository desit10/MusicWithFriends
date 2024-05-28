package com.example.musicwithfriends.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.util.SparseBooleanArray;
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

import java.lang.reflect.Type;
import java.util.ArrayList;

@UnstableApi
public class SelectSongsAdapter extends RecyclerView.Adapter<SelectSongsAdapter.ViewHolder>{

    public static final String APP_PREFERENCES = "mysettings";
    private SharedPreferences mSettings;
    Context context;
    ArrayList<Song> songs;
    ArrayList<Song> roomSongs;
    private SparseBooleanArray selectedItems = new SparseBooleanArray();
    int selectedPosition = 0;


    public SelectSongsAdapter(Context context) {
        this.context = context;

        roomSongs = new ArrayList<>();
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

        return new SelectSongsAdapter.ViewHolder(view);
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
            holder.songAlbum.setImageResource(R.drawable.img_alternativ_song_album);
        } else {
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            holder.songAlbum.setImageBitmap(bitmap);
        }

        holder.songTitle.setText(song.getTitle());
        holder.songArtist.setText(song.getArtist());

        holder.contextMenu.setVisibility(View.GONE);

        holder.frameSong.setSelected(selectedItems.get(position, false));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(roomSongs.remove(song)){
                    selectedItems.delete(holder.getAbsoluteAdapterPosition());
                    holder.frameSong.setSelected(false);
                } else {
                    selectedItems.put(holder.getAbsoluteAdapterPosition(), true);
                    holder.frameSong.setSelected(true);
                    roomSongs.add(song);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(getItemCount());
    }

    public ArrayList<Song> getRoomSongs() {
        return roomSongs;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
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

        @Override
        public void onClick(View v) {
            if (getAbsoluteAdapterPosition() == RecyclerView.NO_POSITION) return;

            notifyItemChanged(selectedPosition);
            selectedPosition = getAbsoluteAdapterPosition();
            notifyItemChanged(selectedPosition);
        }
    }
}
