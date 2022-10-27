package com.gmail.in2horizon.wordsinweb.ui;

import android.content.Context;

import androidx.annotation.NonNull;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

public abstract class CenteredSpinnerObserverAdapter<T> extends CenteredSpinnerAdapter<T> implements Observer {


    public CenteredSpinnerObserverAdapter(@NonNull Context context, int resource,
                                          @NonNull List objects) {
        super(context, resource, objects);
    }
}
