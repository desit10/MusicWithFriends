package com.example.musicwithfriends.Adapters;


import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaMetadataRetriever;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import com.example.musicwithfriends.Helpers.MediaHelpers;
import com.example.musicwithfriends.Helpers.SnapHelperOneByOne;
import com.example.musicwithfriends.Models.Song;
import com.example.musicwithfriends.R;

import java.util.ArrayList;
import java.util.List;

@UnstableApi
public class CurrentSongsAdapter extends RecyclerView.Adapter<CurrentSongsAdapter.ViewHolder> {

    MediaHelpers mediaHelpers;
    Context context;
    ArrayList<Song> songs;
    ExoPlayer player;
    SeekBar seekBar;
    TextView currentTime;
    TextView fullTime;
    RecyclerView recyclerCurrentSongs, recyclerCurrentSongFullScreen;
    SongsAdapter songsAdapter;
    ViewHolder viewHolder = null;
    Boolean stateSong = false;
    int positionSong;

    public CurrentSongsAdapter(Context context, ArrayList<Song> songs, SongsAdapter songsAdapter) {
        this.context = context;
        this.songs = songs;
        this.songsAdapter = songsAdapter;
        this.mediaHelpers = new MediaHelpers(context, songs);
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
        addingSongToPlayer(holder, song);

        //При окончании воспроизведения медиа передаём списку следующую позицию
        holder.playerView.getPlayer().addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                Player.Listener.super.onPlaybackStateChanged(playbackState);

                if (playbackState == Player.STATE_ENDED) {
                    checkingStatusSong(holder, position);
                }
            }
        });

        //Бегущая строка на названии и артисте
        runningLineTitleAndArtist(holder, song);

        //Play\Pause песни
        holder.songManagement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stateSong(holder);
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setViewHolder(holder);
                positionSong = position;
                showFullScreenSong();
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

        this.recyclerCurrentSongs = recyclerView;

        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            @SuppressLint("MissingPermission")
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();

                Log.i("BroadcastReceiver", action);

                switch (action) {
                    case "ACTION_LAST_SONG":
                        skipToTheLastSong();
                        break;
                    case "ACTION_STATE_SONG":
                        stateSong(getViewHolder());
                        break;
                    case "ACTION_NEXT_SONG":
                        skipToTheNextSong();
                        break;
                    case "ACTION_CREATE_TEMP_PLAYER":
                        mediaHelpers.createTempPlayer((ExoPlayer) getViewHolder().playerView.getPlayer());
                        break;
                    case "ACTION_DESTROY_TEMP_PLAYER":
                        //getViewHolder().playerView.setPlayer(mediaHelpers.getTempPlayer());
                        //Log.i("TEMP_PLAYER", String.valueOf(mediaHelpers.getTempPlayer().getCurrentPosition()));
                        //Log.i("PLAYER", String.valueOf(getViewHolder().playerView.getPlayer().getCurrentPosition()));

                        //getViewHolder().playerView.getPlayer().seekTo(mediaHelpers.getTempPlayer().getCurrentPosition());
                        //mediaHelpers.destroyTempPlayer();
                        break;
                    case "ACTION_START_MAIN_ACTIVITY":
                            ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
                            List<ActivityManager.AppTask> tasks = activityManager.getAppTasks();

                            if (tasks != null && !tasks.isEmpty()) {
                                ActivityManager.AppTask task = tasks.get(0);
                                if (task.getTaskInfo().baseActivity.getPackageName().equals(context.getPackageName())) {
                                    activityManager.moveTaskToFront(task.getTaskInfo().id, ActivityManager.MOVE_TASK_WITH_HOME);
                                }
                            }
                        break;
                    case "ACTION_DESTROY_NOTIFICATION":
                        //mediaHelpers.destroyMediaNotification();
                        break;
                }
            }

            public void skipToTheLastSong() {
                if (positionSong != 0) {
                    positionSong--;
                } else {
                    positionSong = songs.size() - 1;
                }

                mediaHelpers.showMediaNotification(positionSong);
                recyclerCurrentSongs.scrollToPosition(positionSong);

                if(recyclerCurrentSongFullScreen != null){
                    recyclerCurrentSongFullScreen.scrollToPosition(positionSong);
                }
            }

            public void skipToTheNextSong() {
                if (positionSong != songs.size() - 1) {
                    positionSong++;
                } else {
                    positionSong = 0;
                }

                mediaHelpers.showMediaNotification(positionSong);
                recyclerCurrentSongs.scrollToPosition(positionSong);

                if(recyclerCurrentSongFullScreen != null){
                    recyclerCurrentSongFullScreen.scrollToPosition(positionSong);
                }
            }

        };
        context.registerReceiver(broadcastReceiver, mediaHelpers.getIntentFilter());
    }

    //При появлении элемента на экране запускаем песню
    @Override
    public void onViewAttachedToWindow(@NonNull ViewHolder holder) {
        super.onViewAttachedToWindow(holder);

        holder.playerView.getPlayer().prepare();

        if (!getStateSong()) {
            holder.songManagement.setImageResource(R.drawable.btn_song_play);
            mediaHelpers.updateMediaNotification(R.drawable.btn_song_play);
        } else {
            holder.songManagement.setImageResource(R.drawable.btn_song_pause);
            mediaHelpers.updateMediaNotification(R.drawable.btn_song_pause);
            holder.playerView.getPlayer().seekTo(0);
            holder.playerView.getPlayer().play();
        }

        positionSong = holder.getAbsoluteAdapterPosition();
        setViewHolder(holder);
        mediaHelpers.showMediaNotification(positionSong);

        songsAdapter.selectingSong(positionSong);

        if(dialogFullScreenSong != null){
            setDominantColor(dialogFullScreenSong, positionSong);
        }
    }

    //При исчезнавении элемента с экрана заканчиваем песню
    @Override
    public void onViewDetachedFromWindow(@NonNull ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);

        holder.playerView.getPlayer().seekTo(0);
        holder.playerView.getPlayer().stop();
    }

    void addingSongToPlayer(ViewHolder holder, Song song) {
        player = new ExoPlayer.Builder(context)
                .setSeekForwardIncrementMs(5000)
                .build();
        player.setMediaItem(MediaItem.fromUri(song.getPath()));
        holder.playerView.setPlayer(player);
    }

    void runningLineTitleAndArtist(ViewHolder holder, Song song) {
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
    }

    void checkingStatusSong(ViewHolder holder, int position) {
        setViewHolder(holder);

        recyclerCurrentSongs.scrollToPosition(position + 1);

        if (recyclerCurrentSongFullScreen != null) {
            recyclerCurrentSongFullScreen.scrollToPosition(position + 1);
        }

        //Если песня из списка последняя, то начинаем воспроизведение сначала списка
        if (getItemCount() - 1 == position) {
            recyclerCurrentSongs.scrollToPosition(0);

            if (recyclerCurrentSongFullScreen != null) {
                recyclerCurrentSongFullScreen.scrollToPosition(0);
            }
        }
    }

    private void stateSong(ViewHolder holder) {
        setStateSong(!getStateSong());
        if (!getStateSong()) {
            holder.playerView.getPlayer().pause();
            holder.songManagement.setImageResource(R.drawable.btn_song_play);
            mediaHelpers.updateMediaNotification(R.drawable.btn_song_play);

            if(songFullScreenManagement != null){
                songFullScreenManagement.setImageResource(R.drawable.btn_song_full_screen_play);
            }
        } else {
            holder.playerView.getPlayer().prepare();
            holder.playerView.getPlayer().play();
            holder.songManagement.setImageResource(R.drawable.btn_song_pause);
            mediaHelpers.updateMediaNotification(R.drawable.btn_song_pause);

            if(songFullScreenManagement != null){
                songFullScreenManagement.setImageResource(R.drawable.btn_song_full_screen_pause);
            }
        }
        mediaHelpers.showMediaNotification(positionSong);
    }

    public int getPositionSong(){
        return positionSong;
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

    Handler handler = new Handler();
    private Runnable moveSeekBarThread = new Runnable() {
        public void run() {
            int mediaPos_new = (int) getViewHolder().playerView.getPlayer().getCurrentPosition();
            int mediaMax_new = (int) getViewHolder().playerView.getPlayer().getDuration();

            currentTime.setText(mediaHelpers.mediaFormatDuration(mediaPos_new));
            fullTime.setText(mediaHelpers.mediaFormatDuration(mediaMax_new));

            getSeekBar().setMax(mediaMax_new);
            getSeekBar().setProgress(mediaPos_new, true);

            handler.postDelayed(this, 1000);
        }
    };

    public void setDominantColor(Dialog dialog, int positionSong) {
        android.media.MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(songs.get(positionSong).getPath());
        byte[] data = mmr.getEmbeddedPicture();

        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
        Bitmap newBitmap = Bitmap.createScaledBitmap(bitmap, 1, 1, true);

        int color = newBitmap.getPixel(0, 0);

        dialog.getWindow().setStatusBarColor(color);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(color));
        dialog.getWindow().setNavigationBarColor(color);
    }

    Dialog dialogFullScreenSong;
    ImageButton songFullScreenManagement;
    @SuppressLint("ResourceAsColor")
    private void showFullScreenSong() {
        dialogFullScreenSong = new Dialog(context);
        //dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogFullScreenSong.setContentView(R.layout.item_song_full_screen);

        dialogFullScreenSong.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        dialogFullScreenSong.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        dialogFullScreenSong.getWindow().getDecorView()
                .setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        setDominantColor(dialogFullScreenSong, positionSong);
        dialogFullScreenSong.getWindow().getAttributes().windowAnimations = R.style.AnimationContextMenu;
        dialogFullScreenSong.getWindow().setGravity(Gravity.CENTER);

        SeekBar seekBar = dialogFullScreenSong.findViewById(R.id.seekBar);
        setSeekBar(seekBar);

        currentTime = dialogFullScreenSong.findViewById(R.id.currentTime);
        fullTime = dialogFullScreenSong.findViewById(R.id.fullTime);

        ImageButton songLast = dialogFullScreenSong.findViewById(R.id.songLast);
        songFullScreenManagement = dialogFullScreenSong.findViewById(R.id.songManagement);
        ImageButton songNext = dialogFullScreenSong.findViewById(R.id.songNext);

        recyclerCurrentSongFullScreen = dialogFullScreenSong.findViewById(R.id.recyclerCurrentSongFullScreen);

        SnapHelperOneByOne snapHelperOneByOne = new SnapHelperOneByOne();

        SongsFullScreenAdapter songsFullScreenAdapter = new SongsFullScreenAdapter(context, songs, recyclerCurrentSongs, snapHelperOneByOne);

        recyclerCurrentSongFullScreen.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));

        recyclerCurrentSongFullScreen.setOnFlingListener(null);
        snapHelperOneByOne.attachToRecyclerView(recyclerCurrentSongFullScreen);

        recyclerCurrentSongFullScreen.setAdapter(songsFullScreenAdapter);

        recyclerCurrentSongFullScreen.scrollToPosition(positionSong);

        int mediaPos = (int) getViewHolder().playerView.getPlayer().getCurrentPosition();
        int mediaMax = (int) getViewHolder().playerView.getPlayer().getDuration();

        fullTime.setText(mediaHelpers.mediaFormatDuration(mediaMax));

        seekBar.setMax(mediaMax);
        seekBar.setProgress(mediaPos, true);

        handler.removeCallbacks(moveSeekBarThread);
        handler.postDelayed(moveSeekBarThread, 100);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean input) {
                if (input) {
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

        if (!getStateSong()) {
            songFullScreenManagement.setImageResource(R.drawable.btn_song_full_screen_play);
        } else {
            songFullScreenManagement.setImageResource(R.drawable.btn_song_full_screen_pause);
        }

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == songLast.getId()) {
                    skipToTheLastSong(positionSong);
                }
                if (v.getId() == songFullScreenManagement.getId()) {
                    stateSong(getViewHolder());
                }
                if (v.getId() == songNext.getId()) {
                    skipToTheNextSong(positionSong);
                }
            }

            public void skipToTheLastSong(int positionSong) {
                if (positionSong != 0) {
                    positionSong--;
                } else {
                    positionSong = songs.size() - 1;
                }
                recyclerCurrentSongFullScreen.scrollToPosition(positionSong);
                setDominantColor(dialogFullScreenSong, positionSong);
            }

            public void skipToTheNextSong(int positionSong) {
                if (positionSong != songs.size() - 1) {
                    positionSong++;
                } else {
                    positionSong = 0;
                }
                recyclerCurrentSongFullScreen.scrollToPosition(positionSong);
                setDominantColor(dialogFullScreenSong, positionSong);
            }
        };

        songLast.setOnClickListener(onClickListener);
        songFullScreenManagement.setOnClickListener(onClickListener);
        songNext.setOnClickListener(onClickListener);

        dialogFullScreenSong.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                songsAdapter.notifyItemChanged(songsAdapter.oldSelectedHolder.getAbsoluteAdapterPosition());
                songsAdapter.notifyItemChanged(positionSong);
                songsAdapter.recyclerSongs.smoothScrollToPosition(positionSong);
            }
        });

        dialogFullScreenSong.show();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

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
