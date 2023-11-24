package com.example.musicwithfriends.Adapters;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.fragment.app.FragmentActivity;
import androidx.media3.common.util.UnstableApi;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musicwithfriends.Models.Playlist;
import com.example.musicwithfriends.Models.Song;
import com.example.musicwithfriends.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;

@UnstableApi
public class PlaylistsAdapter extends RecyclerView.Adapter<PlaylistsAdapter.ViewHolder>{

    public static final String APP_PREFERENCES = "mysettings";
    private SharedPreferences mSettings;
    private SharedPreferences.Editor editor;

    Context context;
    FragmentActivity mainActivity;
    ArrayList<Playlist> playlists;
    RecyclerView recyclerPlaylist, recyclerSongs;
    SongsAdapter songsAdapter;
    View.OnClickListener onClickListener;
    ValueAnimator anim;
    int positionItem = 0;

    public void setPlaylists(ArrayList<Playlist> playlists) {
        this.playlists = playlists;
    }

    public PlaylistsAdapter(Context context, FragmentActivity mainActivity, ArrayList<Playlist> playlists,
                            RecyclerView recyclerPlaylist, RecyclerView recyclerSongs, View.OnClickListener onClickListener) {
        this.context = context;
        this.playlists = playlists;
        this.recyclerSongs = recyclerSongs;
        this.recyclerPlaylist = recyclerPlaylist;
        this.mainActivity = mainActivity;
        this.onClickListener = onClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_playlist, parent, false);

