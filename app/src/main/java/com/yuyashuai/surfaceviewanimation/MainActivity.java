package com.yuyashuai.surfaceviewanimation;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import com.yuyashuai.surfaceanimation.SurfaceViewAnimation;


public class MainActivity extends AppCompatActivity {


    private SurfaceViewAnimation surfaceViewAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SurfaceView mSurfaceView = (SurfaceView) findViewById(R.id.sv_main);
        Button btnStart = (Button) findViewById(R.id.btn_start);
        Button btnStop = (Button) findViewById(R.id.btn_stop);
        surfaceViewAnimation = new SurfaceViewAnimation.Builder(mSurfaceView, "blacktest")
                .setRepeatMode(SurfaceViewAnimation.MODE_INFINITE)
                .setScaleType(SurfaceViewAnimation.SCALE_TYPE_CENTER_INSIDE)
                .setFrameInterval(300)
                .build();
                /*new SurfaceViewAnimation.Builder(mSurfaceView,file)
                .setRepeatMode(SurfaceViewAnimation.MODE_INFINITE)
                .setFrameInterval(80)
                .setCacheCount(8)
                .build();*/

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //you should put your frame animation in ExternalStorageDirectory/zzzz/
                surfaceViewAnimation.start();
            }
        });
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                surfaceViewAnimation.stop();
            }
        });

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.e("df", newConfig.orientation + "");

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("df", "onDESTORY");
    }
}
