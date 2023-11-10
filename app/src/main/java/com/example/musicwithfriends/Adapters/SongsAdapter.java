package com.example.musicwithfriends.Adapters;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;
import androidx.media3.common.MediaItem;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.musicwithfriends.Helpers.SnapHelperOneByOne;
import com.example.musicwithfriends.Models.Song;
import com.example.musicwithfriends.R;
import com.example.musicwithfriends.SongPlayerActivity;

import java.io.File;
import java.util.ArrayList;

@UnstableApi
public class SongsAdapter extends RecyclerView.Adapter<SongsAdapter.ViewHolder>{

    Context context;
    FragmentActivity mainActivity;
    ArrayList<Song> songs;
    CurrentSongsAdapter currentSongsAdapter;
    RecyclerView recyclerCurrentSong;
    SnapHelperOneByOne snapHelperOneByOne;
    Boolean stateAdapter;
    public SongsAdapter(Context context, FragmentActivity mainActivity, ArrayList<Song> songs, Boolean stateAdapter) {
        this.context = context;
        this.mainActivity = mainActivity;
        this.songs = songs;
        this.stateAdapter = stateAdapter;
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

       /* if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.Q){
            android.media.MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            mmr.setDataSource(song.getPath());
            byte[] data = mmr.getEmbeddedPicture();
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);

            if(bitmap == null){
                holder.songAlbum.setImageResource(R.drawable.alternativ_song_album);
            } else {
                holder.songAlbum.setImageBitmap(bitmap);
            }
        } else{
            holder.songAlbum.setImageResource(R.drawable.alternativ_song_album);
        }*/

        //Создание обложки песни
        android.media.MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(song.getPath());
        byte[] data = mmr.getEmbeddedPicture();
        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);

        //Если обложки нет, то подставляем альтернативную абложку
        if(bitmap == null){
            holder.songAlbum.setImageResource(R.drawable.alternativ_song_album);
        } else {
            holder.songAlbum.setImageBitmap(bitmap);
        }

        holder.songTitle.setText(song.getTitle());
        holder.songArtist.setText(song.getArtist());

        //При нажатии на песню в списке передаём позицию в список текущих песен
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentSongsAdapter.setStateSong(true);
                recyclerCurrentSong.scrollToPosition(position);
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

            recyclerCurrentSong.animate().translationXBy(1000f).setDuration(500).start();
        }
    }

    @Override
    public void onViewRecycled(@NonNull SongsAdapter.ViewHolder holder) {
        super.onViewRecycled(holder);
        holder.setIsRecyclable(true);
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull SongsAdapter.ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.setIsRecyclable(false);
    }

    public  class ViewHolder extends RecyclerView.ViewHolder{

        FrameLayout frameSong;
        ImageView songAlbum;
        TextView songTitle, songArtist;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            frameSong = itemView.findViewById(R.id.frameSong);
            songAlbum = itemView.findViewById(R.id.songAlbum);
            songTitle = itemView.findViewById(R.id.songTitle);
            songArtist = itemView.findViewById(R.id.songArtist);
        }
    }
}
