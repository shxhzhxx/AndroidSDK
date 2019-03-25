package com.shxhzhxx.app;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.shxhzhxx.imageloader.ImageLoader;
import com.shxhzhxx.sdk.activity.BaseActivity;
import com.shxhzhxx.sdk.activity.ImageViewerActivity;
import com.shxhzhxx.sdk.utils.FileUtils;

import java.util.ArrayList;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final ImageView iv= findViewById(R.id.iv);
        findViewById(R.id.load).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageLoader.getInstance()
                        .load("https://static.usasishu.com/image/2018/09/30/bg-index.jpg")
                        .into(iv);
//                getPhotoByAlbum(new GetMediaListener() {
//                    @Override0
//                    public void onSuccess(Uri uri) {
//                        ArrayList<String> list = new ArrayList<>();
//                        list.add(FileUtils.getFileByUri(MainActivity.this, uri).getAbsolutePath());
//                        list.add("https://static.usasishu.com/image/2018/09/29/course_bg2_new.jpg");
//                        list.add("https://static.usasishu.com/image/2018/09/30/bg-china-map.png");
//                        list.add("https://static.usasishu.com/image/2018/09/30/bg-index.jpg");
//                        list.add("https://static.usasishu.com/image/2018/10/12/course_first_bg.png");
//                        list.add("https://static.usasishu.com/image/2018/10/12/how_to_learn_banner.png");
//                        ImageViewerActivity.start(MainActivity.this, list, 0, null);
//                    }
//                });
            }
        });
    }
}
