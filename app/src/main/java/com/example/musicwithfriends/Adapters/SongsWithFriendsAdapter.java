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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.media3.common.util.UnstableApi;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musicwithfriends.Helpers.FirebaseHelper;
import com.example.musicwithfriends.Models.Room;
import com.example.musicwithfriends.Models.Song;
import com.example.musicwithfriends.R;
import com.google.firebase.database.DatabaseReference;

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
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(song.getPath());
        byte[] data = mmr.getEmbeddedPicture();

        //Если обложки нет, то подставляем альтернативную абложку
        if(data == null){
            holder.songAlbum.setImageResource(R.drawable.img_alternativ_song_album);
        } else {
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            holder.songAlbum.setImageBitmap(bitmap);
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
                /*DatabaseReference updateStateSong = firebaseHelper.Request("rooms/" + roomId + "/stateSong");
                updateStateSong.setValue(true);*/
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
