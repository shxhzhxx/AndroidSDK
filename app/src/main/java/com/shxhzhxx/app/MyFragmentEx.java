package com.shxhzhxx.app;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.shxhzhxx.sdk.ui.ListFragmentEx;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class MyFragmentEx extends ListFragmentEx<MyFragmentEx.Model, MyFragmentEx.MyHolder, MyFragmentEx.MyAdapter> {
    @NonNull
    @Override
    protected MyAdapter onAdapter() {
        return new MyAdapter();
    }

    @Override
    protected int pageSize() {
        return 20;
    }

    @Override
    protected void onNextPage(int page, final LoadCallback callback) {
        (new Handler()).postDelayed(new Runnable() {
            @Override
            public void run() {
                callback.onResult();
                List<Model> list = new ArrayList<>();
                for (int i = 0; i < 20; ++i) {
                    list.add(new Model());
                }
                callback.onLoad(list);
            }
        },1000);
    }

    public class MyAdapter extends RecyclerView.Adapter<MyHolder> {
        @NonNull
        @Override
        public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new MyHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull MyHolder holder, int position) {
            holder.text.setText("item" + position);
        }

        @Override
        public int getItemCount() {
            return getListSize();
        }
    }

    public static class Model {
    }

    public static class MyHolder extends RecyclerView.ViewHolder {
        TextView text;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.text);
        }
    }
}
