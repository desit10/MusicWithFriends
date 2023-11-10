package com.example.musicwithfriends;

import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.MediaItem;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

import android.os.Bundle;

import com.example.musicwithfriends.Models.Song;

import java.util.ArrayList;

@UnstableApi public class SongPlayerActivity extends AppCompatActivity {

    PlayerView songPlayer;
    ExoPlayer player;
    ArrayList<Song> songs;
    int index;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_player);

        songPlayer = findViewById(R.id.songPlayer);

        index = getIntent().getIntExtra("INDEX", 0);
        songs = (ArrayList<Song>) getIntent().getSerializableExtra("SONGS");

        ArrayList<MediaItem> mediaItems = new ArrayList<>();
        for(Song song : songs){
            mediaItems.add(MediaItem.fromUri(song.getPath()));
        }

        player = new ExoPlayer.Builder(this)
                .setSeekForwardIncrementMs(5000)
                .build();

        player.setMediaItems(mediaItems);

        songPlayer.setPlayer(player);
        player.seekToDefaultPosition(index);
        songPlayer.getPlayer().prepare();
        songPlayer.getPlayer().play();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        songPlayer.getPlayer().clearMediaItems();
    }
}