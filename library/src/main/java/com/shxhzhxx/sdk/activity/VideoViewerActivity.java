package com.shxhzhxx.sdk.activity;

public class VideoViewerActivity extends ForResultActivity {
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
