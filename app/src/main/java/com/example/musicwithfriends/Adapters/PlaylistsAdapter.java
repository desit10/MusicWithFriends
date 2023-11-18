package com.example.musicwithfriends.Adapters;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.media3.common.util.UnstableApi;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musicwithfriends.Models.Playlist;
import com.example.musicwithfriends.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

@UnstableApi
public class PlaylistsAdapter extends RecyclerView.Adapter<PlaylistsAdapter.ViewHolder>{

    public static final String APP_PREFERENCES = "mysettings";
    private SharedPreferences mSettings;
    private SharedPreferences.Editor editor;
    FragmentActivity mainActivity;


    Context context;
    ArrayList<Playlist> playlists;
    RecyclerView recyclerPlaylist, recyclerSongs;
    View.OnClickListener onClickListener;
    ValueAnimator anim;
    int positionItem = 0;
    Boolean stateAdapter;
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

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*Collections.swap(playlists, position, 0);
                notifyItemMoved(position, 0);*/

                animRecyclerPlaylist(position);

                onClickListener.onClick(v);

                mSettings = context.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);

                recyclerSongs.setLayoutManager(new LinearLayoutManager(context));
                recyclerSongs.setAdapter(new SongsAdapter(context, mainActivity, playlist.getSongs(), mSettings.getBoolean("stateSongsAdapter", true)));

                editor = mSettings.edit();
                editor.putBoolean("stateSongsAdapter", false);
                editor.apply();

                recyclerSongs.setTranslationX(-1000f);
                recyclerSongs.animate().translationXBy(1000f).setDuration(500).setStartDelay(250).start();
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
        Playlist playlist = playlists.get(0);

        mSettings = context.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);

        recyclerSongs.setLayoutManager(new LinearLayoutManager(context));
        recyclerSongs.setAdapter(new SongsAdapter(context, mainActivity, playlist.getSongs(), mSettings.getBoolean("stateSongsAdapter", true)));

        editor = mSettings.edit();
        editor.putBoolean("stateSongsAdapter", false);
        editor.apply();

        recyclerSongs.setTranslationX(-1000f);
        recyclerSongs.animate().translationXBy(1000f).setDuration(500).setStartDelay(250).start();

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

    private void animRecyclerPlaylist(int position){

        int playlistsRecyclerHeight = (int) context.getResources().getDimension(R.dimen.get_playlists_recycler_height);

        anim = ValueAnimator.ofInt(recyclerPlaylist.getMeasuredHeight(), playlistsRecyclerHeight);

        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(@NonNull ValueAnimator valueAnimator) {
                int value = (Integer) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = recyclerPlaylist.getLayoutParams();
                layoutParams.height = value;
                recyclerPlaylist.requestLayout();
            }
        });

        if(position != 0){
            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    recyclerPlaylist.scrollToPosition(position);
                }
            });
        }

        setPositionItem(position);

        anim.setDuration(250);
        anim.start();
    }

    public  class ViewHolder extends RecyclerView.ViewHolder{

        TextView playlistTitle, songCount;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            playlistTitle = itemView.findViewById(R.id.playlistTitle);
            songCount = itemView.findViewById(R.id.songCount);
        }
    }
}
