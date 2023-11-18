package com.example.musicwithfriends.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musicwithfriends.Models.Song;
import com.example.musicwithfriends.R;

import java.util.ArrayList;

@UnstableApi
public class CurrentSongsAdapter extends RecyclerView.Adapter<CurrentSongsAdapter.ViewHolder>{

    Context context;
    ArrayList<Song> songs;
    ExoPlayer player;
    RecyclerView recyclerCurrentSongs;
    Boolean stateSong = false;

    public CurrentSongsAdapter(Context context, RecyclerView recyclerCurrentSongs, ArrayList<Song> songs) {
        this.context = context;
        this.recyclerCurrentSongs = recyclerCurrentSongs;
        this.songs = songs;


        /*ArrayList<MediaItem> mediaItems = new ArrayList<>();
        for(Song song : songs){
            mediaItems.add(MediaItem.fromUri(song.getPath()));
        }*/

        /*player = new ExoPlayer.Builder(context)
                .setSeekForwardIncrementMs(5000)
                .build();

        player.setMediaItems(mediaItems);*/
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_current_song, parent, false);

        return new CurrentSongsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {

        Song song = songs.get(position);

        //В каждый элемент списка добавляем медиа
        player = new ExoPlayer.Builder(context)
                .setSeekForwardIncrementMs(5000)
                .build();
        player.setMediaItem(MediaItem.fromUri(song.getPath()));
        holder.playerView.setPlayer(player);

        //При окончании воспроизведения медиа передаём списку следующую позицию
        holder.playerView.getPlayer().addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                Player.Listener.super.onPlaybackStateChanged(playbackState);
                if(playbackState == Player.STATE_ENDED){
                    recyclerCurrentSongs.smoothScrollToPosition(position + 1);

                    //Если песня из списка последняя, то начинаем воспроизведение сначала списка
                    if(getItemCount() - 1 == position){
                        recyclerCurrentSongs.smoothScrollToPosition(0);
                    }
                }
            }
        });

        //Бегущая строка на названии и артисте
        holder.songTitle.setText(song.getTitle());
        holder.songTitle.setSelected(true);
        holder.songTitle.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        holder.songTitle.setMarqueeRepeatLimit(-1);
        holder.songTitle.setFocusable(true);
        holder.songTitle.setFocusableInTouchMode(true);
        holder.songTitle.requestFocus();

        holder.songArtist.setText(song.getArtist());
        holder.songArtist.setSelected(true);
        holder.songArtist.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        holder.songArtist.setMarqueeRepeatLimit(-1);
        holder.songArtist.setFocusable(true);
        holder.songArtist.setFocusableInTouchMode(true);
        holder.songArtist.requestFocus();


        holder.songHeart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        //Play\Pause песни
        holder.songManagement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*stateSong = getStateSong();
                stateSong = !stateSong;*/
                setStateSong(!getStateSong());
                if(!getStateSong()){
                    holder.playerView.getPlayer().pause();
                    holder.songManagement.setImageResource(R.drawable.song_play);
                } else {
                    holder.playerView.getPlayer().prepare();
                    holder.playerView.getPlayer().play();
                    holder.songManagement.setImageResource(R.drawable.song_pause);
                }
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               /* Intent intent = new Intent(context, SongPlay111erActivity.class);
                intent.putExtra("INDEX", position);
                intent.putExtra("SONGS", songs);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);*/
            }
        });

    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        super.onViewRecycled(holder);
        holder.setIsRecyclable(true);
    }

    //При появлении элемента на экране запускаем песню
    @Override
    public void onViewAttachedToWindow(@NonNull ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        if(!getStateSong()){
            holder.songManagement.setImageResource(R.drawable.song_play);
        } else {
            holder.songManagement.setImageResource(R.drawable.song_pause);
            holder.playerView.getPlayer().seekTo(0);
            holder.playerView.getPlayer().prepare();
            holder.playerView.getPlayer().play();
        }

    }

    //При исчезнавении элемента с экрана заканчиваем песню
    @Override
    public void onViewDetachedFromWindow(@NonNull ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.setIsRecyclable(false);
        holder.playerView.getPlayer().stop();
    }

    public void setStateSong(Boolean stateSong) {
        this.stateSong = stateSong;
    }

    public boolean getStateSong() {
        return stateSong;
    }

    public  class ViewHolder extends RecyclerView.ViewHolder{

        PlayerView playerView;
        TextView songTitle, songArtist;
        ImageButton songManagement, songHeart;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            songHeart = itemView.findViewById(R.id.songHeart);
            songManagement = itemView.findViewById(R.id.songManagement);

            playerView = itemView.findViewById(R.id.playerSong);

            songTitle = itemView.findViewById(R.id.songTitle);
            songArtist = itemView.findViewById(R.id.songArtist);
        }
    }
}
