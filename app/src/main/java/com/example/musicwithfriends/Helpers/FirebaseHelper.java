package com.example.musicwithfriends.Helpers;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseHelper {

    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseDatabase db = FirebaseDatabase.getInstance();
    DatabaseReference databaseReference = db.getReference();

    public FirebaseHelper() {}

    public String getIdUser() {
        return auth.getUid();
    }

    public DatabaseReference Request(String path){
      return databaseReference.child(path);
    };


}
