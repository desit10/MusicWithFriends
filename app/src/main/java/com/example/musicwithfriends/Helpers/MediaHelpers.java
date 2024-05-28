package com.example.musicwithfriends.Helpers;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadata;
import android.media.MediaMetadataRetriever;
import android.media.session.MediaSession;
import android.os.Build;
import android.provider.Settings;

import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.ExoPlayer;

import com.example.musicwithfriends.Models.Song;
import com.example.musicwithfriends.R;
import com.example.musicwithfriends.Services.MediaNotificationListenerService;

import java.util.ArrayList;
import java.util.Locale;

public class MediaHelpers {

    private int NOTIFICATION_ID = 1;
    private static final String NOTIFICATION_CHANNEL_ID = "Media channel";

    Context context;
    ArrayList<Song> songs;

    ExoPlayer tempPlayer;
    MediaSession mediaSession;
    Notification.MediaStyle mediaStyle;
    PendingIntent pendingIntentStartMainActivity;
    PendingIntent pendingIntentLastSong;
    PendingIntent pendingIntentStateSong;
    PendingIntent pendingIntentNextSong;
    IntentFilter intentFilter;
    NotificationChannel notificationChannel;
    NotificationManager mediaNotificationManager;
    int stateMediaNotification;

    @SuppressLint("NewApi")
    public MediaHelpers(Context context, ArrayList<Song> songs){
        this.context = context;
        this.songs = songs;

        this.intentFilter = new IntentFilter();
        this.intentFilter.addCategory(Intent.CATEGORY_DEFAULT);

        this.intentFilter.addAction("ACTION_LAST_SONG");
        this.intentFilter.addAction("ACTION_STATE_SONG");
        this.intentFilter.addAction("ACTION_NEXT_SONG");
        this.intentFilter.addAction("ACTION_CREATE_TEMP_PLAYER");
        this.intentFilter.addAction("ACTION_DESTROY_TEMP_PLAYER");
        this.intentFilter.addAction("ACTION_START_MAIN_ACTIVITY");
        this.intentFilter.addAction("ACTION_DESTROY_NOTIFICATION");

        this.mediaSession = new MediaSession(context, "MediaNotificationSession");

        this.mediaStyle = new Notification.MediaStyle();
        this.mediaStyle.setShowActionsInCompactView(0,1,2);

        this.pendingIntentStartMainActivity = PendingIntent.getBroadcast(context, 0, new Intent("ACTION_START_MAIN_ACTIVITY"), PendingIntent.FLAG_MUTABLE);
        this.pendingIntentLastSong = PendingIntent.getBroadcast(context, 0, new Intent("ACTION_LAST_SONG"), PendingIntent.FLAG_MUTABLE);
        this.pendingIntentStateSong = PendingIntent.getBroadcast(context, 0, new Intent("ACTION_STATE_SONG"), PendingIntent.FLAG_MUTABLE);
        this.pendingIntentNextSong = PendingIntent.getBroadcast(context, 0, new Intent("ACTION_NEXT_SONG"), PendingIntent.FLAG_MUTABLE);

        this.notificationChannel = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            this.notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,"Виджет плеера", NotificationManager.IMPORTANCE_HIGH);
            this.notificationChannel.setSound(null, null);
            this.notificationChannel.enableVibration(false);
        }

        if(!isNotificationServiceEnabled()){
            context.startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
        }
    }

    public IntentFilter getIntentFilter() {
        return intentFilter;
    }

    public void createTempPlayer(ExoPlayer player){
        tempPlayer = player;
    }
    public void destroyTempPlayer(){
        tempPlayer.stop();
        tempPlayer.clearMediaItems();
        tempPlayer = null;
    }
    public ExoPlayer getTempPlayer(){
        return tempPlayer;
    }

    @SuppressLint("NewApi")
    public Notification createMediaNotification(int positionSong){
        Song notificationSong = songs.get(positionSong);

        if(tempPlayer != null){
            tempPlayer.setMediaItem(MediaItem.fromUri(notificationSong.getPath()));
        }

        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(String.valueOf(notificationSong.getPath()));
        byte[] data = mmr.getEmbeddedPicture();

        Bitmap albumArt = null;
        //Если обложки нет, то подставляем альтернативную абложку
        if(data == null){
            albumArt = BitmapFactory.decodeResource(context.getResources(), R.drawable.img_alternativ_song_album);
        } else {
            albumArt = BitmapFactory.decodeByteArray(data, 0, data.length);
        }

        String title = notificationSong.getTitle();
        String artist = notificationSong.getArtist();

        MediaMetadata metadata = new MediaMetadata.Builder()
                .putText(MediaMetadata.METADATA_KEY_TITLE, title)
                .putText(MediaMetadata.METADATA_KEY_ARTIST, artist)
                .putBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART, albumArt)
                .build();

        mediaSession.setMetadata(metadata);

        mediaStyle.setMediaSession(mediaSession.getSessionToken());

        Notification.Builder mediaNotificationBuilder = new Notification.Builder(context, NOTIFICATION_CHANNEL_ID);

        Notification mediaNotification = mediaNotificationBuilder
                .setPriority(Notification.PRIORITY_HIGH)
                .setChannelId(NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.icon_app)
                .setContentTitle(title)
                .setContentText(artist)
                .setLargeIcon(albumArt)
                .setStyle(mediaStyle)
                .addAction(
                        R.drawable.btn_last_song,
                        "LastSong",
                        pendingIntentLastSong
                )
                .addAction(
                        stateMediaNotification,
                        "StateSong",
                        pendingIntentStateSong
                )
                .addAction(
                        R.drawable.btn_next_song,
                        "NextSong",
                        pendingIntentNextSong
                )
                .setContentIntent(pendingIntentStartMainActivity)
                .build();

        return mediaNotification;
    }
    @SuppressLint("NewApi")
    public void showMediaNotification(int positionSong){
        this.mediaNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mediaNotificationManager.createNotificationChannel(notificationChannel);
        }

        mediaNotificationManager.notify(NOTIFICATION_ID, createMediaNotification(positionSong));
    }
    public void updateMediaNotification(int stateMediaNotification) {
        this.stateMediaNotification = stateMediaNotification;
    }
    public void destroyMediaNotification(){
        mediaNotificationManager.cancel(NOTIFICATION_ID);
    }
    private boolean isNotificationServiceEnabled(){
        String settings = Settings.Secure.getString(context.getContentResolver(), "enabled_notification_listeners");

        return settings.contains(new ComponentName(context, MediaNotificationListenerService.class).flattenToString());
    }

    public String mediaFormatDuration(long millis) {
        long allSeconds = millis / 1000;
        int allMinutes;
        byte seconds, minutes, hours;
        if (allSeconds >= 60) {
            allMinutes = (int) (allSeconds / 60);
            seconds = (byte) (allSeconds % 60);
            if (allMinutes >= 60) {
                hours = (byte) (allMinutes / 60);
                minutes = (byte) (allMinutes % 60);
                return String.format(Locale.getDefault(), "%d:%d:" + mediaFormatSeconds(seconds), hours, minutes, seconds);
            } else
                return String.format(Locale.getDefault(), "%d:" + mediaFormatSeconds(seconds), allMinutes, seconds);
        } else
            return String.format(Locale.getDefault(), "0:" + mediaFormatSeconds((byte) allSeconds), allSeconds);
    }
    public String mediaFormatSeconds(byte seconds) {
        String secondsFormatted;
        if (seconds < 10) secondsFormatted = "0%d";
        else secondsFormatted = "%d";
        return secondsFormatted;
    }
}
