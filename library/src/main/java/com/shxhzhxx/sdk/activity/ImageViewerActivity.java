package com.shxhzhxx.sdk.activity;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;

import com.github.chrisbanes.photoview.PhotoView;
import com.shxhzhxx.imageloader.ImageLoader;
import com.shxhzhxx.sdk.R;
import com.shxhzhxx.sdk.utils.FileUtils;
import com.shxhzhxx.urlloader.UrlLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

/**
 * {@link com.shxhzhxx.sdk.ui.HolderRefAdapter} could help.
 */
public class ImageViewerActivity /*extends BaseActivity implements View.OnLongClickListener*/ {
//    public static void start(Activity context, ArrayList<String> paths, int position, @Nullable List<Pair<View, String>> pairs) {
//        Intent intent = new Intent(context, ImageViewerActivity.class);
//        intent.putStringArrayListExtra("paths", paths);
//        intent.putExtra("position", position);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && pairs != null) {
//            intent.putExtra("transition", true);
//            context.startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(context, pairs.toArray(new Pair[]{})).toBundle());
//        } else {
//            intent.putExtra("transition", false);
//            context.startActivity(intent);
//        }
//    }
//
//    private ArrayList<String> mPaths;
//    private List<PhotoView> mCache = new ArrayList<>();
//    private ViewPager mPager;
//    private boolean mWaitTransition = false;
//
//    @Override
//    protected void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setStatusBarColor(Color.TRANSPARENT, true);
//        setContentView(R.layout.activity_image_viewer);
//
//        mPaths = getIntent().getStringArrayListExtra("paths");
//        int position = getIntent().getIntExtra("position", -1);
//        if (mPaths == null || position >= mPaths.size() || position < 0)
//            return;
//
//        mWaitTransition = getIntent().getBooleanExtra("transition", false);
//        mPager = findViewById(R.id.pager);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && mWaitTransition) {
//            supportPostponeEnterTransition();
//            mPager.setTransitionName(mPaths.get(position));
//        }
//        mPager.setOffscreenPageLimit(2);
//        mPager.setAdapter(new PagerAdapter() {
//            @Override
//            public int getCount() {
//                return mPaths.size();
//            }
//
//            @Override
//            public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
//                return view == object;
//            }
//
//            @NonNull
//            @Override
//            public Object instantiateItem(@NonNull ViewGroup container, final int position) {
//                PhotoView view;
//                if (mCache.isEmpty()) {
//                    view = new PhotoView(ImageViewerActivity.this);
//                    view.setOnClickListener(ImageViewerActivity.this);
//                    view.setOnLongClickListener(ImageViewerActivity.this);
//                } else {
//                    view = mCache.remove(0);
//                }
//                String path = mPaths.get(position);
//                if (mWaitTransition) {
//                    mWaitTransition = false;
//                    ImageLoader.getInstance().load(path).tag(IDENTIFY).callback(new ImageLoader.Callback() {
//                        @Override
//                        public void onComplete() {
//                            supportStartPostponedEnterTransition();
//                        }
//
//                        @Override
//                        public void onFailed() {
//                            supportStartPostponedEnterTransition();
//                        }
//
//                        @Override
//                        public void onCanceled() {
//                            supportStartPostponedEnterTransition();
//                        }
//                    }).into(view);
//                } else {
//                    ImageLoader.getInstance().load(path).tag(IDENTIFY).into(view);
//                }
//                container.addView(view);
//                return view;
//            }
//
//            @Override
//            public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
//                container.removeView((PhotoView) object);
//                mCache.add((PhotoView) object);
//            }
//        });
//        mPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
//            @Override
//            public void onPageSelected(int position) {
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                    mPager.setTransitionName(mPaths.get(position));
//                }
//            }
//        });
//        mPager.setCurrentItem(position);
//    }
//
//    @Override
//    public void onClick(View v) {
//        onBackPressed();
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        ImageLoader.getInstance().getUrlLoader().cancelByTag(TAG);
//    }
//
//    @Override
//    public boolean onLongClick(View v) {
//        new AlertDialog.Builder(v.getContext(),R.style.AutoSizeAlertDialog).setItems(new String[]{getString(R.string.save_image_to_sdcard)}, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                performRequestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, new PermissionsResultListener() {
//                    @Override
//                    public void onPermissionGranted() {
//                        String path = mPaths.get(mPager.getCurrentItem());
//                        File f = new File(path);
//                        if (f.exists()) {
//                            saveImage(f);
//                        } else {
//                            ImageLoader.getInstance().getUrlLoader().load(path, TAG, new UrlLoader.ProgressObserver() {
//                                @Override
//                                public void onComplete(File file) {
//                                    saveImage(file);
//                                }
//                            });
//                        }
//                    }
//                });
//            }
//        }).create().show();
//        return true;
//    }
//
//    private void saveImage(File file) {
//        if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
//            ToastUtils.show(getString(R.string.unavailable_external_storage));
//        } else {
//            File dest = new File(Environment.getExternalStoragePublicDirectory(
//                    Environment.DIRECTORY_PICTURES), String.format("%s.jpg", ImageLoader.getInstance().getUrlLoader().md5(UUID.randomUUID().toString())));
//            if (FileUtils.copyFile(file, dest)) {
//                ToastUtils.show(getString(R.string.image_saved_format, dest.getAbsolutePath()));
//            }
//        }
//    }
}
