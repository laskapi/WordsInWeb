package com.gmail.in2horizon.wordsinweb.ui;

import android.content.Context;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

public abstract class SpinnerObserverAdapter<T> extends GravitatedSpinnerAdapter<T> implements Observer {


    public SpinnerObserverAdapter(@NonNull Context context, int resource,
                                  @NonNull List objects) {
        super(context, resource, objects);
    }
}
