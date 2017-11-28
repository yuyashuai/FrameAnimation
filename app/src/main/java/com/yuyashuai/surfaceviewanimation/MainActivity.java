package com.yuyashuai.surfaceviewanimation;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import com.yuyashuai.silkyanimation.SilkyAnimation;

import java.io.File;

/**
 * @author yuyashuai 2016-11-27 15:43:51
 */
public class MainActivity extends AppCompatActivity {


    private SilkyAnimation silkyAnimation;
    private Button btnStartAssets;
    private Button btnStartFile;
    private Button btnStop;
    private SurfaceView mSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        silkyAnimation = new SilkyAnimation.Builder(mSurfaceView)
                .setCacheCount(8)
                .setScaleType(SilkyAnimation.SCALE_TYPE_CENTER)
                .setRepeatMode(SilkyAnimation.MODE_INFINITE)
                .build();
        //如果需要在onCreate方法中直接调用开始，通过handler.postDelayed添加一个时延
        //if you call start() in onCreate method,use handler.postDelayed add a time delay to call it.
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                silkyAnimation.start("blacktest");
            }
        }, 40);

        //从文件读取 file resources
        final File file = new File(Environment.getExternalStorageDirectory() + File.separator + "huabanyu");
        btnStartFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                silkyAnimation.start(file);
            }
        });
        //从assets读取 assets resources
        btnStartAssets.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                silkyAnimation.start("crow", 30);
            }
        });
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                silkyAnimation.stop();
            }
        });
    }

    private void initView() {
        mSurfaceView = (SurfaceView) findViewById(R.id.sv_main);
        btnStartAssets = (Button) findViewById(R.id.btn_start_assets);
        btnStartFile = (Button) findViewById(R.id.btn_start_file);
        btnStop = (Button) findViewById(R.id.btn_stop);
    }

}
