package com.example.musicwithfriends.Helpers;


import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;

import com.example.musicwithfriends.R;

public class DialogHelper {
    private Dialog dialog;
    private AnimatedVectorDrawableCompat animatedVectorDrawableCompat;
    private AnimatedVectorDrawable animatedVectorDrawable;

    //Обычные диалоговые окна

    public DialogHelper(Context context) {
        this.dialog = new Dialog(context);
    }
    private void dialogCreate(int layout){
        dialog.setContentView(layout);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
    }
    private void dialogAnimationStart(ImageView circle, ImageView item){

        Drawable drawableCircle = circle.getDrawable();
        Drawable drawableDone = item.getDrawable();

        if(drawableCircle instanceof AnimatedVectorDrawableCompat){
            animatedVectorDrawableCompat = (AnimatedVectorDrawableCompat) drawableCircle;
            animatedVectorDrawableCompat.start();
        } else if (drawableCircle instanceof  AnimatedVectorDrawable) {
            animatedVectorDrawable = (AnimatedVectorDrawable) drawableCircle;
            animatedVectorDrawable.start();
        }

        if(drawableDone instanceof AnimatedVectorDrawableCompat){
            animatedVectorDrawableCompat = (AnimatedVectorDrawableCompat) drawableDone;
            animatedVectorDrawableCompat.start();
        } else if (drawableDone instanceof  AnimatedVectorDrawable) {
            animatedVectorDrawable = (AnimatedVectorDrawable) drawableDone;
            animatedVectorDrawable.start();
        }
    }
    public void showDialogProgressBar(){
        dialogCreate(R.layout.dialog_progress_registration);

        ProgressBar progressBar = dialog.findViewById(R.id.progressBar);
        progressBar.getIndeterminateDrawable().setColorFilter(Color.parseColor("#FF9234"), PorterDuff.Mode.SRC_IN);
        progressBar.setVisibility(View.VISIBLE);

        dialog.show();
    }
    public void showDialogDoneRegistr(){
        dialogCreate(R.layout.dialog_done_registration);

        ImageView circle = dialog.findViewById(R.id.animationCircle);
        ImageView done = dialog.findViewById(R.id.animationDone);

        dialogAnimationStart(circle, done);

        dialog.show();
    }
    public void showDialogUndoneRegistr(){
        dialogCreate(R.layout.dialog_undone_registration);

        ImageView circle = dialog.findViewById(R.id.animationCircle);
        ImageView undone = dialog.findViewById(R.id.animationUndone);

        dialogAnimationStart(circle, undone);

        dialog.show();
    }

    public void dialogDismiss(){
        dialog.dismiss();
    }

}
