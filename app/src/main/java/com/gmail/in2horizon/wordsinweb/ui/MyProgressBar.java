package com.gmail.in2horizon.wordsinweb.ui;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.AndroidException;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import com.gmail.in2horizon.wordsinweb.R;

public class MyProgressBar extends ConstraintLayout {


    private static final String TAG = "MyPRogressBar";

    public MyProgressBar(@NonNull Context context) {
        super(context);
        init(context);
    }

    public MyProgressBar(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater inflater =
                (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.my_progress_bar, this);

    }

    public void show() {
        MyProgressBar myProgressBar=((View) getParent()).findViewById(R.id.my_progress_bar);
       myProgressBar.setBackgroundColor(0xaa000000);
        myProgressBar.setClickable(true);
        myProgressBar.setVisibility(VISIBLE);
    }
    public void hide(){
        MyProgressBar myProgressBar=((View) getParent()).findViewById(R.id.my_progress_bar);
        myProgressBar.setBackgroundColor(0x00000000);
        myProgressBar.setClickable(false);
        myProgressBar.setVisibility(INVISIBLE);

    }
}