        return new PlaylistsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {

        Playlist playlist = playlists.get(position);
        int playlistSize = playlist.getSongs().size();

        if(position == 0){
            holder.playlistManagement.setEnabled(false);
            holder.playlistManagement.setAlpha(0f);
        }

        if(position == 0){
            songsAdapter.setPlaylistsAdapter((PlaylistsAdapter) holder.getBindingAdapter());
        }

        holder.playlistTitle.setText(playlist.getName());

        if(playlistSize == 1) {
            holder.songCount.setText(String.valueOf(playlist.getSongs().size()) + " трек");
        }
        else if (playlistSize <= 4 && playlistSize != 0) {
            holder.songCount.setText(String.valueOf(playlist.getSongs().size()) + " трека");
        }
        else {
            holder.songCount.setText(String.valueOf(playlist.getSongs().size()) + " треков");
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animRecyclerPlaylist(position);

                onClickListener.onClick(v);

                createRecyclerSongs(playlist.getSongs());
                songsAdapter.setPlaylistsAdapter((PlaylistsAdapter) holder.getBindingAdapter());
            }
        });

        holder.playlistManagement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBottomDialog(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return playlists.size();
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

        createRecyclerSongs(playlists.get(0).getSongs());
    }

    @Override
    public void onViewRecycled(@NonNull PlaylistsAdapter.ViewHolder holder) {
        super.onViewRecycled(holder);
        holder.setIsRecyclable(true);
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull PlaylistsAdapter.ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.setIsRecyclable(false);
    }

    public int getPositionItem() {
        return positionItem;
    }

    private void setPositionItem(int positionItem) {
        this.positionItem = positionItem;
    }

    private void createRecyclerSongs(ArrayList<Song> songs){
        mSettings = context.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);

        recyclerSongs.setLayoutManager(new LinearLayoutManager(context));

        songsAdapter = new SongsAdapter(context, mainActivity, songs,
                mSettings.getBoolean("stateSongsAdapter", true));
        songsAdapter.setPositionPlaylist(positionItem);

        recyclerSongs.setItemAnimator(null);
        recyclerSongs.setAdapter(songsAdapter);

        editor = mSettings.edit();
        editor.putBoolean("stateSongsAdapter", false);
        editor.apply();

        recyclerSongs.setTranslationX(-1000f);
        recyclerSongs.animate().translationXBy(1000f).setDuration(500).setStartDelay(250).start();
    }

    private void animRecyclerPlaylist(int position){

        int playlistsRecyclerHeight = (int) context.getResources().getDimension(R.dimen.get_playlists_recycler_height);

        anim = ValueAnimator.ofInt(recyclerPlaylist.getMeasuredHeight(), playlistsRecyclerHeight);

        recyclerPlaylist.suppressLayout(false);

        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(@NonNull ValueAnimator valueAnimator) {
                int value = (Integer) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = recyclerPlaylist.getLayoutParams();
                layoutParams.height = value;
                recyclerPlaylist.requestLayout();
            }
        });

        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                if(position != 0){
                    recyclerPlaylist.scrollToPosition(position);
                }

                recyclerPlaylist.suppressLayout(true);
            }
        });

        setPositionItem(position);

        anim.setDuration(250);
        anim.start();
    }
    public void showBottomDialog(int positionItem) {
        Dialog mainDialog = new Dialog(context);
        mainDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mainDialog.setContentView(R.layout.bottom_sheet_layout_playlist);

        mainDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mainDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mainDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        mainDialog.getWindow().setGravity(Gravity.BOTTOM);

        LinearLayout layoutRenamePlaylist = mainDialog.findViewById(R.id.layoutRenamePlaylist);
        LinearLayout layoutShare = mainDialog.findViewById(R.id.layoutShare);
        LinearLayout layoutDeletePlaylist = mainDialog.findViewById(R.id.layoutDeletePlaylist);

        ImageView cancelButton = mainDialog.findViewById(R.id.cancelButton);

        View.OnClickListener dialogOnClickListener = new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if(v.getId() == R.id.layoutRenamePlaylist){

                    Gson gson = new Gson();
                    Type typeArrayPlaylist = new TypeToken<ArrayList<Playlist>>(){}.getType();
                    String json = mSettings.getString("Playlists", "");
                    playlists = gson.fromJson(json, typeArrayPlaylist);

                    Dialog dialog = new Dialog(context);
                    dialog.setContentView(R.layout.dialog_rename_playlist);
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
                    dialog.getWindow().getAttributes().windowAnimations = R.style.DialogPlaylistAnimation;

                    TextInputLayout layoutNamePlaylist = dialog.findViewById(R.id.layoutNamePlaylist);
                    TextInputEditText namePlaylist = dialog.findViewById(R.id.namePlaylist);

                    Button btnClose = dialog.findViewById(R.id.btnClose);
                    Button btnRename = dialog.findViewById(R.id.btnRename);

                    btnClose.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });

                    btnRename.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(namePlaylist.getText().toString().length() > 0){

                                playlists.get(positionItem).setName(namePlaylist.getText().toString());

                                Gson gsonOld = new Gson();
                                String jsonOld = gsonOld.toJson(playlists);
                                editor = mSettings.edit();
                                editor.putString("Playlists", jsonOld);
                                editor.apply();

                                notifyItemChanged(positionItem);

                                namePlaylist.setText("");

                                dialog.dismiss();
                                mainDialog.dismiss();
                            } else {
                                layoutNamePlaylist.setErrorEnabled(true);
                                layoutNamePlaylist.setError("Введите название плейлиста!");

                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        layoutNamePlaylist.setErrorEnabled(false);
                                    }
                                }, 2000);
                            }
                        }
                    });

                    dialog.show();

                }
                if(v.getId() == R.id.layoutShare){

                    ArrayList<Uri> uris = new ArrayList<>();

                    for( Song song : playlists.get(positionItem).getSongs()){
                        Uri uriSong = FileProvider.getUriForFile(context, "com.example.musicwithfriends.provider", new File(song.getPath()));
                        uris.add(uriSong);
                    }

                    Intent share = new Intent(Intent.ACTION_SEND_MULTIPLE);
                    share.setType("audio/*");
                    share.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    share.putExtra(Intent.EXTRA_STREAM, uris);
                    context.startActivity(Intent.createChooser(share, null));

                    mainDialog.dismiss();
                }
                if(v.getId() == R.id.layoutDeletePlaylist){
                    playlists.remove(positionItem);

                    Gson gsonOld = new Gson();
                    String jsonOld = gsonOld.toJson(playlists);
                    editor = mSettings.edit();
                    editor.putString("Playlists", jsonOld);
                    editor.apply();

                    notifyItemRemoved(positionItem);

                    mainDialog.dismiss();
                }
                if(v.getId() == R.id.cancelButton){
                    mainDialog.dismiss();
                }

            }
        };
        layoutRenamePlaylist.setOnClickListener(dialogOnClickListener);
        layoutShare.setOnClickListener(dialogOnClickListener);
        layoutDeletePlaylist.setOnClickListener(dialogOnClickListener);
        cancelButton.setOnClickListener(dialogOnClickListener);


        mainDialog.show();
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
