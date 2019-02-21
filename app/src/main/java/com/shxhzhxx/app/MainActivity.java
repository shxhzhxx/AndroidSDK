package com.shxhzhxx.app;

import android.os.Bundle;
import android.view.View;

import com.shxhzhxx.sdk.activity.ImageViewerActivity;

import java.util.ArrayList;
import java.util.Collections;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        findViewById(R.id.imageViewer).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                ArrayList<String> list=new ArrayList<>();
//                list.add("https://static.usasishu.com/image/2018/09/29/course_bg2_new.jpg");
//                list.add("https://static.usasishu.com/image/2018/09/30/bg-china-map.png");
//                list.add("https://static.usasishu.com/image/2018/09/30/bg-index.jpg");
//                list.add("https://static.usasishu.com/image/2018/10/12/course_first_bg.png");
//                list.add("https://static.usasishu.com/image/2018/10/12/how_to_learn_banner.png");
//                ImageViewerActivity.start(MainActivity.this,list,0,null);
//            }
//        });
    }
}
