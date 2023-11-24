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
import android.widget.ImageView;
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

import com.example.musicwithfriends.Helpers.SnapHelperOneByOne;
import com.example.musicwithfriends.Models.Song;
import com.example.musicwithfriends.R;

import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

@UnstableApi
public class CurrentSongsAdapter extends RecyclerView.Adapter<CurrentSongsAdapter.ViewHolder>{

    Context context;
    ArrayList<Song> songs;
    ExoPlayer player;
    SeekBar seekBar;
    TextView currentTime;
    TextView fullTime;
    RecyclerView recyclerCurrentSongs, recyclerCurrentSongFullScreen;
    ViewHolder viewHolder = null;
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
        View view;

        view = LayoutInflater.from(context).inflate(R.layout.item_current_song, parent, false);

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
                    setViewHolder(holder);

                    recyclerCurrentSongs.scrollToPosition(position + 1);

                    if(recyclerCurrentSongFullScreen != null){
                        recyclerCurrentSongFullScreen.scrollToPosition(position + 1);
                    }

                    //Если песня из списка последняя, то начинаем воспроизведение сначала списка
                    if(getItemCount() - 1 == position){
                        recyclerCurrentSongs.scrollToPosition(0);

                        if(recyclerCurrentSongFullScreen != null){
                            recyclerCurrentSongFullScreen.scrollToPosition(0);
                        }
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
                PlayPause(holder);
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setViewHolder(holder);
                showFullScreenSong(songs, position);
            }
        });

    }

    @Override
    public int getItemCount() {
        return songs.size();
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
        setViewHolder(holder);
    }
    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        super.onViewRecycled(holder);
        //holder.setIsRecyclable(true);
    }

    //При исчезнавении элемента с экрана заканчиваем песню
    @Override
    public void onViewDetachedFromWindow(@NonNull ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);

        //holder.setIsRecyclable(false);
        holder.playerView.getPlayer().stop();
    }

    public void setStateSong(Boolean stateSong) {
        this.stateSong = stateSong;
    }

    public boolean getStateSong() {
        return stateSong;
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

    private void PlayPause(ViewHolder holder){
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
        dialog.setContentView(R.layout.song_full_screen);

        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
        dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        dialog.getWindow().setStatusBarColor(R.color.transparent);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
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
            songManagement.setImageResource(R.drawable.song_full_screen_play);
        } else {
            songManagement.setImageResource(R.drawable.song_full_screen_pause);
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
                    PlayPause(getViewHolder());
                    if(!getStateSong()){
                        songManagement.setImageResource(R.drawable.song_full_screen_play);
                    } else {
                        songManagement.setImageResource(R.drawable.song_full_screen_pause);
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
