package com.shxhzhxx.sdk.activity;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.shxhzhxx.imageloader.ImageLoader;
import com.shxhzhxx.sdk.R;

import java.util.ArrayList;
import java.util.List;

public class ImageViewerActivity extends BaseActivity {
    public static void start(Activity context, ArrayList<String> paths, int position, @Nullable List<Pair<View, String>> pairs) {
        Intent intent = new Intent(context, ImageViewerActivity.class);
        intent.putStringArrayListExtra("paths", paths);
        intent.putExtra("position", position);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && pairs != null) {
            intent.putExtra("transition", true);
            context.startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(context, pairs.toArray(new Pair[]{})).toBundle());
        } else {
            intent.putExtra("transition", false);
            context.startActivity(intent);
        }
    }

    private ArrayList<String> mPaths;
    private List<ImageView> mCache = new ArrayList<>();
    private ViewPager mPager;
    private boolean mWaitTransition = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);

        mPaths = getIntent().getStringArrayListExtra("paths");
        int position = getIntent().getIntExtra("position", -1);
        if (mPaths == null || position >= mPaths.size() || position < 0)
            return;

        mWaitTransition = getIntent().getBooleanExtra("transition", false);
        mPager = findViewById(R.id.pager);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && mWaitTransition) {
            supportPostponeEnterTransition();
            mPager.setTransitionName(mPaths.get(position));
        }
        mPager.setOffscreenPageLimit(2);
        mPager.setAdapter(new PagerAdapter() {
            @Override
            public int getCount() {
                return mPaths.size();
            }

            @Override
            public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
                return view == object;
            }

            @NonNull
            @Override
            public Object instantiateItem(@NonNull ViewGroup container, final int position) {
                ImageView view;
                if (mCache.isEmpty()) {
                    view = new ImageView(ImageViewerActivity.this);
                    view.setOnClickListener(ImageViewerActivity.this);
                } else {
                    view = mCache.remove(0);
                }
                String path = mPaths.get(position);
                if (mWaitTransition) {
                    mWaitTransition = false;
                    ImageLoader.getInstance().load(path).tag(IDENTIFY).callback(new ImageLoader.Callback() {
                        @Override
                        public void onComplete() {
                            supportStartPostponedEnterTransition();
                        }

                        @Override
                        public void onFailed() {
                            supportStartPostponedEnterTransition();
                        }

                        @Override
                        public void onCanceled() {
                            supportStartPostponedEnterTransition();
                        }
                    }).into(view);
                } else {
                    ImageLoader.getInstance().load(path).tag(IDENTIFY).into(view);
                }
                container.addView(view);
                return view;
            }

            @Override
            public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
                container.removeView((ImageView) object);
                mCache.add((ImageView) object);
            }
        });
        mPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mPager.setTransitionName(mPaths.get(position));
                }
            }
        });
        mPager.setCurrentItem(position);
    }

    @Override
    public void onClick(View v) {
        onBackPressed();
    }

}
