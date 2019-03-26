package com.shxhzhxx.sdk.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.View;

import com.shxhzhxx.sdk.R;
import com.shxhzhxx.sdk.ui.VideoViewer;

import java.io.IOException;

public class VideoViewerActivity extends BaseActivity {
//    public static void start(Context context, String url) {
//        context.startActivity(new Intent(context, VideoViewerActivity.class).putExtra("url", url));
//    }
//
//    private VideoViewer videoViewer;
//
//    @Override
//    protected void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setStatusBarColor(Color.BLACK, false);
//        setContentView(R.layout.activity_video_viewer);
//
//        findViewById(R.id.back).setOnClickListener(this);
//
//        videoViewer = findViewById(R.id.video);
//        videoViewer.getPreview().setImageDrawable(new ColorDrawable(Color.BLACK));
//        try {
//            videoViewer.setDataSource(getIntent().getStringExtra("url"));
//            videoViewer.start();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//
//    @Override
//    public void onClick(View v) {
//        onBackPressed();
//    }
//
//    @Override
//    public void onBackPressed() {
//        super.onBackPressed();
//        videoViewer.release();
//    }
}
