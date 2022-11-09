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

public class GravitatedSpinnerAdapter<T> extends ArrayAdapter<T>  {

    public GravitatedSpinnerAdapter(@NonNull Context context, int resource, @NonNull List<T> objects) {


        super(context, resource, new ArrayList<T>(objects));
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        return setGravity(super.getView(position, convertView, parent));
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent)
    {
        return setGravity(super.getDropDownView(position, convertView, parent));
    }


    private View setGravity(View view)
    {
        view.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        TextView textView = (TextView)view.findViewById(android.R.id.text1);
        textView.setPadding(5, 5, 5, 5);

        textView.setGravity(Gravity.CENTER);
        return view;
    }



}
