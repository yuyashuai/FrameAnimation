package com.yuyashuai.surfaceviewanimation;

import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.yuyashuai.silkyanimation.SilkyAnimation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author yuyashuai 2016-11-27 15:43:51
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    private SilkyAnimation silkyAnimation;
    private Button btnStartAssets;
    private Button btnStartFile;
    private Button btnStop;
    private SurfaceView mSurfaceView;
    private ImageView iv_main;
    //TODO 你需要自定义你的文件路径
    private String fileDir;
    private String file1Dir;
    private String file2Dir;
    private Button btn_start_files;
    private Button btn_start_assets_files;
    private final String TAG = "SilkyAnimationMaster";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        silkyAnimation = new SilkyAnimation.Builder(mSurfaceView)
                .setCacheCount(5)
                .setFrameInterval(80)
                .setScaleType(SilkyAnimation.SCALE_TYPE_FIT_XY)
                .build();
        //如果需要在onCreate方法中直接调用开始，通过handler.postDelayed添加一个时延
        //if you call start() in onCreate method,use handler.postDelayed add a time delay to call it.
        new Handler().postDelayed(() -> silkyAnimation.start("blacktest"), 40);


        //从assets读取 assets resources
        btnStartAssets.setOnClickListener(view -> {
                    silkyAnimation.setSupportInBitmap(false);
                    silkyAnimation.setRepeatMode(SilkyAnimation.MODE_ONCE);
                    silkyAnimation.start("huabanyu", 40);
                }
        );

        //从多个assets文件夹中读取
        final List<String> mAssetsPathList = new ArrayList<>();
        mAssetsPathList.addAll(getPathList("crow"));
        mAssetsPathList.addAll(getPathList("daku"));
        btn_start_assets_files.setOnClickListener(v -> silkyAnimation.startWithAssetsPathList(mAssetsPathList));
        btnStartFile.setOnClickListener(view -> {
            Toast.makeText(getApplicationContext(), "请配置你的文件路径", Toast.LENGTH_LONG).show();
        });

        if (!TextUtils.isEmpty(fileDir)) {
            //从文件读取 file resources
            final File file = new File(Environment.getExternalStorageDirectory(), fileDir);
            btnStartFile.setOnClickListener(view -> silkyAnimation.start(file));
        }

        btn_start_files.setOnClickListener(v -> {
            Toast.makeText(getApplicationContext(), "请配置你的文件路径", Toast.LENGTH_LONG).show();
        });

        if (!TextUtils.isEmpty(file1Dir) && !TextUtils.isEmpty(file2Dir)) {
            //从多个文件中读取,自定义文件路径
            final List<String> mFilePathList = new ArrayList<>();
            final File file = new File(Environment.getExternalStorageDirectory(), fileDir);
            final File file1 = new File(Environment.getExternalStorageDirectory(), file1Dir);
            final File file2 = new File(Environment.getExternalStorageDirectory(), file2Dir);
            Log.e(TAG,file.getAbsolutePath());
            File[] files = file.listFiles();
            for (File mFrameFile : files) {
                mFilePathList.add(mFrameFile.getAbsolutePath());
            }
            File[] files1 = file1.listFiles();
            for (File mFrameFile : files1) {
                mFilePathList.add(mFrameFile.getAbsolutePath());
            }
            File[] files2 = file2.listFiles();
            for (File mFrameFile : files2) {
                mFilePathList.add(mFrameFile.getAbsolutePath());
            }
            btn_start_files.setOnClickListener(v -> silkyAnimation.startWithFilePathList(mFilePathList));
        }

        btnStop.setOnClickListener(view -> silkyAnimation.stop());
    }


    /**
     * @param assetsPath assets文件路径
     * @return 文件夹中的文件集合
     */
    private List<String> getPathList(String assetsPath) {
        AssetManager assetManager = getAssets();
        try {
            String[] assetFiles = assetManager.list(assetsPath);
            if (assetFiles.length == 0) {
                Log.e(TAG, "no file in this asset directory");
                return new ArrayList<>(0);
            }
            //转换真实路径
            for (int i = 0; i < assetFiles.length; i++) {
                assetFiles[i] = assetsPath + File.separator + assetFiles[i];
            }
            List<String> mAssertList = Arrays.asList(assetFiles);
            return mAssertList;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<>(0);
    }

    private void initView() {
        mSurfaceView = (SurfaceView) findViewById(R.id.sv_main);
        btnStartAssets = (Button) findViewById(R.id.btn_start_assets);
        btnStartFile = (Button) findViewById(R.id.btn_start_file);
        btnStop = (Button) findViewById(R.id.btn_stop);
        iv_main = (ImageView) findViewById(R.id.iv_main);
        btn_start_files = (Button) findViewById(R.id.btn_start_files);
        btn_start_assets_files = (Button) findViewById(R.id.btn_start_assets_files);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_start_files:

                break;
            case R.id.btn_start_assets_files:

                break;
        }
    }
}
