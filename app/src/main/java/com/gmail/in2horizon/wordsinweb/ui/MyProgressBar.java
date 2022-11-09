package com.gmail.in2horizon.wordsinweb.ui;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.gmail.in2horizon.wordsinweb.R;

public class MyProgressBar extends ConstraintLayout {


    private static final String TAG = "MyPRogressBar";
    private TextView progressTextView;

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
        progressTextView = findViewById(R.id.progressTextView);
    }

    public void setProgress(Object amount) {
        if (amount==null) {
            progressTextView.setText(getContext().getString(R.string.done));
          Handler handler= getHandler();
          if(handler!=null) {
              handler.postDelayed(this::hide, 1000);
          }
        } else {
            progressTextView.setText(getContext().getString(R.string.loading, (String) amount));
        }
    }

    public void show() {
        setProgress("");
        setBackgroundColor(0xaa000000);
        setClickable(true);
        setVisibility(VISIBLE);
    }

    public void hide() {
        setBackgroundColor(0x00000000);
        setClickable(false);
        setVisibility(INVISIBLE);

    }
}
