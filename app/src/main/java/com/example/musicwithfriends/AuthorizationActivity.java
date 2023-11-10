package com.example.musicwithfriends;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.splashscreen.SplashScreen;

import com.example.musicwithfriends.Helpers.NetworkHelper;
import com.example.musicwithfriends.Models.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AuthorizationActivity extends AppCompatActivity {

    public static final String APP_PREFERENCES = "mysettings";
    private SharedPreferences mSettings;
    private SharedPreferences.Editor editor;
    private boolean isReady = false;
    private boolean isAuth;
    private NetworkHelper networkHelper;
    private ConstraintLayout root;
    private TextInputLayout emailInputLayout, passwordInputLayout;
    private TextInputEditText editTextEmail, editTextPassword;
    private Button btnOpenRegistration, btnLogIn;
    private FrameLayout LayoutBtnLogIn;
    private TextView textLogIn;
    private ProgressBar progressBar;
    private View revealView;
    private FirebaseAuth auth;
    private FirebaseDatabase db;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FirebaseApp.initializeApp(this);
        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(
                PlayIntegrityAppCheckProviderFactory.getInstance());

        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
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

        mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        isAuth = mSettings.getBoolean("stateAcc", false);
        if(isAuth) {
            startActivity(new Intent(AuthorizationActivity.this, MainActivity.class));
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authorization);

        networkHelper = new NetworkHelper(this);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                networkHelper.checkNetworkConnection();
            }
        }, 3500);

        root = findViewById(R.id.authorizationActivity);

        auth = FirebaseAuth.getInstance();

        revealView = findViewById(R.id.revealView);
        textLogIn = findViewById(R.id.textLogIn);
        progressBar = findViewById(R.id.progressBar);
        emailInputLayout = findViewById(R.id.loginInputLayout);
        passwordInputLayout = findViewById(R.id.passwordInputLayout);

        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);

        editTextEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                Pattern pattern = Pattern.compile("^([a-z0-9_\\.-]+)@([a-z0-9_\\.-]+)\\.([a-z\\.]{2,6})+$");
                Matcher matcher = pattern.matcher(editTextEmail.getText().toString());

                if(matcher.find() || TextUtils.isEmpty(editTextEmail.getText().toString())){
                    emailInputLayout.setErrorEnabled(false);
                }
                else {
                    emailInputLayout.setErrorEnabled(true);
                    emailInputLayout.setError("Некорректная электронная почта");
                }
            }
        });

        LayoutBtnLogIn = findViewById(R.id.LayoutBtnLogIn);
        btnLogIn = findViewById(R.id.btnLogIn);
        btnLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(networkHelper.checkNetworkConnection()){
                    LogIn();
                }
            }
        });

        btnOpenRegistration = findViewById(R.id.btnOpenRegistration);
        btnOpenRegistration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(networkHelper.checkNetworkConnection()){
                    editTextEmail.setText("");
                    editTextPassword.setText("");
                    Intent intent = new Intent(AuthorizationActivity.this, RegistrationActivity.class);
                    startActivity(intent);
                }
            }
        });
    }

    private void load(){
        animateButtonWidth();
        fadeOutTextAndSetProgressBar();
    }
    private void animateButtonWidth(){
        ValueAnimator anim = ValueAnimator.ofInt(LayoutBtnLogIn.getMeasuredWidth(), getFinalWidth());
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(@NonNull ValueAnimator valueAnimator) {
                int value = (Integer) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = LayoutBtnLogIn.getLayoutParams();
                layoutParams.width = value;
                LayoutBtnLogIn.requestLayout();
            }
        });

        anim.setDuration(250);
        anim.start();
    }
    private void fadeOutTextAndSetProgressBar(){
        textLogIn.animate().alpha(0f).setDuration(250)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        showProgressDialog();
                    }
                })
                .start();
    }
    private void showProgressDialog(){
        progressBar.getIndeterminateDrawable().setColorFilter(Color.parseColor("#FF9234"), PorterDuff.Mode.SRC_IN);
        progressBar.setVisibility(View.VISIBLE);
    }
    private void nextAction(){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                revealButton();
                fadeOutProgressDialog();
                delayedStartNextActivity();
            }
        }, 2000);
    }
    private void revealButton(){
        LayoutBtnLogIn.setElevation(0f);
        revealView.setVisibility(View.VISIBLE);

        int x = revealView.getWidth();
        int y = revealView.getHeight();

        int startX = (int) (getFinalWidth() / 2 + LayoutBtnLogIn.getX());
        int startY = (int) (getFinalWidth() / 2 + LayoutBtnLogIn.getY());

        float radius = Math.max(x,y) * 1.2f;

        Animator reveal = ViewAnimationUtils.createCircularReveal(revealView,startX,startY,getFinalWidth(),radius);
        reveal.setDuration(350);
        reveal.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                finish();
            }
        });

        reveal.start();
    }
    private void fadeOutProgressDialog(){
        progressBar.animate().alpha(0f).setDuration(200).start();
    }
    private void delayedStartNextActivity(){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(AuthorizationActivity.this, MainActivity.class);
                startActivity(intent);
            }
        }, 100);
    }
    private int getFinalWidth(){
        return  (int) getResources().getDimension(R.dimen.get_width);
    }
    private void dismissSplashScreen() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                isReady = true;
            }
        }, 1000);
    }
    public void LogIn(){
        btnLogIn.setClickable(false);

        if(TextUtils.isEmpty(editTextEmail.getText().toString())){
            Snackbar.make(root, "Введите почту!", Snackbar.LENGTH_LONG).show();
            btnLogIn.setClickable(true);
            return;
        }

        if(TextUtils.isEmpty(editTextPassword.getText().toString())){
            Snackbar.make(root, "Введите пароль!", Snackbar.LENGTH_LONG).show();
            btnLogIn.setClickable(true);
            return;
        }

        btnOpenRegistration.setVisibility(View.INVISIBLE);

        load();
        //Доступно пять попыток входа на один акк, потом ставиться блок на пару минут
        auth.signInWithEmailAndPassword(
                editTextEmail.getText().toString().trim(),
                editTextPassword.getText().toString().trim()
        ).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                editor = mSettings.edit();
                editor.putBoolean("stateAcc", true);
                editor.apply();

                FirebaseDatabase db = FirebaseDatabase.getInstance();
                DatabaseReference users = db.getReference("users/" + auth.getUid());

                users.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        User user = dataSnapshot.getValue(User.class);

                        editor.putString("userName", user.getName());
                        editor.apply();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {}
                });

                nextAction();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        btnOpenRegistration.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.INVISIBLE);
                        textLogIn.animate().cancel();

                        ValueAnimator anim = ValueAnimator.ofInt(100, 400);
                        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(@NonNull ValueAnimator valueAnimator) {
                                int value = (Integer) valueAnimator.getAnimatedValue();
                                ViewGroup.LayoutParams layoutParams = LayoutBtnLogIn.getLayoutParams();
                                layoutParams.width = value;
                                LayoutBtnLogIn.requestLayout();
                            }
                        });
                        anim.addListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                textLogIn.setAlpha(1f);
                            }
                        });

                        anim.setDuration(250);
                        anim.start();

                        Snackbar.make(root, "Неверная почта или пароль!", Snackbar.LENGTH_LONG).show();
                        btnLogIn.setClickable(true);
                    }
                }, 2000);
            }
        });
    }
}