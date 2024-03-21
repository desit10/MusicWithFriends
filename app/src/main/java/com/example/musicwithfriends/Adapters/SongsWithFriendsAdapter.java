package com.example.musicwithfriends.Adapters;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.fragment.app.FragmentActivity;
import androidx.media3.common.util.UnstableApi;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musicwithfriends.Helpers.FirebaseHelper;
import com.example.musicwithfriends.Helpers.SnapHelperOneByOne;
import com.example.musicwithfriends.Models.Playlist;
import com.example.musicwithfriends.Models.Room;
import com.example.musicwithfriends.Models.Song;
import com.example.musicwithfriends.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;

@UnstableApi
public class SongsWithFriendsAdapter extends RecyclerView.Adapter<SongsWithFriendsAdapter.ViewHolder>{

    Context context;
    Room room;
    RecyclerView recyclerCurrentSong;
    String roomId;
    FirebaseHelper firebaseHelper = new FirebaseHelper();

    public SongsWithFriendsAdapter(Context context, RecyclerView recyclerCurrentSong, Room room, String roomId) {
        this.context = context;
        this.recyclerCurrentSong = recyclerCurrentSong;
        this.room = room;
        this.roomId = roomId;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_song, parent, false);

        return new SongsWithFriendsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {

        Song song = room.getRoomPlaylist().get(position);

        //Создание обложки песни
        android.media.MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        if(new File(song.getPath()).exists()){
            mmr.setDataSource(song.getPath());
            byte[] data = mmr.getEmbeddedPicture();

            //Если обложки нет, то подставляем альтернативную абложку
            if(data == null){
                holder.songAlbum.setImageResource(R.drawable.alternativ_song_album);
            } else {
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                holder.songAlbum.setImageBitmap(bitmap);
            }
        } else {
            holder.songAlbum.setImageResource(R.drawable.alternativ_song_album);
        }

        holder.songTitle.setText(song.getTitle());
        holder.songArtist.setText(song.getArtist());

        //При нажатии на песню в списке передаём позицию в список текущих песен
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recyclerCurrentSong.scrollToPosition(position);
                DatabaseReference updateCurrentSong = firebaseHelper.Request("rooms/" + roomId + "/currentSong");
                updateCurrentSong.setValue(song.getSongName());
                DatabaseReference updateStateSong = firebaseHelper.Request("rooms/" + roomId + "/stateSong");
                updateStateSong.setValue(true);
            }
        });

    }

    @Override
    public int getItemCount() {
        return room.getRoomPlaylist().size();
    }

    public  class ViewHolder extends RecyclerView.ViewHolder{

        FrameLayout frameSong;
        ImageView songAlbum;
        ImageButton contextMenu;
        TextView songTitle, songArtist;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            frameSong = itemView.findViewById(R.id.frameSong);

            songAlbum = itemView.findViewById(R.id.songAlbum);

            contextMenu = itemView.findViewById(R.id.contextMenu);

            songTitle = itemView.findViewById(R.id.songTitle);
            songArtist = itemView.findViewById(R.id.songArtist);
        }
    }
}
