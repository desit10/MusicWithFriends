package com.example.musicwithfriends.Adapters;


import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musicwithfriends.Helpers.FirebaseHelper;
import com.example.musicwithfriends.Helpers.SnapHelperOneByOne;
import com.example.musicwithfriends.Models.Room;
import com.example.musicwithfriends.Models.Song;
import com.example.musicwithfriends.R;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.Locale;

@UnstableApi
public class CurrentSongsWithFriendsAdapter extends RecyclerView.Adapter<CurrentSongsWithFriendsAdapter.ViewHolder>{

    Context context;
    Room room;
    ExoPlayer player;
    SeekBar seekBar;
    TextView currentTime;
    TextView fullTime;
    ViewHolder viewHolder;
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
                    setViewHolder(holder);

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

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setViewHolder(holder);
                showFullScreenSong(room.getRoomPlaylist(), position);
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
            holder.songManagement.setImageResource(R.drawable.btn_song_play);
        } else {
            holder.songManagement.setImageResource(R.drawable.btn_song_pause);
            holder.playerView.getPlayer().seekTo(0);
            holder.playerView.getPlayer().play();
        }
        setViewHolder(holder);
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
            holder.songManagement.setImageResource(R.drawable.btn_song_play);
        } else {
            holder.playerView.getPlayer().prepare();
            holder.playerView.getPlayer().play();
            holder.songManagement.setImageResource(R.drawable.btn_song_pause);
        }
        updateStateSong.setValue(getStateSong());
        updateProgressSong.setValue(holder.playerView.getPlayer().getCurrentPosition());
    }

    public ViewHolder getViewHolder() {
        return viewHolder;
    }

    public void setViewHolder(ViewHolder viewHolder) {
        this.viewHolder = viewHolder;
    }

    public SeekBar getSeekBar() {
        return seekBar;
    }

    public void setSeekBar(SeekBar seekBar) {
        this.seekBar = seekBar;
    }
    Handler handler = new Handler();
    private Runnable moveSeekBarThread = new Runnable() {
        public void run() {
            int mediaPos_new = (int) getViewHolder().playerView.getPlayer().getCurrentPosition();
            int mediaMax_new = (int) getViewHolder().playerView.getPlayer().getDuration();

            currentTime.setText(format(mediaPos_new));
            fullTime.setText(format(mediaMax_new));

            getSeekBar().setMax(mediaMax_new);
            getSeekBar().setProgress(mediaPos_new, true);

            handler.postDelayed(this, 1000);
        }
    };

    public String format(long millis) {
        long allSeconds = millis / 1000;
        int allMinutes;
        byte seconds, minutes, hours;
        if (allSeconds >= 60) {
            allMinutes = (int) (allSeconds / 60);
            seconds = (byte) (allSeconds % 60);
            if (allMinutes >= 60) {
                hours = (byte) (allMinutes / 60);
                minutes = (byte) (allMinutes % 60);
                return String.format(Locale.getDefault(), "%d:%d:" + formatSeconds(seconds), hours, minutes, seconds);
            } else
                return String.format(Locale.getDefault(), "%d:" + formatSeconds(seconds), allMinutes, seconds);
        } else
            return String.format(Locale.getDefault(), "0:" + formatSeconds((byte) allSeconds), allSeconds);
    }

    public String formatSeconds(byte seconds) {
        String secondsFormatted;
        if (seconds < 10) secondsFormatted = "0%d";
        else secondsFormatted = "%d";
        return secondsFormatted;
    }

    @SuppressLint("ResourceAsColor")
    private void showFullScreenSong(ArrayList<Song> songs, int position) {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.item_song_full_screen);

        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
        dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        dialog.getWindow().setStatusBarColor(R.color.transparent);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.AnimationContextMenu;
        dialog.getWindow().setGravity(Gravity.CENTER);

        SeekBar seekBar =  dialog.findViewById(R.id.seekBar);
        setSeekBar(seekBar);

        currentTime = dialog.findViewById(R.id.currentTime);
        fullTime = dialog.findViewById(R.id.fullTime);

        ImageButton songLast = dialog.findViewById(R.id.songLast);
        ImageButton songManagement = dialog.findViewById(R.id.songManagement);
        ImageButton songNext = dialog.findViewById(R.id.songNext);

        recyclerCurrentSongFullScreen = dialog.findViewById(R.id.recyclerCurrentSongFullScreen);

        SnapHelperOneByOne snapHelperOneByOne = new SnapHelperOneByOne();

        SongsFullScreenAdapter songsFullScreenAdapter = new SongsFullScreenAdapter(context, songs, recyclerCurrentSongs, snapHelperOneByOne);

        recyclerCurrentSongFullScreen.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));

        recyclerCurrentSongFullScreen.setOnFlingListener(null);
        snapHelperOneByOne.attachToRecyclerView(recyclerCurrentSongFullScreen);

        recyclerCurrentSongFullScreen.setAdapter(songsFullScreenAdapter);

        recyclerCurrentSongFullScreen.scrollToPosition(position);

        int mediaPos = (int) getViewHolder().playerView.getPlayer().getCurrentPosition();
        int mediaMax = (int) getViewHolder().playerView.getPlayer().getDuration();

        fullTime.setText(format(mediaMax));

        seekBar.setMax(mediaMax);
        seekBar.setProgress(mediaPos, true);

        handler.removeCallbacks(moveSeekBarThread);
        handler.postDelayed(moveSeekBarThread, 100);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean input) {
                if(input){
                    getViewHolder().playerView.getPlayer().seekTo(progress);
                    seekBar.setProgress(progress);
                    DatabaseReference updateProgressSong = firebaseHelper.Request("rooms/" + roomId + "/progressSong");
                    updateProgressSong.setValue(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        if(!getStateSong()){
            songManagement.setImageResource(R.drawable.btn_song_full_screen_play);
        } else {
            songManagement.setImageResource(R.drawable.btn_song_full_screen_pause);
        }

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = getViewHolder().getAbsoluteAdapterPosition();

                if(v.getId() == songLast.getId()){
                    if(position != 0){
                        position--;
                        recyclerCurrentSongFullScreen.scrollToPosition(position);
                    } else {
                        position = songs.size() - 1;
                        recyclerCurrentSongFullScreen.scrollToPosition(position);
                    }
                }
                if(v.getId() == songManagement.getId()){
                    stateSong(getViewHolder());
                    if(!getStateSong()){
                        songManagement.setImageResource(R.drawable.btn_song_full_screen_play);
                    } else {
                        songManagement.setImageResource(R.drawable.btn_song_full_screen_pause);
                    }
                }
                if(v.getId() == songNext.getId()){
                    if(position != songs.size() - 1){
                        position++;
                        recyclerCurrentSongFullScreen.scrollToPosition(position);
                    } else {
                        recyclerCurrentSongFullScreen.scrollToPosition(0);
                    }
                }
            }
        };

        songLast.setOnClickListener(onClickListener);
        songManagement.setOnClickListener(onClickListener);
        songNext.setOnClickListener(onClickListener);

        dialog.show();
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
