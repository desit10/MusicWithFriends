package com.example.musicwithfriends.Adapters;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.media3.common.util.UnstableApi;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musicwithfriends.Models.Playlist;
import com.example.musicwithfriends.Models.Song;
import com.example.musicwithfriends.R;
import com.google.gson.Gson;

import java.util.ArrayList;

@UnstableApi
public class DialogPlaylistsAdapter extends RecyclerView.Adapter<DialogPlaylistsAdapter.ViewHolder>{

    public static final String APP_PREFERENCES = "mysettings";
    private SharedPreferences mSettings;
    private SharedPreferences.Editor editor;
    Context context;
    ArrayList<Playlist> playlists;
    RecyclerView playlistsRecycler;
    PlaylistsAdapter playlistsAdapter;
    Dialog dialog, mainDialog;
    Song song;
    int positionPlaylist;
    public DialogPlaylistsAdapter(Context context, ArrayList<Playlist> playlists, PlaylistsAdapter playlistsAdapter,
                                  int positionPlaylist, Dialog dialog, Dialog mainDialog) {
        this.context = context;
        this.playlists = playlists;
        this.playlistsAdapter = playlistsAdapter;
        this.positionPlaylist = positionPlaylist;
        this.dialog = dialog;
        this.mainDialog = mainDialog;

        mSettings = context.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_playlist, parent, false);

        return new DialogPlaylistsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {

        if(position != positionPlaylist){

            Playlist playlist = playlists.get(position);
            int playlistSize = playlist.getSongs().size();

            holder.playlistTitle.setText(playlist.getName());

            if(playlistSize == 1) {
                holder.songCount.setText(String.valueOf(playlist.getSongs().size()) + " трек");
            }
            else if (playlistSize <= 4) {
                holder.songCount.setText(String.valueOf(playlist.getSongs().size()) + " трека");
            }
            else {
                holder.songCount.setText(String.valueOf(playlist.getSongs().size()) + " треков");
            }

            holder.playlistManagement.setEnabled(false);
            holder.playlistManagement.setAlpha(0f);

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    playlist.getSongs().add(song);

                    Gson gsonOld = new Gson();
                    String jsonOld = gsonOld.toJson(playlists);
                    editor = mSettings.edit();
                    editor.putString("Playlists", jsonOld);
                    editor.apply();

                    playlistsAdapter.setPlaylists(playlists);
                    playlistsAdapter.notifyDataSetChanged();

                    dialog.dismiss();
                    mainDialog.dismiss();
                }
            });

        } else {
            holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0,0));
        }

    }

    public void setSong(Song song) {
        this.song = song;
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        playlistsRecycler = recyclerView;
    }

    @Override
    public int getItemCount() {
        return playlists.size();
    }

    public  class ViewHolder extends RecyclerView.ViewHolder{

        ImageButton playlistManagement;
        TextView playlistTitle, songCount;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            playlistTitle = itemView.findViewById(R.id.playlistTitle);
            songCount = itemView.findViewById(R.id.songCount);

            playlistManagement = itemView.findViewById(R.id.playlistManagement);
        }
    }
}
