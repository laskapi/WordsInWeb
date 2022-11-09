package com.gmail.in2horizon.wordsinweb.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gmail.in2horizon.wordsinweb.R;

import java.util.List;

public class RecyclerItemAdapter extends RecyclerView.Adapter<RecyclerItemAdapter.ItemViewHolder> {

    private static final String TAG = RecyclerItemAdapter.class.getSimpleName();
    private final Context context;
    private List<String> dataSet;
    private int selected_position;

    public RecyclerItemAdapter(Context context, List<String> dataSet) {
        this.context = context;
        setDataSet(dataSet);
    }

    public void setDataSet(List<String> dataSet) {
        this.dataSet = dataSet;
        selected_position = -1;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View adapterLayout = layoutInflater.inflate(R.layout.list_item, parent, false);
        return new ItemViewHolder(adapterLayout);
    }


    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        String item = dataSet.get(position);
        holder.textView.setText(item);
        Resources.Theme theme = context.getTheme();
        if (selected_position == position) {
            holder.textView.setBackgroundColor(context.getColor(R.color.purple_700));
            holder.textView.setTextColor(Color.WHITE);
        } else {
            holder.textView.setTextColor(Color.BLACK);
            if (position % 2 == 0) {
                holder.textView.setBackgroundColor(context.getColor(R.color.background_lite));
            } else {
                holder.textView.setBackgroundColor(context.getColor(R.color.background_dark));
            }
        }



        holder.textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selected_position == position) {
                    selected_position = -1;
                    notifyDataSetChanged();
                    return;
                }
                selected_position = position;
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }


    public int getSelected() {

        return selected_position;
    }

    public void deselect() {
        selected_position = -1;
    }


    class ItemViewHolder extends RecyclerView.ViewHolder {

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        TextView textView = itemView.findViewById(R.id.item_title);
    }
}
