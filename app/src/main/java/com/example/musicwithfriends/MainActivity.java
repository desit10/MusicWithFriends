package com.example.musicwithfriends;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.splashscreen.SplashScreen;
import androidx.core.widget.ImageViewCompat;
import androidx.media3.common.util.UnstableApi;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musicwithfriends.Adapters.DialogSongsAdapter;
import com.example.musicwithfriends.Helpers.FirebaseHelper;
import com.example.musicwithfriends.Helpers.NetworkHelper;
import com.example.musicwithfriends.Models.Room;
import com.example.musicwithfriends.Models.Song;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.ArrayList;

@UnstableApi
public class MainActivity extends AppCompatActivity {
    public static final String APP_PREFERENCES = "mysettings";
    private SharedPreferences mSettings;
    private SharedPreferences.Editor editor;
    private NetworkHelper networkHelper;
    private FloatingActionButton floatingActionButton;
    private BottomNavigationView bottomNavigationView;
    private NavHostFragment navHostFragment;
    private NavController navController;
    private MenuItem menuItem;
    private int BackFromAppCount = 0;
    private boolean isReady = false;
    private Room room = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SplashScreen.installSplashScreen(this);
        View content = findViewById(android.R.id.content);
        content.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                if(isReady){
                    content.getViewTreeObserver().removeOnPreDrawListener(this);
                }
                dismissSplashScreen();
                return false;
            }
        });

        setContentView(R.layout.activity_main);

        /*networkHelper = new NetworkHelper(this);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                networkHelper.checkNetworkConnection();
            }
        }, 2000);*/

        mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        editor = mSettings.edit();
        editor.putBoolean("stateSongsAdapter", true);
        editor.apply();

        // Получение ссылки из интента
        Intent intent = getIntent();
        Uri data = intent.getData();

        //Оболочка для фрагментов
        navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        navController = navHostFragment.getNavController();

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        floatingActionButton = findViewById(R.id.floatingActionButton);

        bottomNavigationView.setBackground(null);
        bottomNavigationView.setSelectedItemId(0);
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                BackFromAppCount = 0;
                menuItem = item;
                ImageViewCompat.setImageTintList(floatingActionButton, ColorStateList.valueOf(ContextCompat.getColor(MainActivity.this, R.color.smoky_white)));
                item.setCheckable(true);
                item.setChecked(true);
                navController.popBackStack();
                navController.navigate(item.getItemId());
                return false;
            }
        });

        if (data != null) {
            // Выполнение нужных действий на основе ссылки
            String path = data.getPath();

            String[] separated = path.split("/");
            String roomId = separated[separated.length - 1];

            FirebaseHelper firebaseHelper = new FirebaseHelper();
            DatabaseReference rooms = firebaseHelper.Request("rooms");
            rooms.orderByKey().equalTo(roomId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Log.e("Room", dataSnapshot.toString());

                        DialogSongsAdapter dialogSongsAdapter = new DialogSongsAdapter(MainActivity.this);

                        room = dataSnapshot.getValue(Room.class);

                        if(dialogSongsAdapter.getItemCount() != 0){
                            Dialog dialog = new Dialog(MainActivity.this);
                            dialog.setContentView(R.layout.dialog_connection_in_room);
                            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
                            dialog.getWindow().getAttributes().windowAnimations = R.style.DialogPlaylistAnimation;

                            RecyclerView songsRecycler = dialog.findViewById(R.id.songsRecycler);
                            LinearLayoutManager layoutManager = new LinearLayoutManager(MainActivity.this, RecyclerView.VERTICAL, false);

                            songsRecycler.setLayoutManager(layoutManager);
                            songsRecycler.setAdapter(dialogSongsAdapter);

                            Button btnClose = dialog.findViewById(R.id.btnClose);
                            Button btnSave = dialog.findViewById(R.id.btnSave);

                            btnClose.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    dialog.dismiss();
                                }
                            });
                            btnSave.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    ArrayList<Song> roomPlaylist = room.getRoomPlaylist();
                                    ArrayList<Song> userSong = dialogSongsAdapter.getRoomSong();
                                    if(userSong.size() != 0) {

                                        ArrayList<Song> deleteSongs = new ArrayList<>();

                                        roomPlaylist.addAll(userSong);

                                        for (int i = 0; i < userSong.size(); i++) {
                                            for (int j = 0; j < roomPlaylist.size(); j++) {
                                                if (roomPlaylist.get(j).getTitle().equals(userSong.get(i).getTitle())) {
                                                    deleteSongs.add(userSong.get(i));
                                                }
                                            }
                                        }
                                        roomPlaylist.removeAll(deleteSongs);


                                        for (int i = 0; i < roomPlaylist.size(); i++) {
                                            Uri uriFile = Uri.fromFile(new File(roomPlaylist.get(i).getPath()));

                                            StorageReference storageRef = FirebaseStorage.getInstance().getReference();
                                            StorageReference ref = storageRef.child(roomId + "/" + roomPlaylist.get(i).getSongName());

                                            UploadTask uploadTask = ref.putFile(uriFile);
                                            int counter = i;
                                            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                                    uploadTask.cancel();
                                                    if (counter == roomPlaylist.size() - 1) {
                                                        dialog.dismiss();

                                                        Intent roomIntent = new Intent(MainActivity.this, ClientActivity.class);
                                                        roomIntent.putExtra("ROOM_ID", roomId);
                                                        startActivity(roomIntent);
                                                    }
                                                }
                                            });
                                        }
                                    } else {
                                        dialog.dismiss();

                                        Intent roomIntent = new Intent(MainActivity.this, ClientActivity.class);
                                        roomIntent.putExtra("ROOM_ID", roomId);
                                        startActivity(roomIntent);
                                    }

                                    room.setHost(false);
                                    room.setRoomPlaylist(roomPlaylist);
                                    rooms.child(roomId).setValue(room);
                                }
                            });

                            dialog.show();
                        } else {
                            Intent roomIntent = new Intent(MainActivity.this, ClientActivity.class);
                            roomIntent.putExtra("ROOM_ID", roomId);
                            startActivity(roomIntent);

                            room.setHost(false);
                            rooms.child(roomId).setValue(room);
                        }

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        }

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BackFromAppCount = 0;
                if(menuItem != null){
                    menuItem.setCheckable(false);
                }
                ImageViewCompat.setImageTintList(floatingActionButton, ColorStateList.valueOf(ContextCompat.getColor(MainActivity.this, R.color.black)));
                navController.popBackStack();
                navController.navigate(R.id.musicsFragment);
            }
        });

        //Выход из приложения
        OnBackPressedCallback exitApp = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                BackFromAppCount++;

                if(BackFromAppCount == 1){
                    Toast.makeText(MainActivity.this, "Нажмите ещё раз для выхода ", Toast.LENGTH_SHORT).show();
                }

                if(BackFromAppCount == 2){
                    finishAffinity();
                }
            }
        };
        getOnBackPressedDispatcher().addCallback(this, exitApp);
    }

    private void dismissSplashScreen() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                isReady = true;
            }
        }, 1000);
    }
}