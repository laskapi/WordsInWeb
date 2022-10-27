package com.gmail.in2horizon.wordsinweb.ui;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class CenteredSpinnerAdapter<T> extends ArrayAdapter<T>  {

    public CenteredSpinnerAdapter(@NonNull Context context, int resource, @NonNull List<T> objects) {


        super(context, resource, new ArrayList<T>(objects));
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        return setCentered(super.getView(position, convertView, parent));
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent)
    {
        return setCentered(super.getDropDownView(position, convertView, parent));
    }


    private View setCentered(View view)
    {
        TextView textView = (TextView)view.findViewById(android.R.id.text1);
        textView.setGravity(Gravity.CENTER);
        return view;
    }



}
