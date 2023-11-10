package com.example.musicwithfriends.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.media3.common.util.UnstableApi;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musicwithfriends.Helpers.SnapHelperOneByOne;
import com.example.musicwithfriends.Models.Song;
import com.example.musicwithfriends.R;

import java.util.ArrayList;

@UnstableApi
public class PlaylistsAdapter extends RecyclerView.Adapter<PlaylistsAdapter.ViewHolder>{

    Context context;
    FragmentActivity mainActivity;
    ArrayList<Song> songs;
    CurrentSongsAdapter currentSongsAdapter;
    RecyclerView recyclerCurrentSong;
    SnapHelperOneByOne snapHelperOneByOne;
    Boolean stateAdapter;
    public PlaylistsAdapter(Context context, FragmentActivity mainActivity, ArrayList<Song> songs, Boolean stateAdapter) {
        this.context = context;
        this.mainActivity = mainActivity;
        this.songs = songs;
        this.stateAdapter = stateAdapter;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_song, parent, false);

        return new PlaylistsAdapter.ViewHolder(view);
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

        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(song.getPath());
        byte[] data = mmr.getEmbeddedPicture();
        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);

        if(bitmap == null){
            holder.songAlbum.setImageResource(R.drawable.alternativ_song_album);
        } else {
            holder.songAlbum.setImageBitmap(bitmap);
        }

        holder.songTitle.setText(song.getTitle());
        holder.songArtist.setText(song.getArtist());
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
    public void onViewRecycled(@NonNull PlaylistsAdapter.ViewHolder holder) {
        super.onViewRecycled(holder);
        holder.setIsRecyclable(true);
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull PlaylistsAdapter.ViewHolder holder) {
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
