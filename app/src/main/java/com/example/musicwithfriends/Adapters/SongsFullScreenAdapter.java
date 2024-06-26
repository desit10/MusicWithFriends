package com.example.musicwithfriends.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.media3.common.util.UnstableApi;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musicwithfriends.Helpers.SnapHelperOneByOne;
import com.example.musicwithfriends.Models.Song;
import com.example.musicwithfriends.R;

import java.util.ArrayList;

@UnstableApi
public class SongsFullScreenAdapter extends RecyclerView.Adapter<SongsFullScreenAdapter.ViewHolder>{

    Context context;
    ArrayList<Song> songs;
    RecyclerView recyclerCurrentSongs;
    SnapHelperOneByOne snapHelperOneByOne;
    RecyclerView thisRecyclerView;
    int positionSong;

    public SongsFullScreenAdapter(Context context, ArrayList<Song> songs, RecyclerView recyclerCurrentSongs, SnapHelperOneByOne snapHelperOneByOne) {
        this.context = context;
        this.songs = songs;
        this.recyclerCurrentSongs = recyclerCurrentSongs;
        this.snapHelperOneByOne = snapHelperOneByOne;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_current_song_full_screen, parent, false);

        return new SongsFullScreenAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {

        Song song = songs.get(position);

        //Создание обложки песни
        creatingSongCover(holder, song);

        holder.songTitle.setText(song.getTitle());
        holder.songArtist.setText(song.getArtist());

    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        thisRecyclerView = recyclerView;
    }

    @Override
    public void onViewAttachedToWindow(@NonNull ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        scrollItem();
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        scrollItem();
    }

    void creatingSongCover(ViewHolder holder, Song song){
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
    }

    private void scrollItem() {
        positionSong = snapHelperOneByOne.findTargetSnapPosition(thisRecyclerView.getLayoutManager(), 30, 0);
        recyclerCurrentSongs.scrollToPosition(positionSong);
    }

    public  class ViewHolder extends RecyclerView.ViewHolder{

        ImageView songAlbum;
        TextView songTitle, songArtist;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            songAlbum = itemView.findViewById(R.id.songAlbum);

            songTitle = itemView.findViewById(R.id.songTitle);
            songArtist = itemView.findViewById(R.id.songArtist);
        }
    }
}
