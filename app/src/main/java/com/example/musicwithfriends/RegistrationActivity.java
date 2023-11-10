package com.example.musicwithfriends;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.musicwithfriends.Helpers.DialogHelper;
import com.example.musicwithfriends.Helpers.NetworkHelper;
import com.example.musicwithfriends.Models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegistrationActivity extends AppCompatActivity {

    private NetworkHelper networkHelper;
    private LinearLayout root;
    private DialogHelper dialogHelper;
    private TextInputLayout parent;
    private TextInputEditText editTextName, editTextEmail, editTextPassword;
    private Button btnSignIn, btnBackAuth;
    private FirebaseAuth auth;
    private FirebaseDatabase db;
    private DatabaseReference users;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        networkHelper = new NetworkHelper(this);

        dialogHelper = new DialogHelper(this);
        root = findViewById(R.id.registrationActivity);

        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        users = db.getReference("users");

        //Регистрация пользователя
        btnSignIn = findViewById(R.id.btnSignIn);
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (networkHelper.checkNetworkConnection()) {
                    dialogHelper.showDialogProgressBar();

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            users.orderByKey().addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    Boolean permission = true;
                                    String name = editTextName.getText().toString().trim();
                                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                        String userName= snapshot.getValue(User.class).getName();

                                        if (name.equals(userName)) {
                                            permission = false;

                                            parent = (TextInputLayout) findViewById(editTextName.getId()).getParent().getParent();
                                            parent.setErrorEnabled(true);
                                            parent.setError("Данное имя уже используется");
                                        }
                                    }

                                    if(permission){
                                        SignIn();
                                    } else {
                                        dialogHelper.dialogDismiss();
                                        dialogHelper.showDialogUndoneRegistr();
                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                dialogHelper.dialogDismiss();
                                            }
                                        }, 2000);
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {}
                            });
                        }
                    }, 2000);
                }
            }
        });

        //Возврат к авторизации
        btnBackAuth = findViewById(R.id.btnBackAuthorization);
        btnBackAuth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        //Поля ввода
        ////////////////////////////////////////////
        //Ник
        editTextName = findViewById(R.id.name);
        //Email
        editTextEmail = findViewById(R.id.email);
        //Пароль
        editTextPassword = findViewById(R.id.password);
        ///////////////////////////////////////////////

        //Регулярные выражения
        ///////////////////////////////////////////////
        editTextName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                parent = (TextInputLayout) findViewById(editTextName.getId()).getParent().getParent();

                Pattern pattern = Pattern.compile("^[А-яA-z0-9_]{2,}+$");
                Matcher matcher = pattern.matcher(editTextName.getText().toString().trim());

                if(matcher.find() || TextUtils.isEmpty(editTextName.getText().toString())){
                    parent.setErrorEnabled(false);
                }
                else{
                    parent.setErrorEnabled(true);
                    parent.setError("Некорректное имя");
                }
            }
        });
        editTextEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                parent = (TextInputLayout) findViewById(editTextEmail.getId()).getParent().getParent();

                Pattern pattern = Pattern.compile("^([a-z0-9_\\.-]+)@([a-z0-9_\\.-]+)\\.([a-z\\.]{2,6})+$");
                Matcher matcher = pattern.matcher(editTextEmail.getText().toString());

                if(matcher.find() || TextUtils.isEmpty(editTextEmail.getText().toString())){
                    parent.setErrorEnabled(false);
                }
                else {
                    parent.setErrorEnabled(true);
                    parent.setError("Некорректная электронная почта");
                }
            }
        });
        editTextPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                parent = (TextInputLayout) findViewById(editTextPassword.getId()).getParent().getParent();

                //Pattern pattern = Pattern.compile("(?=.*[A-Z].*[A-Z])(?=.*[!@#$&*])(?=.*[0-9].*[0-9])(?=.*[a-z].*[a-z].*[a-z]).{8,}");
                Pattern pattern = Pattern.compile("(?=.*[a-z].*[a-z].*[a-z]).{8,}");
                Matcher matcher = pattern.matcher(editTextPassword.getText().toString());

                if(matcher.find() || TextUtils.isEmpty(editTextPassword.getText().toString())){
                    parent.setErrorEnabled(false);
                }
                else{
                    parent.setErrorEnabled(true);
                    parent.setError("Пароль должен включать в себя не менее 8 символов!");
                }
            }
        });
        ///////////////////////////////////////////////

    }

    private void backToAuthorization(){
        finish();
    }


    private void SignIn(){
        String name, email, password;

        ArrayList<TextInputEditText> list = new ArrayList<TextInputEditText>();
        list.add(editTextEmail);
        list.add(editTextPassword);
        list.add(editTextName);

        for(TextInputEditText item : list){
            parent = (TextInputLayout) findViewById(item.getId()).getParent().getParent();

            if(parent.isErrorEnabled() || TextUtils.isEmpty(item.getText().toString())){
                dialogHelper.dialogDismiss();
                Snackbar snackbar = Snackbar.make(root, "Заполните все поля корректно!", Snackbar.LENGTH_INDEFINITE);
                snackbar.setAction("Скрыть", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        snackbar.dismiss();
                    }
                }).setActionTextColor(Color.parseColor("#4F98CA"));;
                snackbar.show();
                return;
            }
        }
        list.clear();

        email = editTextEmail.getText().toString().trim();
        password = editTextPassword.getText().toString().trim();
        name = editTextName.getText().toString().trim();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                auth.createUserWithEmailAndPassword(email, password)
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                User user = new User();
                                user.setName(name);
                                user.setEmail(email);
                                user.setPassword(password);

                                //Первичный ключ и занчения
                                users.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .setValue(user)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    dialogHelper.dialogDismiss();
                                                    dialogHelper.showDialogDoneRegistr();
                                                    new Handler().postDelayed(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            dialogHelper.dialogDismiss();
                                                            finish();
                                                        }
                                                    }, 2000);
                                                } else {
                                                    dialogHelper.dialogDismiss();
                                                    dialogHelper.showDialogUndoneRegistr();
                                                    new Handler().postDelayed(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            dialogHelper.dialogDismiss();
                                                        }
                                                    }, 2000);
                                                }
                                            }
                                        });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                dialogHelper.dialogDismiss();
                                dialogHelper.showDialogUndoneRegistr();
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        dialogHelper.dialogDismiss();
                                        parent = (TextInputLayout) findViewById(editTextEmail.getId()).getParent().getParent();
                                        parent.setErrorEnabled(true);
                                        parent.setError("Данная почта уже используется!");
                                        editTextEmail.requestFocus();
                                    }
                                }, 2000);
                            }
                        });
            }
        }, 2000);


    }
}