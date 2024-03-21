package com.example.musicwithfriends.Adapters;


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musicwithfriends.Helpers.FirebaseHelper;
import com.example.musicwithfriends.Models.Room;
import com.example.musicwithfriends.Models.Song;
import com.example.musicwithfriends.R;
import com.google.firebase.database.DatabaseReference;

@UnstableApi
public class CurrentSongsWithFriendsAdapter extends RecyclerView.Adapter<CurrentSongsWithFriendsAdapter.ViewHolder>{

    Context context;
    Room room;
    ExoPlayer player;
    SeekBar seekBar;
    TextView currentTime;
    TextView fullTime;
    RecyclerView recyclerCurrentSongs, recyclerCurrentSongFullScreen;
    Boolean stateSong = false;
    String roomId;
    FirebaseHelper firebaseHelper = new FirebaseHelper();

    public CurrentSongsWithFriendsAdapter(Context context, RecyclerView recyclerCurrentSongs, Room room, String roomId) {
        this.context = context;
        this.recyclerCurrentSongs = recyclerCurrentSongs;
        this.room = room;
        this.roomId = roomId;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;

        view = LayoutInflater.from(context).inflate(R.layout.item_current_song, parent, false);

        return new CurrentSongsWithFriendsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {

        Song song = room.getRoomPlaylist().get(position);
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
                    DatabaseReference updateCurrentSong = firebaseHelper.Request("rooms/" + roomId + "/currentSong");
                    DatabaseReference updateProgressSong = firebaseHelper.Request("rooms/" + roomId + "/progressSong");

                    recyclerCurrentSongs.scrollToPosition(position);

                    //Если песня из списка последняя, то начинаем воспроизведение сначала списка
                    if(getItemCount() - 1 == position){
                        recyclerCurrentSongs.scrollToPosition(position);
                    }
                    updateCurrentSong.setValue(song.getSongName());
                    updateProgressSong.setValue(holder.playerView.getPlayer().getCurrentPosition());
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

        //Play\Pause песни
        holder.songManagement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stateSong(holder);

                DatabaseReference updateProgressSong = firebaseHelper.Request("rooms/" + roomId + "/progressSong");
                updateProgressSong.setValue(holder.playerView.getPlayer().getCurrentPosition());
            }
        });


    }

    @Override
    public int getItemCount() {
        return room.getRoomPlaylist().size();
    }

    //При появлении элемента на экране запускаем песню
    @Override
    public void onViewAttachedToWindow(@NonNull ViewHolder holder) {
        super.onViewAttachedToWindow(holder);

        holder.playerView.getPlayer().prepare();

        if(!getStateSong()){
            holder.songManagement.setImageResource(R.drawable.song_play);
        } else {
            holder.songManagement.setImageResource(R.drawable.song_pause);
            holder.playerView.getPlayer().seekTo(0);
            holder.playerView.getPlayer().play();
        }

        DatabaseReference updateProgressSong = firebaseHelper.Request("rooms/" + roomId + "/progressSong");
        updateProgressSong.setValue(holder.playerView.getPlayer().getCurrentPosition());
    }

    //При исчезнавении элемента с экрана заканчиваем песню
    @Override
    public void onViewDetachedFromWindow(@NonNull ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);

        holder.playerView.getPlayer().stop();
        DatabaseReference updateProgressSong = firebaseHelper.Request("rooms/" + roomId + "/progressSong");
        updateProgressSong.setValue(holder.playerView.getPlayer().getCurrentPosition());
    }

    public void setStateSong(Boolean stateSong) {
        this.stateSong = stateSong;
    }

    public boolean getStateSong() {
        return stateSong;
    }

    public void stateSong(ViewHolder holder){
        DatabaseReference updateProgressSong = firebaseHelper.Request("rooms/" + roomId + "/progressSong");
        DatabaseReference updateStateSong = firebaseHelper.Request("rooms/" + roomId + "/stateSong");
        setStateSong(!getStateSong());
        if(!getStateSong()){
            holder.playerView.getPlayer().pause();
            holder.songManagement.setImageResource(R.drawable.song_play);
        } else {
            holder.playerView.getPlayer().prepare();
            holder.playerView.getPlayer().play();
            holder.songManagement.setImageResource(R.drawable.song_pause);
        }
        updateStateSong.setValue(getStateSong());
        updateProgressSong.setValue(holder.playerView.getPlayer().getCurrentPosition());
    }

    public  class ViewHolder extends RecyclerView.ViewHolder{

        PlayerView playerView;
        TextView songTitle, songArtist;
        ImageButton songManagement;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            songManagement = itemView.findViewById(R.id.songManagement);

            playerView = itemView.findViewById(R.id.playerSong);

            songTitle = itemView.findViewById(R.id.songTitle);
            songArtist = itemView.findViewById(R.id.songArtist);
        }
    }
}
