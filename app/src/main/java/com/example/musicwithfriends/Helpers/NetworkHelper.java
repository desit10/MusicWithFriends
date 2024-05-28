package com.example.musicwithfriends.Helpers;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;

import com.example.musicwithfriends.R;

public class NetworkHelper {

    private Context context;
    private ConnectivityManager connMgr;
    private NetworkInfo networkInfo;
    private AnimatedVectorDrawableCompat animatedVectorDrawableCompat;
    private AnimatedVectorDrawable animatedVectorDrawable;
    private Dialog dialog;

    public NetworkHelper(Context context) {
        this.context = context;
        this.dialog = new Dialog(context);
    }

    public boolean checkNetworkConnection(){
        connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        networkInfo = connMgr.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isAvailable()) {
            dialog.dismiss();
            return true;
        } else {
            showDialogNoConnection();
            return false;
        }
    }
    public void showDialogNoConnection(){
        dialog.setContentView(R.layout.dialog_no_connection);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);

        Button btnClose = dialog.findViewById(R.id.btnClose);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        ImageView imgOffWifi = dialog.findViewById(R.id.imgOffWifi);

        Drawable drawableCircle = imgOffWifi.getDrawable();

        if(drawableCircle instanceof AnimatedVectorDrawableCompat){
            animatedVectorDrawableCompat = (AnimatedVectorDrawableCompat) drawableCircle;
            animatedVectorDrawableCompat.start();
        } else if (drawableCircle instanceof AnimatedVectorDrawable) {
            animatedVectorDrawable = (AnimatedVectorDrawable) drawableCircle;
            animatedVectorDrawable.start();
        }

        dialog.show();
    }
}
