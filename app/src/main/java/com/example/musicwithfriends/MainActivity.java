package com.example.musicwithfriends;

import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.splashscreen.SplashScreen;
import androidx.core.widget.ImageViewCompat;
import androidx.media3.common.util.UnstableApi;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.musicwithfriends.Adapters.CurrentSongsAdapter;
import com.example.musicwithfriends.Helpers.FirebaseHelper;
import com.example.musicwithfriends.Models.Room;
import com.example.musicwithfriends.Models.Song;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

@UnstableApi
public class MainActivity extends AppCompatActivity {
    public static final String APP_PREFERENCES = "mysettings";
    private SharedPreferences mSettings;
    private SharedPreferences.Editor editor;
    private FloatingActionButton floatingActionButton;
    private BottomNavigationView bottomNavigationView;
    private NavHostFragment navHostFragment;
    private NavController navController;
    private MenuItem menuItem;
    private int BackFromAppCount = 0;
    private boolean isReady = false;
    private Room room = null;
    private String roomId;
    private Uri uriAvatar;

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

        mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        editor = mSettings.edit();
        editor.putBoolean("stateSongsAdapter", true);
        editor.apply();

        // Получение ссылки из намерения
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
                    Toast.makeText(MainActivity.this, "Нажмите ещё раз для выхода", Toast.LENGTH_SHORT).show();
                }

                if(BackFromAppCount == 2){
                    finishAffinity();
                }
            }
        };
        getOnBackPressedDispatcher().addCallback(this, exitApp);

        if (data != null) {
            // Выполнение нужных действий на основе ссылки
            String path = data.getPath();

            String[] separated = path.split("/");
            roomId = separated[separated.length - 1];

            FirebaseHelper firebaseHelper = new FirebaseHelper();
            DatabaseReference roomsRef = firebaseHelper.Request("rooms");
            roomsRef.orderByKey().equalTo(roomId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {

                        room = dataSnapshot.getValue(Room.class);

                        Dialog dialog = new Dialog(MainActivity.this);
                        dialog.setContentView(R.layout.dialog_connection_in_room);
                        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
                        dialog.getWindow().getAttributes().windowAnimations = R.style.AnimationDialog;

                        EditText editTextNickname = dialog.findViewById(R.id.editTextNickname);

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
                                String nickname = editTextNickname.getText().toString().trim();
                                if(!TextUtils.isEmpty(nickname)){
                                    startClientActivity(roomId, nickname);
                                }
                            }
                        });

                        dialog.show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        PendingIntent pendingIntentDestroyTempPlayer = PendingIntent.getBroadcast(this, 0, new Intent("ACTION_DESTROY_TEMP_PLAYER"), PendingIntent.FLAG_MUTABLE);
        try {
            pendingIntentDestroyTempPlayer.send();
        } catch (PendingIntent.CanceledException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        PendingIntent pendingIntentCreateTempPlayer = PendingIntent.getBroadcast(this, 0, new Intent("ACTION_CREATE_TEMP_PLAYER"), PendingIntent.FLAG_MUTABLE);
        try {
            pendingIntentCreateTempPlayer.send();
        } catch (PendingIntent.CanceledException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        PendingIntent pendingIntentDestroyNotification = PendingIntent.getBroadcast(this, 0, new Intent("ACTION_DESTROY_NOTIFICATION"), PendingIntent.FLAG_MUTABLE);
        try {
            pendingIntentDestroyNotification.send();
        } catch (PendingIntent.CanceledException e) {
            throw new RuntimeException(e);
        }
    }

    private ArrayList<Song> deletingDuplicateSongs(ArrayList<Song> roomPlaylist, ArrayList<Song> userSongs){
        for (int i = 0; i < roomPlaylist.size(); i++) {
            for (int j = 0; j < userSongs.size(); j++) {
                if (roomPlaylist.get(i).getTitle().equals(userSongs.get(j).getTitle()) &&
                        roomPlaylist.get(i).getArtist().equals(userSongs.get(j).getArtist())) {
                    Log.e("SONG_DELETE", userSongs.get(j).getTitle());

                    userSongs.remove(j);
                }
            }
        }

        return roomPlaylist;
    }

    private void startClientActivity(String roomId, String nickname){
        Intent roomIntent = new Intent(MainActivity.this, ClientActivity.class);
        roomIntent.putExtra("ROOM_ID", roomId);
        roomIntent.putExtra("NICKNAME", nickname);

        startActivity(roomIntent);
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